package org.spectra.cluster.normalizer;

/**
 * Classes used to bin m/z values
 */
public interface IMzBinner extends IIntegerNormalizer {
    /**
     * Convert binned values back to the original representation. Returned
     * values will not be identical to the original ones as some binning
     * methods use lower resolutions than stored in the original values.
     * @param valuesToUnbin Previously binned values.
     * @return Unbinned approximation of the original double values.
     */
    double[] unbinValues(int[] valuesToUnbin);
}
