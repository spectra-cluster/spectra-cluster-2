package org.spectra.cluster.cdf;

import org.junit.Assert;
import org.junit.Test;

public class MinNumberComparisonAssessorTest {
    @Test
    public void testGetNumberOfComparisons() {
        MinNumberComparisonsAssessor assessor = new MinNumberComparisonsAssessor(100);

        Assert.assertEquals(100, assessor.getNumberOfComparisons(1, 10));
        Assert.assertEquals(200, assessor.getNumberOfComparisons(1, 200));

        Assert.assertEquals(100, assessor.getMinNumberComparisons());
    }
}
