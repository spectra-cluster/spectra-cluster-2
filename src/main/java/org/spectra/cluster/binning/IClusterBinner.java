package org.spectra.cluster.binning;

import org.spectra.cluster.exceptions.SpectraClusterException;
import org.spectra.cluster.model.cluster.IClusterProperties;

/**
 * Bins cluster.
 */
public interface IClusterBinner {
    /**
     * Bins the specified clusters based on the implementation.
     *
     * The result of the binning procedure is returned as a 2-dimensional
     * array. The first dimension corresponds to the bins which then
     * contains an array of cluster ids.
     *
     * @param clusters The clusters to bin.
     * @param shift If set, the clusters are shifted by 50%.
     * @return The binning result as a 2-dimensional array of cluster ids.
     */
    String[][] binClusters(IClusterProperties[] clusters, boolean shift) throws SpectraClusterException;
}
