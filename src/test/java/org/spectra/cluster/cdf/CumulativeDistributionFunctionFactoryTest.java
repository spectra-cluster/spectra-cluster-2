package org.spectra.cluster.cdf;

import org.junit.Assert;
import org.junit.Test;
import org.spectra.cluster.similarity.CombinedFisherIntensityTest;

public class CumulativeDistributionFunctionFactoryTest {
    @Test
    public void testGetCdfForCombinedScore() throws Exception {
        CumulativeDistributionFunction cdf = CumulativeDistributionFunctionFactory.getDefaultCumlativeDistributionFunctionForSimilarityMetric(CombinedFisherIntensityTest.class);

        Assert.assertNotNull(cdf);
        Assert.assertEquals(0.5, cdf.scoreIncrements, 0.00001);
        Assert.assertTrue(cdf.isSaveMatch(100, 100, 0.9));
    }
}
