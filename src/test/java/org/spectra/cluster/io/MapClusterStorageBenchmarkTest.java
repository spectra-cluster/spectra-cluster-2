package org.spectra.cluster.io;

import io.github.bigbio.pgatk.io.common.PgatkIOException;
import io.github.bigbio.pgatk.io.mapcache.IMapStorage;
import io.github.bigbio.pgatk.io.objectdb.LongObject;
import io.github.bigbio.pgatk.io.objectdb.ObjectsDB;
import lombok.extern.slf4j.Slf4j;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.spectra.cluster.engine.IClusteringEngine;
import org.spectra.cluster.exceptions.SpectraClusterException;
import org.spectra.cluster.io.cluster.ClusterStorageFactory;
import org.spectra.cluster.io.cluster.ObjectDBGreedyClusterStorage;
import org.spectra.cluster.io.cluster.SparkKeyClusterStorage;
import org.spectra.cluster.io.spectra.MzSpectraReader;
import org.spectra.cluster.model.cluster.GreedySpectralCluster;
import org.spectra.cluster.model.cluster.ICluster;
import org.spectra.cluster.tools.SpectraClusterToolTest;
import org.spectra.cluster.util.ClusteringParameters;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
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
public class MapClusterStorageBenchmarkTest {

    private static final int MAX_READING_VALUE = 200_000;
    private static long NUMBER_CLUSTERS = 1_000_000;
    private static ClusteringParameters clusteringParameters = new ClusteringParameters();

    private List<ICluster> spectra = new ArrayList<>(30);
     private ICluster[] clusters;


    @Before
    public void setUp() throws Exception {
        IClusteringEngine engine = clusteringParameters.createGreedyClusteringEngine();

        File mgfFile = new File(MapClusterStorageBenchmarkTest.class.getClassLoader().getResource("same_sequence_cluster.mgf").toURI());
        MzSpectraReader reader = new MzSpectraReader(clusteringParameters, mgfFile);

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
    public void storeBigClusterStatic() throws IOException, SpectraClusterException, PgatkIOException {

        long time = System.currentTimeMillis();
        Random random = new Random();

        IMapStorage<ICluster> clusterStorage = ClusterStorageFactory.buildTemporaryStaticStorage(
                Files.createTempDirectory("clusters-").toFile(), NUMBER_CLUSTERS);


        for(int i = 0; i < NUMBER_CLUSTERS; i++){
            int index = random.nextInt(clusters.length);
            ICluster cluster = clusters[index];
            clusterStorage.put(cluster.getId() + String.valueOf(i), cluster);
        }

        Assert.assertEquals(NUMBER_CLUSTERS, clusterStorage.storageSize());
        System.out.println("HashMap: Writing 1M Clusters -- " + (System.currentTimeMillis() - time) / 1000);

        time = System.currentTimeMillis();
        IntStream.range(0, MAX_READING_VALUE).forEach(x -> {
            int key = random.nextInt(clusters.length);
            try {
                ICluster value = clusterStorage.get(clusters[key].getId() + x);
            } catch (PgatkIOException e) {
                e.printStackTrace();
            }
        });

        System.out.println("ChronicleMap: Reading 200'000 Clusters -- " + (System.currentTimeMillis() - time) / 1000);

        time = System.currentTimeMillis();
        clusterStorage.close();
        System.out.println("ChronicleMap: Deleting Temporary Cache -- " + (System.currentTimeMillis() - time) / 1000);

    }

    @Ignore
    @Test
    public void storeBigClusterDynamic() throws IOException, SpectraClusterException, PgatkIOException {

        long time = System.currentTimeMillis();
        Random random = new Random();

        ClusterStorageFactory factory = new ClusterStorageFactory();
        IMapStorage<ICluster> clusterStorage = factory.buildTemporaryDynamicStorage(
                Files.createTempDirectory("clusters-").toFile(), GreedySpectralCluster.class);


        for(int i = 0; i < NUMBER_CLUSTERS; i++){
            ICluster cluster = clusters[0];
            clusterStorage.put(cluster.getId() + "-" + i, cluster);
        }

        ((SparkKeyClusterStorage) clusterStorage).flush();

        Assert.assertEquals(NUMBER_CLUSTERS, clusterStorage.storageSize());
        System.out.println("Sparkey: Writing 1M Clusters -- " + (System.currentTimeMillis() - time) / 1000);

        time = System.currentTimeMillis();
        IntStream.range(0, MAX_READING_VALUE).forEach(x -> {
            try {
                ICluster value = clusterStorage.get(clusters[0].getId() +  "-" + x);
            } catch (PgatkIOException e) {
                e.printStackTrace();
            }
        });

        System.out.println("Sparkey: Reading 200'000 Clusters -- " + (System
                .currentTimeMillis() - time) / 1000);

        time = System.currentTimeMillis();
        clusterStorage.close();
        System.out.println("Sparkey: Deleting Temporary Cache -- " + (System.currentTimeMillis() - time) / 1000);

    }

    @Test
    public void clusteringObjectDBTest() throws IOException {

        long time = System.currentTimeMillis();
        Random random = new Random();

        ObjectDBGreedyClusterStorage clusterStorage = new ObjectDBGreedyClusterStorage(new ObjectsDB(Files
                .createTempDirectory("clusters-").toFile()
                .getAbsolutePath(), "clustering-results.zcl")
        );

        for(int i = 0; i < NUMBER_CLUSTERS; i++){
            GreedySpectralCluster cluster = (GreedySpectralCluster) clusters[0];
            long longKey = LongObject.asLongHash(cluster.getObjectId() + "-" + String.valueOf(i));
            clusterStorage.addGreedySpectralCluster(longKey, cluster);
        }

        Assert.assertEquals(NUMBER_CLUSTERS, clusterStorage.getNumber(GreedySpectralCluster.class));
        System.out.println("Sparkey: Writing 1M Clusters -- " + (System.currentTimeMillis() - time) / 1000);

    }

    @Test
    public void readingClusteringObjectFile() throws IOException, URISyntaxException {

        String clusterFile = new File(SpectraClusterToolTest.class
                .getClassLoader().getResource("previous-clusters.zcl").toURI()).getAbsolutePath();
        ObjectDBGreedyClusterStorage clusterStorage = new ObjectDBGreedyClusterStorage(new ObjectsDB(clusterFile, false));
        Assert.assertEquals(0, clusterStorage.getNumber(GreedySpectralCluster.class));


    }

}