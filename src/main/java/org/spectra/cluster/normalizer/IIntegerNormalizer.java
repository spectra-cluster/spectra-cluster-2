package org.spectra.cluster.normalizer;

import java.io.Serializable;
import java.util.List;

/**
 * This interface describes classes that normalizes a list
 * of floats / doubles and returns their values as integers. This Interface can be use to
 * model a normalizer like:
 *
 * {@link BasicIntegerNormalizer}
 *
 * where the data is easy transformed from one {@link Double} to an {@link Integer}.
 *
 * or it can be used to do binning of a set of Doubles into a vector of integers:
 *
 * {@link SequestBinner}
 *
 * @author jg
 */
public interface IIntegerNormalizer extends Serializable, Cloneable {
    /**
     * Convert a list of doubles to an array of ints by
     * binning them. The integer array then only contains
     * the indexes of the bins.
     * @param valuesToBin The values that should be binned
     * @return An integer array containing the values' indexes
     *         based on the binning procedure.
     */
    int[] binDoubles(List<Double> valuesToBin);
}
