package org.spectra.cluster.cdf;

import org.junit.Assert;
import org.junit.Test;
import org.spectra.cluster.model.cluster.GreedySpectralCluster;
import org.spectra.cluster.model.cluster.ICluster;
import org.spectra.cluster.model.consensus.GreedyConsensusSpectrum;

public class MinNumberComparisonAssessorTest {
    @Test
    public void testGetNumberOfComparisons() {
        INumberOfComparisonAssessor assessor = new MinNumberComparisonsAssessor(100);

        Assert.assertEquals(100, assessor.getNumberOfComparisons(1, 10));
        Assert.assertEquals(200, assessor.getNumberOfComparisons(1, 200));

        Assert.assertEquals(100, ((MinNumberComparisonsAssessor) assessor).getMinNumberComparisons());
    }
}
