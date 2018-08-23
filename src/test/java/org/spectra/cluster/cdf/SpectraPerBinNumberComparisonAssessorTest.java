package org.spectra.cluster.cdf;

import org.junit.Assert;
import org.junit.Test;

public class SpectraPerBinNumberComparisonAssessorTest {
    @Test
    public void testNumberAssessor() {
        SpectraPerBinNumberComparisonAssessor assessor = new SpectraPerBinNumberComparisonAssessor(10, 50, 2000);

        int[] precursorsObserved = {10, 11, 21};

        for (int precursor : precursorsObserved) {
            for (int i = 0; i < 100; i++) {
                assessor.countSpectrum(precursor);
            }
        }

        Assert.assertEquals(50, assessor.getNumberOfComparisons(9, 1));
        Assert.assertEquals(200, assessor.getNumberOfComparisons(10, 1));
        Assert.assertEquals(200, assessor.getNumberOfComparisons(11, 1));
        Assert.assertEquals(100, assessor.getNumberOfComparisons(20, 1));
        Assert.assertEquals(50, assessor.getNumberOfComparisons(30, 1));
        Assert.assertEquals(50, assessor.getNumberOfComparisons(40, 1));
    }
}
