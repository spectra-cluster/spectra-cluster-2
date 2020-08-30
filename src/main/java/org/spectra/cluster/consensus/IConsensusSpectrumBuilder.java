package org.spectra.cluster.consensus;

import io.github.bigbio.pgatk.io.common.spectra.Spectrum;
import io.github.bigbio.pgatk.io.properties.IPropertyStorage;
import org.spectra.cluster.model.cluster.ICluster;

/**
 * Describes classes that create consensus spectra
 * based on a list of clusters.
 *
 * @author jg
 */
public interface IConsensusSpectrumBuilder {
    /**
     * Creates a consensus spectrum for the defined cluster. Some consensus
     * spectrum building approaches require additional information that is
     * retrieved from the IPropertyStorage.
     *
     * @param cluster The cluster to build the consensus spectrum for.
     * @param spectrumPropertyStorage The property storage holding the spectra's additional properties.
     * @return The consensus spectrum.
     */
    public Spectrum createConsensusSpectrum(ICluster cluster, IPropertyStorage spectrumPropertyStorage);
}
