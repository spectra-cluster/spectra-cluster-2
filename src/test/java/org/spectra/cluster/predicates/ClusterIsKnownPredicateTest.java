package org.spectra.cluster.predicates;

import org.junit.Assert;
import org.junit.Test;
import org.spectra.cluster.engine.GreedyClusteringEngine;
import org.spectra.cluster.model.cluster.GreedySpectralCluster;
import org.spectra.cluster.model.consensus.GreedyClusteringConsensusSpectrum;

public class ClusterIsKnownPredicateTest {
    @Test
    public void testKnownPredicate() {
        GreedySpectralCluster cluster1 = new GreedySpectralCluster(new GreedyClusteringConsensusSpectrum(GreedyClusteringEngine.COMPARISON_FILTER));
        GreedySpectralCluster cluster2 = new GreedySpectralCluster(new GreedyClusteringConsensusSpectrum(GreedyClusteringEngine.COMPARISON_FILTER));

        ClusterIsKnownComparisonPredicate predicate = new ClusterIsKnownComparisonPredicate();

        Assert.assertFalse(predicate.test(cluster1, cluster2));
        cluster1.saveComparisonResult(cluster2.getId(), 1);
        Assert.assertTrue(predicate.test(cluster1, cluster2));
        Assert.assertTrue(predicate.test(cluster2, cluster1));
    }
}
