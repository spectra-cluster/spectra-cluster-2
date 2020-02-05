package org.spectra.cluster.io;

import lombok.extern.slf4j.Slf4j;
import org.bigbio.pgatk.io.common.PgatkIOException;
import org.bigbio.pgatk.io.mapcache.IMapStorage;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.spectra.cluster.cdf.MinNumberComparisonsAssessor;
import org.spectra.cluster.engine.GreedyClusteringEngine;
import org.spectra.cluster.engine.IClusteringEngine;
import org.spectra.cluster.exceptions.SpectraClusterException;
import org.spectra.cluster.filter.binaryspectrum.HighestPeakPerBinFunction;
import org.spectra.cluster.filter.rawpeaks.*;
import org.spectra.cluster.io.cluster.ClusterStorageFactory;
import org.spectra.cluster.io.cluster.SparkKeyClusterStorage;
import org.spectra.cluster.io.spectra.MzSpectraReader;
import org.spectra.cluster.model.cluster.GreedySpectralCluster;
import org.spectra.cluster.model.cluster.ICluster;
import org.spectra.cluster.model.consensus.GreedyConsensusSpectrum;
import org.spectra.cluster.normalizer.BasicIntegerNormalizer;
import org.spectra.cluster.normalizer.MaxPeakNormalizer;
import org.spectra.cluster.normalizer.TideBinner;
import org.spectra.cluster.predicates.ShareHighestPeaksClusterPredicate;
import org.spectra.cluster.similarity.CombinedFisherIntensityTest;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.*;
import java.util.stream.IntStream;

/**
 * This code is licensed under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * ==Overview==
 *
 * @author ypriverol on 17/10/2018.
 */
@Slf4j
public class MapClusterStorageTest {

    private static final int MAX_READING_VALUE = 200_000;
    IRawSpectrumFunction loadingFilter;
    private static long NUMBER_CLUSTERS = 1_000_000;

    private List<ICluster> spectra = new ArrayList<>(30);
     private ICluster[] clusters;


    @Before
    public void setUp() throws Exception {

        loadingFilter = new RemoveImpossiblyHighPeaksFunction()
                .specAndThen(new RemovePrecursorPeaksFunction(0.5))
                .specAndThen(new RawPeaksWrapperFunction(new KeepNHighestRawPeaks(40)));

        IClusteringEngine engine = new GreedyClusteringEngine(BasicIntegerNormalizer.MZ_CONSTANT,
                1, 0.99f, 5, new CombinedFisherIntensityTest(),
                new MinNumberComparisonsAssessor(10000), new ShareHighestPeaksClusterPredicate(5),
                GreedyConsensusSpectrum.NOISE_FILTER_INCREMENT);


        File mgfFile = new File(MapClusterStorageTest.class.getClassLoader().getResource("same_sequence_cluster.mgf").toURI());
        MzSpectraReader reader = new MzSpectraReader(mgfFile,
                new TideBinner(),
                new MaxPeakNormalizer(),
                new BasicIntegerNormalizer(),
                new HighestPeakPerBinFunction(),
                loadingFilter,
                GreedyClusteringEngine.COMPARISON_FILTER, engine);
        Iterator<ICluster> iterator = reader.readClusterIterator();

        while (iterator.hasNext()) {
            ICluster s = iterator.next();
            spectra.add(s);
        }

        // sort the spectra
        spectra.sort(Comparator.comparingInt(ICluster::getPrecursorMz));
        clusters = engine.clusterSpectra(spectra.toArray(new ICluster[spectra.size()]));
    }

    @Test
    public void storeBigClusterStatic() throws IOException, SpectraClusterException, PgatkIOException {

        long time = System.currentTimeMillis();
        Random random = new Random();

        ClusterStorageFactory factory = new ClusterStorageFactory();
        IMapStorage<ICluster> clusterStorage = factory.buildStaticStorage(Files.createTempDirectory("clusters-").toFile(), NUMBER_CLUSTERS);


        for(int i = 0; i < NUMBER_CLUSTERS; i++){
            int index = random.nextInt(clusters.length);
            ICluster cluster = clusters[index];
            clusterStorage.put(cluster.getId() + String.valueOf(i), cluster);
        }

        Assert.assertEquals(NUMBER_CLUSTERS, clusterStorage.storageSize());
        System.out.println("HashMap: Writing 1M Clusters -- " + (System.currentTimeMillis() - time) / 1000);

        time = System.currentTimeMillis();
        IntStream.range(0, MAX_READING_VALUE).forEach(x -> {
            try {
                int key = random.nextInt(clusters.length);
                ICluster value = clusterStorage.get(clusters[key].getId() + String.valueOf(x));
            }catch (PgatkIOException ex){
                log.error("Error reading entry -- " + x);
            }
        });

        System.out.println("ChronicleMap: Reading 200'000 Clusters -- " + (System.currentTimeMillis() - time) / 1000);

        time = System.currentTimeMillis();
        clusterStorage.close();
        System.out.println("ChronicleMap: Deleting Temporary Cache -- " + (System.currentTimeMillis() - time) / 1000);

    }


    @Test
    public void storeBigClusterDynamic() throws IOException, SpectraClusterException, PgatkIOException {

        long time = System.currentTimeMillis();
        Random random = new Random();

        ClusterStorageFactory factory = new ClusterStorageFactory();
        IMapStorage<ICluster> clusterStorage = factory.buildDynamicStorage(Files.createTempDirectory("clusters-").toFile(), GreedySpectralCluster.class);


        for(int i = 0; i < NUMBER_CLUSTERS; i++){
            ICluster cluster = clusters[0];
            clusterStorage.put(cluster.getId() + "-" +String.valueOf(i), cluster);
        }

        ((SparkKeyClusterStorage) clusterStorage).flush();

        Assert.assertEquals(NUMBER_CLUSTERS, clusterStorage.storageSize());
        System.out.println("Sparkey: Writing 1M Clusters -- " + (System.currentTimeMillis() - time) / 1000);

        time = System.currentTimeMillis();
        IntStream.range(0, MAX_READING_VALUE).forEach(x -> {
            try {
                ICluster value = clusterStorage.get(clusters[0].getId() +  "-" + String.valueOf(x));
            }catch (PgatkIOException ex){
                log.error("Error reading entry -- " + x);
            }
        });

        System.out.println("Sparkey: Reading 200'000 Clusters -- " + (System.currentTimeMillis() - time) / 1000);

        time = System.currentTimeMillis();
        clusterStorage.close();
        System.out.println("Sparkey: Deleting Temporary Cache -- " + (System.currentTimeMillis() - time) / 1000);

    }

}