package org.spectra.cluster.predicates;

import org.spectra.cluster.model.cluster.ICluster;

/**
 * Predicate to test whether two cluster's consensus spectra
 * have the same charge state.
 *
 * @author jg
 */
public class SameChargePredicate implements IComparisonPredicate<ICluster> {
    @Override
    public boolean test(ICluster o1, ICluster o2) {
        return o1.getConsensusSpectrum().getPrecursorCharge() == o2.getConsensusSpectrum().getPrecursorCharge();
    }
}
