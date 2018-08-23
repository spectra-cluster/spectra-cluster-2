package org.spectra.cluster.cdf;

import org.junit.Assert;
import org.junit.Test;

public class CumulativeDistributionFunctionTest {
    @Test
    public void testCdf() {
        double[] relPeptidesBelowScore = {0.2857142857142857,
                0.7142857142857143,
                0.7142857142857143,
                0.7142857142857143,
                0.7142857142857143,
                0.7142857142857143,
                0.7142857142857143,
                0.7142857142857143,
                0.7142857142857143,
                0.7142857142857143,
                0.7142857142857143,
                0.7142857142857143,
                0.7142857142857143,
                0.7142857142857143,
                0.7142857142857143,
                0.7142857142857143,
                0.7142857142857143,
                0.7142857142857143,
                0.7142857142857143,
                0.7142857142857143,
                0.8571428571428571,
                1.0
        };

        CumulativeDistributionFunction cdf = new CumulativeDistributionFunction(14L, 0.1, relPeptidesBelowScore);

        Assert.assertEquals(1.0, cdf.getCdfForThreshold(2.2), 0.0000001);
        Assert.assertEquals(0.857, cdf.getCdfForThreshold(2.0), 0.001);
        Assert.assertEquals(0, cdf.probability(2.2, 1), 0.0001);
        Assert.assertEquals(0.714, cdf.probability(0, 1), 0.001);

        Assert.assertTrue(cdf.isSaveMatch(3.0, 1, 1));
        Assert.assertTrue(cdf.isSaveMatch(2.0, 1, 0.9));
        Assert.assertFalse(cdf.isSaveMatch(1.0, 10, 0.9));
    }

    @Test
    public void testFromString() throws Exception {
        String cdfString = "max_score\tlower_diff_matches\tcum_lower_diff_matches\trel_cum_lower_matches\ttotal_matches\n" +
                "0.10\t4\t4\t0.2857142857142857\t14\n" +
                "0.20\t6\t10\t0.7142857142857143\t14\n" +
                "0.30\t0\t10\t0.7142857142857143\t14\n" +
                "0.40\t0\t10\t0.7142857142857143\t14\n" +
                "0.50\t0\t10\t0.7142857142857143\t14\n" +
                "0.60\t0\t10\t0.7142857142857143\t14\n" +
                "0.70\t0\t10\t0.7142857142857143\t14\n" +
                "0.80\t0\t10\t0.7142857142857143\t14\n" +
                "0.90\t0\t10\t0.7142857142857143\t14\n" +
                "1.00\t0\t10\t0.7142857142857143\t14\n" +
                "1.10\t0\t10\t0.7142857142857143\t14\n" +
                "1.20\t0\t10\t0.7142857142857143\t14\n" +
                "1.30\t0\t10\t0.7142857142857143\t14\n" +
                "1.40\t0\t10\t0.7142857142857143\t14\n" +
                "1.50\t0\t10\t0.7142857142857143\t14\n" +
                "1.60\t0\t10\t0.7142857142857143\t14\n" +
                "1.70\t0\t10\t0.7142857142857143\t14\n" +
                "1.80\t0\t10\t0.7142857142857143\t14\n" +
                "1.90\t0\t10\t0.7142857142857143\t14\n" +
                "2.00\t0\t10\t0.7142857142857143\t14\n" +
                "2.10\t2\t12\t0.8571428571428571\t14\n" +
                "2.20\t2\t14\t1.0\t14\n";

        CumulativeDistributionFunction cdf = CumulativeDistributionFunction.fromString(cdfString);

        Assert.assertEquals(0.1, cdf.scoreIncrements, 0.0001);
        Assert.assertEquals(1.0, cdf.getCdfForThreshold(2.2), 0.0000001);
        Assert.assertEquals(0.857, cdf.getCdfForThreshold(2.0), 0.001);
        Assert.assertEquals(0, cdf.probability(2.2, 1), 0.0001);
        Assert.assertEquals(0.714, cdf.probability(0, 1), 0.001);
    }
}
