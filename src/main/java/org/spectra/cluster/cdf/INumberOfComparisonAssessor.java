package org.spectra.cluster.cdf;

/**
 * This interface describes classes that derive the
 * number of comparisons to use for the threshold
 * calculation.
 *
 * Created by jg on 13.10.17.
 */
public interface INumberOfComparisonAssessor {
    /**
     * Returns the number of comparisons to use for the defined
     * cluster.
     * @param precursorMz The precursor m/z of the object being compared.
     * @param nCurrentClusters The number of clusters this cluster will be compared to.
     * @return The number of comparisons to use.
     */
    int getNumberOfComparisons(int precursorMz, int nCurrentClusters);
}
