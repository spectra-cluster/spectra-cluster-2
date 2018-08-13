package org.spectra.cluster.similarity;

import cern.colt.matrix.impl.SparseDoubleMatrix1D;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.spectra.cluster.utils.math.Matrix2DUtils;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Slf4j
public class JaccardComparator {

    /**
     * This method computes Jaccard coefficient for neighbors of a given
     * pair of Collections.
     * @param firstCollection First Collection to Compare
     * @param secondCollection Second Collection to Compare
     * @return The similarity score between both collections.
     */
    public static double computeVectorJaccard(int[] firstCollection,
                                              int[]  secondCollection) {
        List<Integer> coll1 =  Arrays.stream(firstCollection).boxed().collect(Collectors.toList());
        List<Integer> coll2 =  Arrays.stream(secondCollection).boxed().collect(Collectors.toList());

        if(coll1 == null || coll2 == null || coll1.isEmpty() || coll2.isEmpty())
            log.error("One of the peak Lists is empty -- ");

        int intersection = CollectionUtils.intersection(Objects.requireNonNull(coll1), coll2)
                .size();
        int union = CollectionUtils.union(coll1, coll2).size();

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
        double num;
        double den;
        num = Matrix2DUtils.productQuick(firstCollection, secondCollection);
        den = Matrix2DUtils.getSqrSum(firstCollection) + Matrix2DUtils.getSqrSum(secondCollection);


        if((den-num) == 0)
            return 0;
        sim = num/(den - num);
        return sim;
    }


}
