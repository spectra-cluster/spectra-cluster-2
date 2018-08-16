package org.spectra.cluster.similarity;

import org.spectra.cluster.model.spectra.IBinarySpectrum;

/**
 * Assess the similarity between two spectra.
 *
 * @author jg
 */
public interface ISimilarityChecker {
    /**
     * Assesses the similarity between two spectra.
     * @param spectrum1
     * @param spectrum2
     * @return The similarity score as a double.
     */
    double assessSimilarity(IBinarySpectrum spectrum1, IBinarySpectrum spectrum2);
}
