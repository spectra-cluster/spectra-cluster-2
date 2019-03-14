package org.spectra.cluster.predicates;

import org.junit.Assert;
import org.junit.Test;
import org.spectra.cluster.engine.GreedyClusteringEngine;
import org.spectra.cluster.model.cluster.GreedySpectralCluster;
import org.spectra.cluster.model.cluster.ICluster;
import org.spectra.cluster.model.consensus.GreedyConsensusSpectrum;
import org.spectra.cluster.model.spectra.BinaryPeak;
import org.spectra.cluster.model.spectra.BinarySpectrum;

public class ShareNComparisonPeaksPredicateTest {
    @Test
    public void testManualSpectra() {
        BinaryPeak[] peakList = {
           new BinaryPeak(10, 10),
           new BinaryPeak(20, 10),
           new BinaryPeak(30, 10)
        };

        BinaryPeak[] peaklist2 = {
            new BinaryPeak(10, 10),
            new BinaryPeak(20, 10),
            new BinaryPeak(30, 10)
        };

        BinaryPeak[] peaklist3 = {
            new BinaryPeak(50, 10),
            new BinaryPeak(60, 10),
            new BinaryPeak(70, 10)
        };

        ICluster c1 = clusterForPeaklist(peakList);
        ICluster c2 = clusterForPeaklist(peaklist2);
        ICluster c3 = clusterForPeaklist(peaklist3);

        ShareNComparisonPeaksPredicate predicate = new ShareNComparisonPeaksPredicate(5);
        Assert.assertFalse(predicate.test(c1, c2));
        Assert.assertFalse(predicate.test(c2, c3));

        predicate = new ShareNComparisonPeaksPredicate(3);
        Assert.assertTrue(predicate.test(c1, c2));
        Assert.assertFalse(predicate.test(c2, c3));
    }

    private ICluster clusterForPeaklist(BinaryPeak[] peaklist) {
        BinarySpectrum s1 = new BinarySpectrum("test1", 100, 1, peaklist, GreedyClusteringEngine.COMPARISON_FILTER);
        ICluster c = new GreedySpectralCluster(new GreedyConsensusSpectrum(GreedyClusteringEngine.COMPARISON_FILTER));
        c.addSpectra(s1);

        return c;
    }
}
