package org.spectra.cluster.normalizer;

import org.junit.Assert;
import org.junit.Test;

import java.util.Arrays;
import java.util.stream.Collectors;

public class BasicIntegerNormalizerTest {
    @Test
    public void testBasicNormalizer() {
        double[] doublesToConvert = {0.1552, 100.2523, 5000.125, 299.99};
        BasicIntegerNormalizer normalizer = new BasicIntegerNormalizer(BasicIntegerNormalizer.MZ_CONSTANT);

        int[] convertedValues = normalizer.binDoubles(Arrays.stream(doublesToConvert).boxed().collect(Collectors.toList()));

        Assert.assertEquals(4, convertedValues.length);
        Assert.assertEquals(1552, convertedValues[0]);
        Assert.assertEquals(1002523, convertedValues[1]);
        Assert.assertEquals(50001250, convertedValues[2]);
        Assert.assertEquals(2999900, convertedValues[3]);

    }
}
