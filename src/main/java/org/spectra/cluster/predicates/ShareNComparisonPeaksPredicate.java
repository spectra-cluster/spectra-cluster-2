package org.spectra.cluster.predicates;

import org.spectra.cluster.model.cluster.ICluster;
import org.spectra.cluster.model.spectra.BinaryPeak;

import java.util.Set;

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
        Set<BinaryPeak> peaks1 = o1.getConsensusSpectrum().getComparisonFilteredPeaks();
        for(BinaryPeak p : o2.getConsensusSpectrum().getComparisonFilteredPeaks()) {
            if (peaks1.contains(p)) {
                nShared++;
            }
        }

        return nShared >= minSharedPeaks;
    }
}
