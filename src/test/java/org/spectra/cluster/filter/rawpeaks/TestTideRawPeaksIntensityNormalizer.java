package org.spectra.cluster.filter.rawpeaks;

import org.junit.Assert;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

public class TestTideRawPeaksIntensityNormalizer {
    @Test
    public void testNormalizer() throws Exception {
        Map<Double, Double> peaks = new HashMap<>();
        peaks.put(1.0, 100.0);
        peaks.put(2.0, 10.0);
        peaks.put(10.0, 100.0);
        peaks.put(15.0, 50.0);
        peaks.put(100.0, 20.0);
        peaks.put(110.0, 0.01);

        IRawPeakFunction function = new TideRawPeaksIntensityNormalizer();
        Map<Double, Double> normPeaks = function.apply(peaks);

        Assert.assertEquals(5, normPeaks.size());
        Assert.assertEquals(50.0, normPeaks.get(1.0), 0.001);
        Assert.assertEquals(50.0, normPeaks.get(10.0), 0.001);
        Assert.assertEquals(50.0, normPeaks.get(15.0), 0.001);
        Assert.assertEquals(50.0, normPeaks.get(100.0), 0.001);
    }
}
