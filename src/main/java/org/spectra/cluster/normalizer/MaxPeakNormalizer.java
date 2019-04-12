package org.spectra.cluster.normalizer;

import lombok.Getter;

import java.util.List;

/**
 * Normalizes peaks' intensities by setting the maximum
 * peak to a fixed number.
 */
public class MaxPeakNormalizer implements IIntensityNormalizer {
    public static final int MAX_INTENSITY = 100000;

    @Getter
    private final int targetMaxIntensity;

    public MaxPeakNormalizer(int maxIntensity) {
        this.targetMaxIntensity = maxIntensity;
    }

    public MaxPeakNormalizer() {
        this(MAX_INTENSITY);
    }

    @Override
    public int[] binDoubles(List<Double> valuesToBin) {
        if (valuesToBin.size() < 1) {
            return new int[0];
        }

        // get the max intensity
        double maxIntensity = valuesToBin.stream().mapToDouble(Double::doubleValue).max().getAsDouble();

        // get the ratio to use to convert the intensities
        double ratio = (double) targetMaxIntensity / maxIntensity;

        return valuesToBin.stream().mapToInt(value -> (int) Math.round(value * ratio)).toArray();
    }
}
