package org.spectra.cluster.normalizer;

import java.util.List;

public class LogNormalizer implements IIntegerNormalizer {

    private final int precision;

    public LogNormalizer(int precision){
        this.precision = precision;
    }

    /**
     * This method normalize the intensity or mz values to log scale.
     * @param valuesToBin The values that should be binned
     * @return Array of int
     */
    @Override
    public int[] binDoubles(List<Double> valuesToBin) {
        return valuesToBin.stream().map(Math::log).mapToInt(x-> (int) Math.round(x * precision)).toArray();
    }
}
