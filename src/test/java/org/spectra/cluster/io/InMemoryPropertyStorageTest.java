package org.spectra.cluster.io;

import org.junit.Assert;
import org.junit.Test;
import org.spectra.cluster.io.properties.IPropertyStorage;
import org.spectra.cluster.io.properties.InMemoryPropertyStorage;

import java.util.Random;

public class InMemoryPropertyStorageTest {
    @Test
    public void testStorage() {
        IPropertyStorage storage = new InMemoryPropertyStorage();

        String spec1 = "12345";
        String spec2 = "43232";

        storage.storeProperty(spec1, "RT", "1234");
        storage.storeProperty(spec2, "RT", "1234");

        Assert.assertEquals("1234", storage.getProperty(spec1, "RT"));
        Assert.assertEquals("1234", storage.getProperty(spec2, "RT"));
        Assert.assertNull(storage.getProperty(spec1, "Hallo"));

        Assert.assertEquals(1, storage.getAvailableProperties().size());
        Assert.assertTrue(storage.getAvailableProperties().contains("RT"));
    }


    @Test
    public void performanceStorageTest(){

        long time = System.currentTimeMillis();
        IPropertyStorage storage = new InMemoryPropertyStorage();

        Random random = new Random();

        for(int i = 0; i < 100000; i++){
            storage.storeProperty(String.valueOf(i), "RT", String.valueOf(Math.random()));
        }
        Assert.assertEquals(1, storage.getAvailableProperties().size());
        Assert.assertEquals(100000, storage.storageSize());

        for( int i = 0; i < 40; i++){
            System.out.println(storage.getProperty(String.valueOf(random.nextInt((100000) + 1)),"RT"));
        }

        System.out.println((System.currentTimeMillis() - time) / 1000);

    }
}
