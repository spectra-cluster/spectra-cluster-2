package org.spectra.cluster.normalizer;


import java.util.List;

/**
 * Implementation of the binning procedure found in the Sequest
 * Comet and MaRaCluster algorithm.
 * Bin boundries are calculated based on Ii = 1.000508 x (0.18 + i) Th, for i = 0, 1
 *
 * @author jg
 */
public class SequestBinner implements IIntegerNormalizer {

    @Override
    public int[] binDoubles(List<Double> valuesToBin) {
        // allocate the memory for the index
        int[] binIndexes = new int[valuesToBin.size()];

        // get the bin for every value
        for (int i = 0; i < valuesToBin.size(); i++) {
            Double value = valuesToBin.get(i);
            // stay in double space to prevent rounding issues by Java
            Double binIndex = (value / 1.000508) - 0.18;

            binIndexes[i] = (int) Math.floor(binIndex);

            if (binIndexes[i] < 0) {
                binIndexes[i] = 0;
            }
        }

        return binIndexes;
    }

    /**
     * Returns the upper and lower bound for the specified bin.
     * @param bin The bin's 0-based index.
     * @return An array of doubles containing the [lower bound, upper bound]
     */
    public double[] getBinLimits(int bin) {
        // make sure the bin is a positive integer
        if (bin < 0) {
            throw new IllegalArgumentException("Bin must be a positive integer");
        }

        double upperBound = 1.000508 * (0.18 + bin);
        double lowerBound;

        if (bin == 0) {
            lowerBound = 0;
        } else {
            lowerBound = 1.000508 * (0.18 + bin - 1);
        }

        return new double[] {lowerBound, upperBound};
    }
}
