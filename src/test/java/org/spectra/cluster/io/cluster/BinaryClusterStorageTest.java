package org.spectra.cluster.io.cluster;

import org.ehcache.sizeof.SizeOf;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.spectra.cluster.cdf.MinNumberComparisonsAssessor;
import org.spectra.cluster.engine.GreedyClusteringEngine;
import org.spectra.cluster.engine.GreedyClusteringEngineTest;
import org.spectra.cluster.engine.IClusteringEngine;
import org.spectra.cluster.filter.binaryspectrum.HighestPeakPerBinFunction;
import org.spectra.cluster.filter.rawpeaks.*;
import org.spectra.cluster.io.spectra.MzSpectraReader;
import org.spectra.cluster.model.cluster.ICluster;
import org.spectra.cluster.model.spectra.IBinarySpectrum;
import org.spectra.cluster.normalizer.BasicIntegerNormalizer;
import org.spectra.cluster.normalizer.MaxPeakNormalizer;
import org.spectra.cluster.normalizer.TideBinner;
import org.spectra.cluster.similarity.CombinedFisherIntensityTest;

import java.io.File;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;

import static org.junit.Assert.*;

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

    private List<IBinarySpectrum> spectra = new ArrayList<>(30);
     private ICluster[] clusters;


    @Before
    public void setUp() throws Exception {
        loadingFilter = new RemoveImpossiblyHighPeaksFunction()
                .specAndThen(new RemovePrecursorPeaksFunction(0.5))
                .specAndThen(new RawPeaksWrapperFunction(new KeepNHighestRawPeaks(40)));

        File mgfFile = new File(BinaryClusterStorageTest.class.getClassLoader().getResource("same_sequence_cluster.mgf").toURI());
        MzSpectraReader reader = new MzSpectraReader(mgfFile,
                new TideBinner(),
                new MaxPeakNormalizer(),
                new BasicIntegerNormalizer(),
                new HighestPeakPerBinFunction(),
                loadingFilter);
        Iterator<IBinarySpectrum> iterator = reader.readBinarySpectraIterator();

        while (iterator.hasNext()) {
            IBinarySpectrum s = iterator.next();
            spectra.add(s);
        }

        // sort the spectra
        spectra.sort(Comparator.comparingInt(IBinarySpectrum::getPrecursorMz));


        IClusteringEngine engine = new GreedyClusteringEngine(BasicIntegerNormalizer.MZ_CONSTANT,
                1, 0.99f, 5, new CombinedFisherIntensityTest(),
                new MinNumberComparisonsAssessor(10000), 5);

        clusters = engine.clusterSpectra(spectra.toArray(new IBinarySpectrum[0]));
    }

    @Test
    public void storeClusterStatic() {

        long time = System.currentTimeMillis();

        ClusterStorageFactory factory = new ClusterStorageFactory();
        Optional<IClusterStorage> clusterStorageOptional = factory.buildStaticStorage();
        IClusterStorage clusterStorage = clusterStorageOptional.get();


        // Store one millions of spectra
        for(int i = 0; i < 100000; i++){
            int finalI = i;
            Arrays.asList(clusters).forEach(cluster -> {
                clusterStorage.storeCluster(cluster.getId() + String.valueOf(finalI), cluster);
            });
        }

        Assert.assertEquals(800000, clusterStorage.size());

        // Retrieve all the spectra
        for(int i = 0; i < 100000; i++){
            int finalI = i;
            Arrays.asList(clusters).forEach(cluster -> {
                clusterStorage.getCluster(cluster.getId() + String.valueOf(finalI)).get().getId();
            });
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
        for(int i = 0; i < 100000; i++){
            int finalI = i;
            Arrays.stream(clusters).forEach(cluster -> {
                clusterStorage.storeCluster(cluster.getId() + String.valueOf(finalI), cluster);
            });
        }

        Assert.assertEquals(800000, clusterStorage.size());

        // Retrieve all the spectra
        for(int i = 0; i < 100000; i++){
            int finalI = i;
            Arrays.stream(clusters).forEach(cluster -> {
                clusterStorage.getCluster(cluster.getId() + String.valueOf(finalI)).get().getId();
            });
        }

        System.out.println((System.currentTimeMillis() - time)/1000);
    }

    @Test
    public void deleteClustersDynamic() {

        long time = System.currentTimeMillis();

        ClusterStorageFactory factory = new ClusterStorageFactory();
        Optional<IClusterStorage> clusterStorageOptional = factory.buildDynamicStorage();
        IClusterStorage clusterStorage = clusterStorageOptional.get();


        // Store one millions of spectra
        for(int i = 0; i < 10000; i++){
            int finalI = i;
            Arrays.stream(clusters).forEach(cluster -> {
                clusterStorage.storeCluster(cluster.getId() + String.valueOf(finalI), cluster);
            });
        }

        Assert.assertEquals(80000, clusterStorage.size());

        // Retrieve all the spectra
        for(int i = 0; i < 10000; i++){
            int finalI = i;
            Arrays.stream(clusters).forEach(cluster -> {
                clusterStorage.getCluster(cluster.getId() + String.valueOf(finalI)).get().getId();
            });

            Arrays.stream(clusters).forEach(cluster -> {
                clusterStorage.deleteCluster(cluster.getId() + String.valueOf(finalI));
            });

        }
        Assert.assertTrue(clusterStorage.size() == 0);

        System.out.println((System.currentTimeMillis() - time)/1000);
    }

}