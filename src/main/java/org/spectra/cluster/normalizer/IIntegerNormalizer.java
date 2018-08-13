package org.spectra.cluster.normalizer;

import java.util.List;

/**
 * This interface describes classes that normalizes a list
 * of floats / doubles and returns their values as
 * integers.
 * @author jg
 */
public interface IIntegerNormalizer {
    /**
     * Convert a list of doubles to an array of ints by
     * binning them. The integer array then only contains
     * the indexes of the bins.
     * @param valuesToBin The values that should be binned
     * @return An integer array containing the values' indexes
     *         based on the binning procedure.
     */
    public int[] binDoubles(List<Double> valuesToBin);
}
