package org.spectra.cluster.model.cluster;


import org.spectra.cluster.exceptions.SpectraClusterException;
import org.spectra.cluster.model.consensus.IConsensusSpectrumBuilder;
import org.spectra.cluster.model.spectra.IBinarySpectrum;

import java.io.Serializable;
import java.util.List;
import java.util.Set;

/**
 * Interface describing a cluster
 *
 * @author jg
 */
public interface ICluster extends Serializable {

    /**
     * Returns the cluster's id. This is identical with the cluster's consensus spectrum's id.
     * @return The cluster's id as String.
     */
    String getId();

    /**
     * Returns the cluster's (average) precursor m/z
     *
     * @return The cluster's precursor m/z
     */
    int getPrecursorMz();

    /**
     * The cluster's consensus spectrum's charge
     *
     * @return The charge
     */
    int getPrecursorCharge();

    /**
     * Get consensus spectrum
     */
    IBinarySpectrum getConsensusSpectrum();

    /**
     * Returns the ids of the clustered spectra. The
     * actual spectra are not stored
     * @return The list of clustered spectra ids.
     */
    Set<String> getClusteredSpectraIds();

    /**
     * The number of clustered spectra
     * @return The number of clustered spectra.
     */
    int getClusteredSpectraCount();

    /**
     * Adds spectra to the cluster
     * @param spectra The IBinarySpectrum objects to put
     */
    void addSpectra(IBinarySpectrum... spectra);

    /**
     * Merges the cluster with this one. The id of this cluster will be
     * adapted to the added one in case the added cluster is larger
     * than the current one.
     * @param clusterToMerge The cluster to merge.
     */
    void mergeCluster(ICluster clusterToMerge);

    IConsensusSpectrumBuilder getConsensusSpectrumBuilder();

    void saveComparisonResult(String id, float similarity);

    /**
     * The results of the last N comparisons.
     * @return A list of ComparisonMatchS
     */
    List<ComparisonMatch> getComparisonMatches();

    boolean isKnownComparisonMatch(String clusterId);

    /**
     * This method allow to convert an ICluster into a Byte Serializable object
     * @return byte Array
     */
    byte[] toBytes() throws SpectraClusterException;

}
