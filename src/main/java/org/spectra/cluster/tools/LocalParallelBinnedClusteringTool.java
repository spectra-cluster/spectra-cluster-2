package org.spectra.cluster.tools;

import io.github.bigbio.pgatk.io.common.PgatkIOException;
import io.github.bigbio.pgatk.io.mapcache.IMapStorage;
import io.github.bigbio.pgatk.io.objectdb.LongObject;
import io.github.bigbio.pgatk.io.objectdb.ObjectsDB;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.spectra.cluster.binning.IClusterBinner;
import org.spectra.cluster.engine.IClusteringEngine;
import org.spectra.cluster.exceptions.SpectraClusterException;
import org.spectra.cluster.io.cluster.ClusterStorageFactory;
import org.spectra.cluster.io.cluster.ObjectDBGreedyClusterStorage;
import org.spectra.cluster.model.cluster.GreedySpectralCluster;
import org.spectra.cluster.model.cluster.ICluster;
import org.spectra.cluster.model.cluster.IClusterProperties;
import org.spectra.cluster.util.ClusteringParameters;

import java.io.File;
import java.util.Arrays;
import java.util.concurrent.ForkJoinPool;

/**
 * This class clusters binned clusters using local temporary files
 * as storage.
 *
 * @author jg
 */
@Data
@Slf4j
public class LocalParallelBinnedClusteringTool {
    private final int parallelJobs;
    private final File temporaryStorageDir;
    private final IClusterBinner binner;
    private final Class clusterClass;

    public void runClustering(IClusterProperties[] clusters, IMapStorage<ICluster> clusterStorage,
                              ClusteringParameters clusteringParameters) throws SpectraClusterException {
        log.debug("------ Local parallel binned clustering -----");

        try {
            // bin the spectra
            log.debug("Binning spectra...");
            String[][] binnedClusterIds = binner.binClusters(clusters, false);

            log.debug(String.format("Clusters binned in %d bins. Starting parallel clustering using %d threads...", binnedClusterIds.length, parallelJobs));

            // need a temporary storage for the result of the first round
            File firstRoundStorageDir = new File(temporaryStorageDir.getAbsolutePath(), "first_round");
            if (!firstRoundStorageDir.mkdir())
                throw new SpectraClusterException("Failed to create storage directory " + firstRoundStorageDir.getAbsolutePath());

            IMapStorage<ICluster> firstRoundStorage = ClusterStorageFactory.buildTemporaryDynamicStorage(firstRoundStorageDir, clusterClass);

            // ensure that all clusters were written in the initial storage
            clusterStorage.flush();

            // cluster the initially binned clusters
            IClusterProperties[][] firstRoundResult = clusterMapped(binnedClusterIds, clusterStorage, firstRoundStorage,
                    clusteringParameters);

            // close the initial storage
            clusterStorage.close();

            // flatten the first round result
            IClusterProperties[] flatFirstRoundResult = Arrays.stream(firstRoundResult)
                    .flatMap(Arrays::stream)
                    .toArray(IClusterProperties[]::new);

            // repeat for the second round with shifted windows
            String[][] rebinnedClusterIds = binner.binClusters(flatFirstRoundResult, true);

            // need another temporary storage for the final result
            File secondRoundStorageDir = new File(temporaryStorageDir.getAbsolutePath(), "second_round");
            if (!secondRoundStorageDir.mkdir())
                throw new SpectraClusterException("Failed to create storage directory " + secondRoundStorageDir.getAbsolutePath());

            IMapStorage<ICluster> secondRoundStorage = ClusterStorageFactory.buildTemporaryDynamicStorage(secondRoundStorageDir, clusterClass);

            // run the clustering again on the re-binned ids
            IClusterProperties[][] secondRoundResult = clusterMapped(rebinnedClusterIds, firstRoundStorage, secondRoundStorage,
                    clusteringParameters);

            // close the first round storage - thereby deleting the temporary data
            firstRoundStorage.close();

            // write the final clusters to file
            ObjectDBGreedyClusterStorage writer = new ObjectDBGreedyClusterStorage(
                    new ObjectsDB(clusteringParameters.getOutputFile().getAbsolutePath(), true));

            // write all clusters to storage
            Arrays.stream(secondRoundResult).flatMap(Arrays::stream)
                    .forEach((IClusterProperties cp) -> {
                        try {
                            GreedySpectralCluster cluster = (GreedySpectralCluster) secondRoundStorage.get(cp.getId());
                            writer.addGreedySpectralCluster(LongObject.asLongHash(cluster.getId()), cluster);
                        }
                        catch (Exception e) {
                            throw new RuntimeException(e);
                        }
                    });

            // save the final file
            writer.writeDBMode();
            writer.flush();

            // close and remove temporary storage
            secondRoundStorage.close();
        } catch (Exception e) {
            throw new SpectraClusterException("Parallel clustering failed", e);
        }
    }

    /**
     * Write clusters to a shared storage.
     *
     * The function ensures that only one writing process is active at a time.
     *
     * Clusters are stored with their id as key.
     *
     * @param storage The storage to use. After writing all clusters, "flush" is called on the storage object.
     * @param clusters The clusters to write.
     * @throws PgatkIOException Thrown on I/O errors.
     */
    private synchronized void writeClusters(IMapStorage<ICluster> storage, ICluster[] clusters) throws PgatkIOException {
        log.debug("Writing clusters...");
        for (ICluster cluster : clusters)  {
            storage.put(cluster.getId(), cluster);
        }

        try {
            storage.flush();
        } catch (PgatkIOException e) {
            log.error("Failed to flush clusters");
            e.printStackTrace();
            throw e;
        }

        log.debug("Writing clusters DONE.");
    }

    private IClusterProperties[][] clusterMapped(String[][] binnedClusterIds, IMapStorage<ICluster> clusterStorage,
                                                 IMapStorage<ICluster> resultStorage, ClusteringParameters clusteringParameters) throws Exception {
        // start the clustering
        ForkJoinPool clusteringPool = new ForkJoinPool(parallelJobs, ForkJoinPool.defaultForkJoinWorkerThreadFactory, null, true);
        return clusteringPool.submit(() -> Arrays.stream(binnedClusterIds).parallel().map((String[] clusterIds) -> {
            try {
                IClusteringEngine engine = clusteringParameters.createGreedyClusteringEngine();

                // load the clusters - parallel reads are not a problem
                ICluster[] loadedClusters = new ICluster[clusterIds.length];

                for (int i = 0; i < clusterIds.length; i++) {
                    try {
                        loadedClusters[i] = clusterStorage.get(clusterIds[i]);
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                }

                // run the clustering
                ICluster[] result = engine.clusterSpectra(loadedClusters);

                // save the clusters
                writeClusters(resultStorage, result);

                // return the properties
                return Arrays.stream(result).map(ICluster::getProperties).toArray(IClusterProperties[]::new);
            } catch (Exception e) {
                log.error("Clustering failed: " + e.toString());
                throw new RuntimeException(e);
            }
        }).toArray(IClusterProperties[][]::new)).get();
    }
}
