package org.spectra.cluster.normalizer;

import org.junit.Assert;
import org.junit.Test;

import java.util.Arrays;
import java.util.stream.Collectors;

public class HighResolutionMzBinnerTest {
    @Test
    public void testBinning() {
        double[] values = {0.01, 10.123, 100.1231, 1000.1231, 239.342, 2398.21};

        HighResolutionMzBinner binner = new HighResolutionMzBinner();
        int[] binned = binner.binDoubles(Arrays.stream(values).boxed().collect(Collectors.toList()));

        Assert.assertEquals(values.length, binned.length);
        Assert.assertEquals(0, binned[0]);
        Assert.assertEquals(506, binned[1]);
        Assert.assertEquals(5006, binned[2]);
        Assert.assertEquals(50006, binned[3]);
        Assert.assertEquals(11967, binned[4]);
        Assert.assertEquals(119910, binned[5]);
    }

    @Test
    public void testUnbinning() {
        double[] values = {0.01, 10.123, 100.1231, 1000.1231, 239.342, 2398.21};

        HighResolutionMzBinner binner = new HighResolutionMzBinner();
        int[] binned = binner.binDoubles(Arrays.stream(values).boxed().collect(Collectors.toList()));
        double[] unbinned = binner.unbinValues(binned);

        Assert.assertEquals(values.length, binned.length);
        for (int i = 0; i < values.length; i++) {
            Assert.assertEquals(values[i], unbinned[i], 0.02);
        }
    }
}
