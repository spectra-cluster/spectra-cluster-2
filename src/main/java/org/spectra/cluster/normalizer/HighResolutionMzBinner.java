package org.spectra.cluster.normalizer;

import org.apache.commons.math3.util.FastMath;

import java.util.List;

/**
 * A binner that bins all m/z values into 0.02 m/z wide bins.
 */
public class HighResolutionMzBinner implements IMzBinner {
    @Override
    public double[] unbinValues(int[] valuesToUnbin) {
        double[] unbinned = new double[valuesToUnbin.length];

        for (int i = 0; i < valuesToUnbin.length; i++) {
            unbinned[i] = (double) valuesToUnbin[i] * 0.02;
        }

        return unbinned;
    }

    @Override
    public int[] binDoubles(List<Double> valuesToBin) {
        int[] binned = new int[valuesToBin.size()];

        for (int i = 0; i < valuesToBin.size(); i++) {
            binned[i] = (int) FastMath.floor(valuesToBin.get(i) / 0.02);
        }

        return binned;
    }
}
