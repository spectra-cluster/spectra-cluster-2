package org.spectra.cluster.tools;

import org.bigbio.pgatk.io.mapcache.IMapStorage;
import org.bigbio.pgatk.io.objectdb.ObjectsDB;
import org.bigbio.pgatk.io.properties.IPropertyStorage;
import org.bigbio.pgatk.io.properties.InMemoryPropertyStorage;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.spectra.cluster.binning.SimilarSizedClusterBinner;
import org.spectra.cluster.cdf.MinNumberComparisonsAssessor;
import org.spectra.cluster.engine.GreedyClusteringEngine;
import org.spectra.cluster.filter.binaryspectrum.HighestPeakPerBinFunction;
import org.spectra.cluster.filter.rawpeaks.*;
import org.spectra.cluster.io.cluster.ClusterStorageFactory;
import org.spectra.cluster.io.cluster.ObjectDBGreedyClusterStorage;
import org.spectra.cluster.io.spectra.MzSpectraReader;
import org.spectra.cluster.model.cluster.GreedySpectralCluster;
import org.spectra.cluster.model.cluster.ICluster;
import org.spectra.cluster.model.cluster.IClusterProperties;
import org.spectra.cluster.model.consensus.GreedyConsensusSpectrum;
import org.spectra.cluster.normalizer.BasicIntegerNormalizer;
import org.spectra.cluster.normalizer.MaxPeakNormalizer;
import org.spectra.cluster.normalizer.TideBinner;
import org.spectra.cluster.predicates.ShareHighestPeaksClusterPredicate;
import org.spectra.cluster.similarity.CombinedFisherIntensityTest;

import java.io.File;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

public class LocalParallelBinnedClusteringToolTest {
    private URI[] mgfFiles;
    private Path testDir;

    @Before
    public void setUp() throws Exception {
        mgfFiles = new URI[] {
                getClass().getClassLoader().getResource("same_sequence_cluster.mgf").toURI(),
                getClass().getClassLoader().getResource("synthetic_mixed_runs.mgf").toURI(),
                getClass().getClassLoader().getResource("imp_hela_test.mgf").toURI()
        };

        testDir = Files.createTempDirectory("clusters-");
    }

    private IClusterProperties[] loadTestCluster(IMapStorage<ICluster> storage) throws Exception {
        // ignore the property storage for now
        IPropertyStorage propertyStorage = new InMemoryPropertyStorage();

        IRawSpectrumFunction loadingFilter = new RemoveImpossiblyHighPeaksFunction()
                .specAndThen(new RemovePrecursorPeaksFunction(0.5))
                .specAndThen(new RawPeaksWrapperFunction(new KeepNHighestRawPeaks(40)));

        // create a basic clustering engine for testing
        GreedyClusteringEngine engine = new GreedyClusteringEngine(BasicIntegerNormalizer.MZ_CONSTANT,
                1, 0.99f, 5, new CombinedFisherIntensityTest(),
                new MinNumberComparisonsAssessor(10000), new ShareHighestPeaksClusterPredicate(5),
                GreedyConsensusSpectrum.NOISE_FILTER_INCREMENT);

        File[] inFiles = Arrays.stream(mgfFiles).map(File::new).toArray(File[]::new);

        // read all files at once
        MzSpectraReader reader = new MzSpectraReader(new TideBinner(), new MaxPeakNormalizer(),
                new BasicIntegerNormalizer(), new HighestPeakPerBinFunction(), loadingFilter,
                GreedyClusteringEngine.COMPARISON_FILTER, engine, inFiles);

        // create the iterator
        Iterator<ICluster> iterator = reader.readClusterIterator(propertyStorage);

        // keep track of the cluster ids
        List<IClusterProperties> clusterProperties = new ArrayList<>(10_000);

        while (iterator.hasNext()) {
            ICluster cluster = iterator.next();
            String storageId = cluster.getId();

            // store the cluster
            storage.put(storageId, cluster);
            clusterProperties.add(cluster.getProperties());
        }

        return clusterProperties.toArray(new IClusterProperties[0]);
    }

    @Test
    public void testParallelClustering() throws Exception {
        IMapStorage<ICluster> clusterStorageWriter = ClusterStorageFactory.buildDynamicStorage(
                testDir.toFile(), GreedySpectralCluster.class);

        // load the clusters
        IClusterProperties[] testClusters = loadTestCluster(clusterStorageWriter);

        clusterStorageWriter.close();

        // open the storage for reading
        IMapStorage<ICluster> clusterStorage = ClusterStorageFactory.openDynamicStorage(testDir.toFile(),
                GreedySpectralCluster.class);

        // create the clusterer
        LocalParallelBinnedClusteringTool clusterer = new LocalParallelBinnedClusteringTool(
                2, testDir.toFile(),
                new SimilarSizedClusterBinner(BasicIntegerNormalizer.MZ_CONSTANT * 2, 10, false),
                GreedySpectralCluster.class);

        File finalResultFile = new File(testDir.toFile(), "result.bcs");

        clusterer.runClustering(testClusters, clusterStorage, finalResultFile,
            BasicIntegerNormalizer.MZ_CONSTANT,
            1, 0.99f, 5, new CombinedFisherIntensityTest(),
            new MinNumberComparisonsAssessor(10000), new ShareHighestPeaksClusterPredicate(5),
            GreedyConsensusSpectrum.NOISE_FILTER_INCREMENT);

        // make sure the final result file exists
        Assert.assertTrue(finalResultFile.exists());

        // open the final result
        ObjectDBGreedyClusterStorage resultReader = new ObjectDBGreedyClusterStorage(new ObjectsDB(finalResultFile.getAbsolutePath(), false));
        int totalClusters = 0;
        int totalSpectra = 0;

        while (resultReader.hasNext()) {
            GreedySpectralCluster cluster = (GreedySpectralCluster) resultReader.next();

            totalClusters++;

            // count the number of spectra
            totalSpectra += cluster.getClusteredSpectraCount();
        }

        Assert.assertEquals(6958, totalClusters);
        Assert.assertEquals(testClusters.length, totalSpectra);
    }
}
