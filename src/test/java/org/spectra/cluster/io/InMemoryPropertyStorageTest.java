package org.spectra.cluster.io;

import org.junit.Assert;
import org.junit.Test;

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
    }
}
