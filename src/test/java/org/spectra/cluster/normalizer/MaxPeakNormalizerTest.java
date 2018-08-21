package org.spectra.cluster.normalizer;

import org.junit.Assert;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.stream.Collectors;

public class MaxPeakNormalizerTest {
    @Test
    public void testNormalizer() {
        double[] intensitiesToNormalize = {15.1254, 1025.241, 12035.123, 1203.4, 123352.92};

        IIntegerNormalizer normalizer = new MaxPeakNormalizer();

        int[] normalizedValues = normalizer.binDoubles(Arrays.stream(intensitiesToNormalize).boxed().collect(Collectors.toCollection(ArrayList::new)));

        Assert.assertEquals(intensitiesToNormalize.length, normalizedValues.length);

        Assert.assertEquals(MaxPeakNormalizer.MAX_INTENSITY, normalizedValues[4]);
        Assert.assertEquals(12, normalizedValues[0]);
        Assert.assertEquals(831, normalizedValues[1]);
        Assert.assertEquals(9757, normalizedValues[2]);
    }
}
