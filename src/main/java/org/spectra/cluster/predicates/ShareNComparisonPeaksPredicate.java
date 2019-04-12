package org.spectra.cluster.predicates;

import org.spectra.cluster.model.cluster.ICluster;
import org.spectra.cluster.model.spectra.BinaryPeak;

import java.util.Map;

/**
 * Assesses whether two cluster share at least N spectra of
 * their comparison (ie. filtered ConsensusSpectrum) peaks
 */
public class ShareNComparisonPeaksPredicate implements IComparisonPredicate<ICluster> {
    private final int minSharedPeaks;

    public ShareNComparisonPeaksPredicate(int minSharedPeaks) {
        this.minSharedPeaks = minSharedPeaks;
    }

    @Override
    public boolean test(ICluster o1, ICluster o2) {
        int nShared = 0;
        Map<BinaryPeak, BinaryPeak> peaks1 = o1.getConsensusSpectrum().getComparisonFilteredPeaks();
        for(BinaryPeak p : o2.getConsensusSpectrum().getComparisonFilteredPeaks().keySet()) {
            if (peaks1.keySet().contains(p)) {
                nShared++;
            }

            if (nShared >= minSharedPeaks) {
                return true;
            }
        }

        return nShared >= minSharedPeaks;
    }
}
