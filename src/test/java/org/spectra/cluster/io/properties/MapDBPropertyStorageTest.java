package org.spectra.cluster.io.properties;

import org.junit.Assert;
import org.junit.Test;
import org.spectra.cluster.exceptions.SpectraClusterException;

import java.io.IOException;
import java.util.Random;

/**
 * This code is licensed under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * ==Overview==
 *
 * @author ypriverol on 14/10/2018.
 */
public class MapDBPropertyStorageTest {

    @Test
    public void storeProperty() throws IOException, SpectraClusterException {

        IPropertyStorage storage = PropertyStorageFactory.buildStaticPropertyStorage(200);

        long time = System.currentTimeMillis();
        Random random = new Random();

        for(int i = 0; i < 200; i++){
            storage.storeProperty(String.valueOf(i), "RT", String.valueOf(Math.random()));
        }
        Assert.assertEquals(1, storage.getAvailableProperties().size());
        Assert.assertEquals(200, storage.storageSize());

        for( int i = 0; i < 40; i++){
            System.out.println(storage.getProperty(String.valueOf(random.nextInt((200) + 1)),"RT"));
        }

        System.out.println((System.currentTimeMillis() - time) / 1000);

        ((MapDBPropertyStorage)storage).close();
    }


    @Test
    public void storePropertyDynamic() throws IOException, SpectraClusterException {

        long time = System.currentTimeMillis();
        IPropertyStorage storage = PropertyStorageFactory.buildDynamicPropertyStorage();
        Random random = new Random();

        for(int i = 0; i < 200; i++){
            storage.storeProperty(String.valueOf(i), "RT", String.valueOf(Math.random()));
        }
        Assert.assertEquals(1, storage.getAvailableProperties().size());
        Assert.assertEquals(200, storage.storageSize());

        for( int i = 0; i < 40; i++){
            System.out.println(storage.getProperty(String.valueOf(random.nextInt((200) + 1)),"RT"));
        }

        System.out.println((System.currentTimeMillis() - time) / 1000);

        ((MapDBPropertyStorage)storage).close();
    }

}