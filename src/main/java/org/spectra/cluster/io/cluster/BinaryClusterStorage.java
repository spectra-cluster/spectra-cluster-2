package org.spectra.cluster.io.cluster;

import lombok.extern.slf4j.Slf4j;
import net.openhft.chronicle.map.ChronicleMapBuilder;
import org.iq80.leveldb.CompressionType;
import org.iq80.leveldb.DB;
import org.iq80.leveldb.Options;
import org.iq80.leveldb.impl.Iq80DBFactory;
import org.spectra.cluster.model.cluster.GreedySpectralCluster;
import org.spectra.cluster.model.cluster.ICluster;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;
import java.util.concurrent.ConcurrentMap;

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
public class BinaryClusterStorage implements IClusterStorage {

    public static File dbFile = null;

    public static final long MAX_NUMBER_FEATURES = 1000000;

    private ConcurrentMap<String, ICluster> clusters;

    private DB levelDB = null;
    private int levelDBSize = 0;

    public boolean dynamic = false;


    public BinaryClusterStorage(boolean dynamic, long numberClusters, String fileName) throws IOException {

        if(fileName == null){
           fileName = "clustering-bin-" + System.nanoTime();
        }
        if(numberClusters == -1)
            numberClusters = MAX_NUMBER_FEATURES;

        // Create the file that will store the persistence database
        this.dynamic = dynamic;

        if(dynamic){
            log.info("----- LEVELDB MAP ------------------------");
            Path tempDirWithPrefix = Files.createTempDirectory(fileName);
            dbFile = tempDirWithPrefix.toFile();
            Options options = new Options();
            options.cacheSize(100 * 1048576); // 100MB cache
            options.createIfMissing(true);
            options.compressionType(CompressionType.SNAPPY);
            levelDB = Iq80DBFactory.factory.open(dbFile, options);

        }else{
            log.info("----- CHRONICLE MAP ------------------------");
            dbFile = File.createTempFile("clustering-bin-" + System.nanoTime(), ".db");
            dbFile.deleteOnExit();
            clusters =
                    ChronicleMapBuilder.of(String.class, ICluster.class)
                            .entries(numberClusters) //the maximum number of entries for the map
                            .averageKeySize(36 + (100 *2))
                            .averageValueSize(6000 + (200*10))
                            .createPersistedTo(dbFile);
        }

    }

    /**
     * BinaryClusterStorage Static with a predefined number of Clusters
     * @param numberClusters number of Clusters
     * @throws IOException
     */
    public BinaryClusterStorage(int numberClusters) throws IOException {
        log.info("----- CHRONICLE MAP ------------------------");
        dbFile = File.createTempFile("clustering-bin-" + System.nanoTime(), ".db");
        dbFile.deleteOnExit();
        clusters =
                ChronicleMapBuilder.of(String.class, ICluster.class)
                        .entries(numberClusters) //the maximum number of entries for the map
                        .averageKeySize(36 + (100 *2))
                        .averageValueSize(4000)  //Todo: Compute this value
                        .createPersistedTo(dbFile);
    }

    @Override
    public void storeCluster(String key, ICluster cluster) {
        if(dynamic){
            try {
                levelDB.put(Iq80DBFactory.bytes(key), cluster.toBytes());
                levelDBSize++;
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        else
            clusters.put(key, cluster);

    }

    @Override
    public Optional<ICluster> getCluster(String key) {
        ICluster value = null;
        if(dynamic) {
            try {
                value = GreedySpectralCluster.fromBytes(levelDB.get(Iq80DBFactory.bytes(key)));
            } catch (IOException | ClassNotFoundException e) {
                e.printStackTrace();
            }
        }
        else
            value = clusters.get(key);
        if(value == null)
            return Optional.empty();

        return Optional.of(value);
    }

    @Override
    public boolean deleteCluster(String key) {
        if(dynamic)
            levelDB.delete(Iq80DBFactory.bytes(key));
        else
            clusters.remove(key);
        return true;
    }

    @Override
    public int size() {
        if(dynamic)
            return levelDBSize;
        return clusters.size();
    }

    /**
     * Close the DB on Disk and delete it.
     */
    public void close() throws IOException {
        if(dynamic)
            levelDB.close();

        if(dbFile != null && dbFile.exists()){
            dbFile.deleteOnExit();
        }

    }
}
