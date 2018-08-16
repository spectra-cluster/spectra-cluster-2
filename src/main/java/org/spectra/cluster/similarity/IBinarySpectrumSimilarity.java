package org.spectra.cluster.similarity;

import org.spectra.cluster.model.spectra.IBinarySpectrum;

import java.io.Serializable;

/**
 * Compute the similarity between a pair of Spectra.
 *
 * @author jg
 */
public interface IBinarySpectrumSimilarity extends Serializable {
    /**
     * Compute the correlation between two {@link IBinarySpectrum}
     * @param spectrumA Spectrum A to be compare
     * @param spectrumB Spectrum B to be compare
     * @return The similarity score as a double.
     */
    double correlation(IBinarySpectrum spectrumA, IBinarySpectrum spectrumB);
}
