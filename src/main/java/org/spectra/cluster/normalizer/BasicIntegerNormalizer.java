package org.spectra.cluster.normalizer;

import java.util.List;

/**
 * This basic normalizer simply multiplies the passed values by a constant
 * and returns the rounded result. This is a simple normalization;
 *
 */
public class BasicIntegerNormalizer implements IIntegerNormalizer {
    /**
     * This is optimised for m/z values.
     */
    public static final int MZ_CONSTANT = 10000;
    private final int constant;

    /**
     * Constructor with parametrized constant
     * @param constant default constant to round the double
     */
    public BasicIntegerNormalizer(int constant){
        this.constant = constant;
    }

    /**
     * Default constant (100000)
     */
    public BasicIntegerNormalizer(){
        this.constant = MZ_CONSTANT;
    }

    @Override
    public int[] binDoubles(List<Double> valuesToBin) {
        return valuesToBin.stream().mapToInt(value -> (int) Math.round(value * constant)).toArray();
    }

    /**
     * The corresponding {@link Double} value to be round
     * @param value value to be round
     * @return the integer representation for the value
     */
    public int binValue(Double value){
        return (int) Math.round(value * constant);
    }
}
