package org.spectra.cluster.io.properties;

import lombok.extern.slf4j.Slf4j;
import net.openhft.chronicle.map.ChronicleMapBuilder;

import java.io.File;
import java.io.IOException;

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
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * ==Overview==
 *
 * @author ypriverol on 14/10/2018.
 */
@Slf4j
public class MapDBPropertyStorage extends InMemoryPropertyStorage{

    public static File dbFile = null;

    public static final long MAX_NUMBER_FEATURES = 100000000;

    public MapDBPropertyStorage() throws IOException {
        log.info("----- CHRONICLE MAP ------------------------");
        dbFile = File.createTempFile("properties-" + System.nanoTime(), ".db");
        dbFile.deleteOnExit();
        this.propertyStorage =
                    ChronicleMapBuilder.of(String.class, String.class)
                            .entries(MAX_NUMBER_FEATURES) //the maximum number of entries for the map
                            .averageKeySize(64)
                            .averageValueSize(54)
                            .createPersistedTo(dbFile);
    }


    @Override
    public void storeProperty(String itemId, String propertyName, String propertyValue) {
        propertyNames.add(propertyName);
        propertyStorage.put(getStorageKey(itemId, propertyName), propertyValue);
    }

    /**
     * Close the DB on Disk and delete it.
     */
    public void close(){
        if(dbFile != null && dbFile.exists()){
            dbFile.deleteOnExit();
        }

    }

}
