package org.spectra.cluster.util;

import org.junit.Assert;
import org.junit.Test;

public class ClusteringParametersTest {
    @Test
    public void testDefaultParameters() {
        ClusteringParameters params = new ClusteringParameters();

        Assert.assertTrue(params.isOutputDotClustering());
        Assert.assertEquals(40, params.getNumberHigherPeaks().intValue());
        Assert.assertEquals(2, params.getNThreads());
        Assert.assertTrue(params.isFilterReportPeaks());
    }
}
