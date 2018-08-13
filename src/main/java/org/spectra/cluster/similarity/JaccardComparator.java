package org.spectra.cluster.similarity;

import cern.colt.matrix.impl.SparseDoubleMatrix1D;
import org.apache.commons.collections.CollectionUtils;
import org.spectra.cluster.utils.math.Matrix2DUtils;

import java.util.Collection;

public class JaccardComparator {

    /**
     * This method computes Jaccard coefficient for neighbors of a given
     * pair of Collections.
     * @param firstCollection
     * @param secondCollection
     * @return The similarity score between both collections.
     */
    public static double computeVectorJaccard(Collection<?> firstCollection,
                                             Collection<?> secondCollection) {
        int intersection = CollectionUtils.intersection(firstCollection, secondCollection)
                .size();
        int union = CollectionUtils.union(firstCollection, secondCollection).size();

        return (double) intersection / union;
    }

    /**
     * Compute the jaccard score for two sparse matrices
     * @param firstCollection first collection to compare
     * @param secondCollection second collection to compare
     * @return similarity score.
     */
    public static double computeSparseMatrixJaccard(SparseDoubleMatrix1D firstCollection, SparseDoubleMatrix1D secondCollection)
    {
        double sim = -1;
        if(firstCollection.size() != secondCollection.size())
            return sim;
        double num = 0;
        double den = 0;
        num = Matrix2DUtils.productQuick(firstCollection, secondCollection);
        den = Matrix2DUtils.getSqrSum(firstCollection) + Matrix2DUtils.getSqrSum(secondCollection);


        if((den-num) == 0)
            return 0;
        sim = num/(den - num);
        return sim;
    }


}
