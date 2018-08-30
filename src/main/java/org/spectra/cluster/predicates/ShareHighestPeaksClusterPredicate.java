package org.spectra.cluster.predicates;

import org.spectra.cluster.model.cluster.ICluster;
import org.spectra.cluster.model.spectra.IBinarySpectrum;

/**
 * This predicate simply applies the ShareHighestPeaksPredicate to clusters.
 */
public class ShareHighestPeaksClusterPredicate implements IComparisonPredicate<ICluster> {
    private final IComparisonPredicate<IBinarySpectrum> highestPeaksPredicate;

    /**
     * Creates a new predicate to compare whether the consensus spectra of the passed
     * clusters share any of the N highest peaks.
     * @param nHighestPeaks The number of peaks to compare.
     */
    public ShareHighestPeaksClusterPredicate(int nHighestPeaks) {
        this.highestPeaksPredicate = new ShareHighestPeaksPredicate(nHighestPeaks);
    }

    @Override
    public boolean test(ICluster o1, ICluster o2) {
        return highestPeaksPredicate.test(o1.getConsensusSpectrum(), o2.getConsensusSpectrum());
    }
}
