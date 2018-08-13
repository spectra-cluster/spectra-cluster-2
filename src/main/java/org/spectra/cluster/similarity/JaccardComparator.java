package org.spectra.cluster.similarity;

import org.apache.commons.collections.CollectionUtils;

import java.util.Collection;

public class JaccardComparator {

    /**
     * This method computes Jaccard coefficient for neighbors of a given
     * pair of Collections.
     * @param firstCollection
     * @param secondCollection
     * @return The similarity score between both collections.
     */
    public static float computeJaccard(Collection<?> firstCollection,
                                               Collection<?> secondCollection) {
        int intersection = CollectionUtils.intersection(firstCollection, secondCollection)
                .size();
        int union = CollectionUtils.union(firstCollection, secondCollection).size();

        return (float) intersection / union;
    }
}
