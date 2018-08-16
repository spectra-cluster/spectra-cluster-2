package org.spectra.cluster.normalizer;

import lombok.Data;

import java.util.List;

/**
 * This basic normalizer simply multiplies the passed values by a constant
 * and returns the rounded result.
 */
@Data
public class BasicIntegerNormalizer implements IIntegerNormalizer {
    /**
     * This is optimised for m/z values.
     */
    public static final int MZ_CONSTANT = 100000;
    private final int constant;

    @Override
    public int[] binDoubles(List<Double> valuesToBin) {
        int[] convertedValues = valuesToBin.stream().mapToInt(value -> (int) Math.round(value * constant)).toArray();
        return convertedValues;
    }
}
