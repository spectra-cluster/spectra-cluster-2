package org.spectra.cluster.io.properties;

import lombok.extern.slf4j.Slf4j;
import net.openhft.chronicle.map.ChronicleMapBuilder;
import org.iq80.leveldb.CompressionType;
import org.iq80.leveldb.DB;
import org.iq80.leveldb.Options;
import org.iq80.leveldb.impl.Iq80DBFactory;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * This code is licensed under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 * <p>
 *  
 *  This class can be used to store properties well millions of Spectra are under analysis.
 *  The current implementation allows to store 1 millions of properties with less than 200 MG of
 *  memory.
 *
 *  Be ware that this implementation is slower than {@link InMemoryPropertyStorage} but consume
 *  10 times less memory than the in memory tool. But it is 2 time slower than the {@link InMemoryPropertyStorage}.
 *
 *  Please Review this benchmark for different implementations of Key-Value Stores
 *  (https://github.com/lmdbjava/benchmarks/blob/master/results/20160710/README.md)
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 *
 * @author ypriverol on 14/10/2018.
 */
@Slf4j
public class MapDBPropertyStorage extends InMemoryPropertyStorage{

    public static File dbFile = null;

    public static final long MAX_NUMBER_FEATURES = 100000000;
    private DB levelDB = null;
    private int levelDBSize = 0;

    public boolean dynamic = false;


    public MapDBPropertyStorage(boolean dynamic, long numberProperties) throws IOException {

        // Create the file that will store the persistence database
        this.dynamic = dynamic;

        if(numberProperties == -1)
            numberProperties = MAX_NUMBER_FEATURES;

        if(dynamic){
            log.info("----- LEVELDB MAP ------------------------");
            Path tempDirWithPrefix = Files.createTempDirectory("properties-" + System.nanoTime());
            dbFile = tempDirWithPrefix.toFile();
            Options options = new Options();
            options.cacheSize(100 * 1048576); // 100MB cache
            options.createIfMissing(true);
            options.compressionType(CompressionType.SNAPPY);
            levelDB = Iq80DBFactory.factory.open(dbFile, options);

        }else{
            log.info("----- CHRONICLE MAP ------------------------");
            dbFile = File.createTempFile("properties-" + System.nanoTime(), ".db");
            dbFile.deleteOnExit();
            this.propertyStorage =
                    ChronicleMapBuilder.of(String.class, String.class)
                            .entries(numberProperties) //the maximum number of entries for the map
                            .averageKeySize(64)
                            .averageValueSize(54)
                            .createPersistedTo(dbFile);
        }

    }

    public MapDBPropertyStorage(int numberProperties) throws IOException {
        log.info("----- CHRONICLE MAP ------------------------");
        dbFile = File.createTempFile("properties-" + System.nanoTime(), ".db");
        dbFile.deleteOnExit();
        this.propertyStorage =
                ChronicleMapBuilder.of(String.class, String.class)
                        .entries(numberProperties) //the maximum number of entries for the map
                        .averageKeySize(64)
                        .averageValueSize(54)
                        .createPersistedTo(dbFile);
    }

    @Override
    public void storeProperty(String itemId, String propertyName, String propertyValue) {
        propertyNames.add(propertyName);
        if(dynamic){
            levelDB.put(Iq80DBFactory.bytes(getStorageKey(itemId, propertyName)), Iq80DBFactory.bytes(propertyValue));
            levelDBSize++;
        }
        else
            propertyStorage.put(getStorageKey(itemId, propertyName), propertyValue);
    }

    @Override
    public String getProperty(String itemId, String propertyName) throws IndexOutOfBoundsException {
        String value;
        if(dynamic)
            value = Iq80DBFactory.asString(levelDB.get(Iq80DBFactory.bytes(getStorageKey(itemId, propertyName))));
        else
            value = super.getProperty(itemId, propertyName);
        return value;
    }

    @Override
    public int storageSize() {
        if(dynamic)
            return levelDBSize;
        return super.storageSize();
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
