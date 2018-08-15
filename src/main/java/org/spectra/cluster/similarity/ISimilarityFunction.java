package org.spectra.cluster.similarity;

public interface ISimilarityFunction {

    /**
     * This method compare two vectors with some similarity function, see for implementations {@link KendallsCorrelation} or {@link JaccardCorrelation}
     *
     * @param collectionA
     * @param collectionB
     * @return
     */
    public double correlation(int[] collectionA, int[] collectionB);
}
