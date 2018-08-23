package org.spectra.cluster.cdf;

import org.junit.Assert;
import org.junit.Test;

public class CdfResultTest {
    @Test
    public void testCdfResultClass() {
        CdfResult result = new CdfResult(0.1);

        double[] scores = {0, 0.1, 0.05, 0.11, 0.15, 2, 2.1};

        for (double score : scores) {
            result.saveRandomMatchResult(score);
        }

        Assert.assertEquals(7L, result.getTotalComparisons());

        String resultString = result.toString();
        Assert.assertEquals("max_score\tlower_diff_matches\tcum_lower_diff_matches\trel_cum_lower_matches\ttotal_matches\n" +
                "0.10\t2\t2\t0.2857142857142857\t7\n" +
                "0.20\t3\t5\t0.7142857142857143\t7\n" +
                "0.30\t0\t5\t0.7142857142857143\t7\n" +
                "0.40\t0\t5\t0.7142857142857143\t7\n" +
                "0.50\t0\t5\t0.7142857142857143\t7\n" +
                "0.60\t0\t5\t0.7142857142857143\t7\n" +
                "0.70\t0\t5\t0.7142857142857143\t7\n" +
                "0.80\t0\t5\t0.7142857142857143\t7\n" +
                "0.90\t0\t5\t0.7142857142857143\t7\n" +
                "1.00\t0\t5\t0.7142857142857143\t7\n" +
                "1.10\t0\t5\t0.7142857142857143\t7\n" +
                "1.20\t0\t5\t0.7142857142857143\t7\n" +
                "1.30\t0\t5\t0.7142857142857143\t7\n" +
                "1.40\t0\t5\t0.7142857142857143\t7\n" +
                "1.50\t0\t5\t0.7142857142857143\t7\n" +
                "1.60\t0\t5\t0.7142857142857143\t7\n" +
                "1.70\t0\t5\t0.7142857142857143\t7\n" +
                "1.80\t0\t5\t0.7142857142857143\t7\n" +
                "1.90\t0\t5\t0.7142857142857143\t7\n" +
                "2.00\t0\t5\t0.7142857142857143\t7\n" +
                "2.10\t1\t6\t0.8571428571428571\t7\n" +
                "2.20\t1\t7\t1.0\t7\n", resultString);
    }
    
    @Test
    public void testAddCdfResult() throws Exception {
        CdfResult result = new CdfResult(0.1);
        CdfResult secondResult = new CdfResult(0.1);
        
        double[] scores = {0, 0.1, 0.05, 0.11, 0.15, 2, 2.1};

        for (double score : scores) {
            result.saveRandomMatchResult(score);
            secondResult.saveRandomMatchResult(score);
        }

        result.addCdfResult(secondResult);

        Assert.assertEquals(14L, result.getTotalComparisons());

        String resultString = result.toString();
        Assert.assertEquals("max_score\tlower_diff_matches\tcum_lower_diff_matches\trel_cum_lower_matches\ttotal_matches\n" +
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
                "2.20\t2\t14\t1.0\t14\n", resultString);
    }
}
