package org.spectra.cluster.similarity;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Slf4j
public class JaccardCorrelation implements ISimilarity {

    /**
     * This method computes Jaccard coefficient for neighbors of a given
     * pair of Collections.
     * @param firstCollection First Collection to Compare
     * @param secondCollection Second Collection to Compare
     * @return The similarity score between both collections.
     */
    @Override
    public double correlation(int[] firstCollection,
                                              int[]  secondCollection) {
        List<Integer> coll1 =  Arrays.stream(firstCollection).boxed().filter(x -> x > 0 ).collect(Collectors.toList());
        List<Integer> coll2 =  Arrays.stream(secondCollection).boxed().filter(x -> x > 0 ).collect(Collectors.toList());

        if(coll1 == null || coll2 == null || coll1.isEmpty() || coll2.isEmpty())
            log.error("One of the peak Lists is empty -- ");

        int intersection = CollectionUtils.intersection(Objects.requireNonNull(coll1), Objects.requireNonNull(coll2))
                .size();
        int union = CollectionUtils.union(coll1, coll2).size();

        return (double) intersection / union;
    }
}
