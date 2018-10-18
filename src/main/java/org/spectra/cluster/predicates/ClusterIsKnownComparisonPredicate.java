package org.spectra.cluster.predicates;

import org.spectra.cluster.model.cluster.ICluster;

/**
 * This predicate checks whether these two clusters are
 * within the top recent comparisons.
 */
public class ClusterIsKnownComparisonPredicate implements IComparisonPredicate<ICluster> {
    @Override
    public boolean test(ICluster o1, ICluster o2) {
        if (o1.isKnownComparisonMatch(o2.getId())) {
            return true;
        }
        return o2.isKnownComparisonMatch(o1.getId());

    }
}
