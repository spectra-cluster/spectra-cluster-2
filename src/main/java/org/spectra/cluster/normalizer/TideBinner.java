package org.spectra.cluster.normalizer;


import org.spectra.cluster.util.Masses;

import java.util.List;

/**
 * Implementation of the binning procedure as used in Tide.
 * This code is adapted from the Tide source code in
 * mass_constants.h
 *
 * @author jg
 */
public class TideBinner implements IMzBinner {
    public static final double BIN_WIDTH = 1.0005079;
    public static final double BIN_OFFSET = 0.68;

    /** All peaks are converted using a charge = 1 **/
    private static final int CHARGE = 1;

    @Override
    public int[] binDoubles(List<Double> valuesToBin) {
        // allocate the memory for the index
        int[] binIndexes = new int[valuesToBin.size()];

        // get the bin for every value
        for (int i = 0; i < valuesToBin.size(); i++) {
            Double value = valuesToBin.get(i);
            // stay in double space to prevent rounding issues by Java
            double binIndex = (value + (CHARGE - 1)* Masses.PROTON)/(CHARGE*BIN_WIDTH) + 1.0 - BIN_OFFSET;

            binIndexes[i] = (int) Math.floor(binIndex);

            if (binIndexes[i] < 0) {
                binIndexes[i] = 0;
            }
        }

        return binIndexes;
    }

    /**
     * Unbin the passed value
     * @param bin The bin's 0-based index.
     * @return double
     */
    public double unbin(int bin) {
        // make sure the bin is a positive integer
        if (bin < 0) {
            throw new IllegalArgumentException("Bin must be a positive integer");
        }

        double binDouble = (double) bin;

        double value = (binDouble - 1.0 + BIN_WIDTH) * (CHARGE * BIN_WIDTH) - (CHARGE - 1) * Masses.PROTON;

        return value;
    }

    @Override
    public double[] unbinValues(int[] valuesToUnbin) {
        double[] unbinned = new double[valuesToUnbin.length];

        for (int i = 0; i < valuesToUnbin.length; i++) {
            unbinned[i] = unbin(valuesToUnbin[i]);
        }

        return unbinned;
    }
}
