package org.spectra.cluster.model.cluster;

/**
 * Holds basic information about a cluster.
 *
 * This class is intended for tasks like binning and sorting of clusters.
 */
public interface IClusterProperties {
    /**
     * Returns the cluster's id. This is identical with the cluster's consensus spectrum's id.
     * @return The cluster's id as String.
     */
    String getId();

    /**
     * The cluster's consensus spectrum's charge
     *
     * @return The charge
     */
    Integer getPrecursorCharge();

    /**
     * Returns the cluster's (average) precursor m/z
     *
     * @return The cluster's precursor m/z
     */
    int getPrecursorMz();
}
