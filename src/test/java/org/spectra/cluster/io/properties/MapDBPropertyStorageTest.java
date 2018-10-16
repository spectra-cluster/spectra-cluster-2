package org.spectra.cluster.io.properties;

import org.junit.Assert;
import org.junit.Test;

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
    public void storeProperty() throws IOException {

        long time = System.currentTimeMillis();
        MapDBPropertyStorage storage = new MapDBPropertyStorage();

        Random random = new Random();

        for(int i = 0; i < 10000000; i++){
            storage.storeProperty(String.valueOf(i), "RT", String.valueOf(Math.random()));
        }
        Assert.assertEquals(1, storage.getAvailableProperties().size());
        Assert.assertEquals(10000000, storage.storageSize());

        for( int i = 0; i < 40; i++){
            System.out.println(storage.getProperty(String.valueOf(random.nextInt((10000000) + 1)),"RT"));
        }

        System.out.println((System.currentTimeMillis() - time) / 1000);

        storage.close();
    }

}