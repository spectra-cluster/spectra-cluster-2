package org.spectra.cluster.consensus;

import org.junit.Assert;
import org.junit.Test;

public class TestConsensusPeak {
    @Test
    public void basicConsensusPeakTest() {
        ConsensusPeak p1 = new ConsensusPeak(1.0, 1.0);
        ConsensusPeak p2 = new ConsensusPeak(2.0, 0.5);

        p1.mergePeak(p2);

        Assert.assertEquals(1.5, p1.getMz(), 0);
        Assert.assertEquals(0.75, p1.getIntensity(), 0);
        Assert.assertEquals(2, p1.getCount());

        ConsensusPeak p3 = new ConsensusPeak(3.0, 4.0);

        p1.mergePeak(p3);

        Assert.assertEquals(2.0, p1.getMz(), 0);
        Assert.assertEquals(1.833, p1.getIntensity(), 0.001);
        Assert.assertEquals(3, p1.getCount());
    }
}
