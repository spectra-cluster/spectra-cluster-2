package org.spectra.cluster.similarity;

import java.io.Serializable;

public interface ISimilarityFunction extends Serializable {

    /**
     * This method compare two vectors with some similarity function, see for implementations {@link KendallsCorrelation}
     * or {@link JaccardCorrelation}
     *
     * @param collectionA Collection A to be compare
     * @param collectionB Collection B to be compare
     * @return A similarity score.
     *
     */
    double correlation(int[] collectionA, int[] collectionB);
}
