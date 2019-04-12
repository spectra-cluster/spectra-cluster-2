package org.spectra.cluster.filter.rawpeaks;

import org.junit.Assert;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

public class KeepNHighestRawPeaksFunctionTest {
    @Test
    public void testKeepHighestPeaks() {
        Map<Double, Double> peaks = new HashMap<>();
        peaks.put(1.0, 100.0);
        peaks.put(2.0, 10.0);
        peaks.put(10.0, 100.0);
        peaks.put(15.0, 50.0);
        peaks.put(100.0, 20.0);
        peaks.put(110.0, 0.01);

        KeepNHighestRawPeaks function = new KeepNHighestRawPeaks(3);
        Map<Double, Double> filtered = function.apply(peaks);

        Assert.assertEquals(3, filtered.size());
        Assert.assertTrue(filtered.containsKey(1.0));
        Assert.assertTrue(filtered.containsKey(15.0));
        Assert.assertFalse(filtered.containsKey(100.0));
    }
}
