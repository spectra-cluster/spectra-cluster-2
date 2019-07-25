package org.spectra.cluster.io.cluster;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.spectra.cluster.cdf.MinNumberComparisonsAssessor;
import org.spectra.cluster.engine.GreedyClusteringEngine;
import org.spectra.cluster.engine.IClusteringEngine;
import org.spectra.cluster.filter.binaryspectrum.HighestPeakPerBinFunction;
import org.spectra.cluster.filter.rawpeaks.*;
import org.spectra.cluster.io.spectra.MzSpectraReader;
import org.spectra.cluster.model.cluster.ICluster;
import org.spectra.cluster.model.consensus.GreedyConsensusSpectrum;
import org.spectra.cluster.normalizer.BasicIntegerNormalizer;
import org.spectra.cluster.normalizer.MaxPeakNormalizer;
import org.spectra.cluster.normalizer.TideBinner;
import org.spectra.cluster.predicates.ShareHighestPeaksClusterPredicate;
import org.spectra.cluster.similarity.CombinedFisherIntensityTest;

import java.io.File;
import java.util.*;

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
public class BinaryClusterStorageTest {
    IRawSpectrumFunction loadingFilter;

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


        File mgfFile = new File(BinaryClusterStorageTest.class.getClassLoader().getResource("same_sequence_cluster.mgf").toURI());
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

    @Ignore
    @Test
    public void storeBigClusterStatic() {

        long time = System.currentTimeMillis();

        ClusterStorageFactory factory = new ClusterStorageFactory();
        Optional<IClusterStorage> clusterStorageOptional = factory.buildStaticStorage();
        IClusterStorage clusterStorage = clusterStorageOptional.get();


        // Store one millions of spectra
        for(int i = 0; i < 100000; i++){
            int finalI = i;
            Arrays.asList(clusters).forEach(cluster -> clusterStorage.storeCluster(cluster.getId() + String.valueOf(finalI), cluster));
        }

        Assert.assertEquals(800000, clusterStorage.size());

        // Retrieve all the spectra
        for(int i = 0; i < 100000; i++){
            int finalI = i;
            Arrays.asList(clusters).forEach(cluster -> clusterStorage.getCluster(cluster.getId() + String.valueOf(finalI)).get().getId());
        }

        System.out.println((System.currentTimeMillis() - time)/1000);
    }

    @Ignore
    @Test
    public void storeBigClusterDynamic() {

        long time = System.currentTimeMillis();

        ClusterStorageFactory factory = new ClusterStorageFactory();
        Optional<IClusterStorage> clusterStorageOptional = factory.buildDynamicStorage();
        IClusterStorage clusterStorage = clusterStorageOptional.get();


        // Store one millions of spectra
        for(int i = 0; i < 100000; i++){
            int finalI = i;
            Arrays.stream(clusters).forEach(cluster -> clusterStorage.storeCluster(cluster.getId() + String.valueOf(finalI), cluster));
        }

        Assert.assertEquals(800000, clusterStorage.size());

        // Retrieve all the spectra
        for(int i = 0; i < 100000; i++){
            int finalI = i;
            Arrays.stream(clusters).forEach(cluster -> clusterStorage.getCluster(cluster.getId() + String.valueOf(finalI)).get().getId());
        }

        System.out.println((System.currentTimeMillis() - time)/1000);
    }

    @Ignore
    @Test
    public void deleteBigClustersDynamic() {

        long time = System.currentTimeMillis();

        ClusterStorageFactory factory = new ClusterStorageFactory();
        Optional<IClusterStorage> clusterStorageOptional = factory.buildDynamicStorage();
        IClusterStorage clusterStorage = clusterStorageOptional.get();


        // Store one millions of spectra
        for(int i = 0; i < 10000; i++){
            int finalI = i;
            Arrays.stream(clusters).forEach(cluster -> clusterStorage.storeCluster(cluster.getId() + String.valueOf(finalI), cluster));
        }

        Assert.assertEquals(80000, clusterStorage.size());

        // Retrieve all the spectra
        for(int i = 0; i < 10000; i++){
            int finalI = i;
            Arrays.stream(clusters).forEach(cluster -> clusterStorage.getCluster(cluster.getId() + String.valueOf(finalI)).get().getId());

            Arrays.stream(clusters).forEach(cluster -> clusterStorage.deleteCluster(cluster.getId() + String.valueOf(finalI)));

        }
        Assert.assertEquals(0, clusterStorage.size());

        System.out.println((System.currentTimeMillis() - time)/1000);
    }


    @Test
    public void storeClusterStatic() {

        long time = System.currentTimeMillis();

        ClusterStorageFactory factory = new ClusterStorageFactory();
        Optional<IClusterStorage> clusterStorageOptional = factory.buildStaticStorage(800);
        IClusterStorage clusterStorage = clusterStorageOptional.get();


        // Store one millions of spectra
        for(int i = 0; i < 10; i++){
            int finalI = i;
            Arrays.asList(clusters).forEach(cluster -> clusterStorage.storeCluster(cluster.getId() + String.valueOf(finalI), cluster));
        }

        Assert.assertEquals(80, clusterStorage.size());

        // Retrieve all the spectra
        for(int i = 0; i < 10; i++){
            int finalI = i;
            Arrays.asList(clusters).forEach(cluster -> clusterStorage.getCluster(cluster.getId() + String.valueOf(finalI)).get().getId());
        }

        System.out.println((System.currentTimeMillis() - time)/1000);
    }

    @Test
    public void storeClusterDynamic() {

        long time = System.currentTimeMillis();

        ClusterStorageFactory factory = new ClusterStorageFactory();
        Optional<IClusterStorage> clusterStorageOptional = factory.buildDynamicStorage();
        IClusterStorage clusterStorage = clusterStorageOptional.get();


        // Store one millions of spectra
        for(int i = 0; i < 10; i++){
            int finalI = i;
            Arrays.stream(clusters).forEach(cluster -> clusterStorage.storeCluster(cluster.getId() + String.valueOf(finalI), cluster));
        }

        Assert.assertEquals(80, clusterStorage.size());

        // Retrieve all the spectra
        for(int i = 0; i < 10; i++){
            int finalI = i;
            Arrays.stream(clusters).forEach(cluster -> clusterStorage.getCluster(cluster.getId() + String.valueOf(finalI)).get().getId());
        }

        System.out.println((System.currentTimeMillis() - time)/1000);
    }

}