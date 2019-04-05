package org.spectra.cluster.normalizer;

import lombok.extern.slf4j.Slf4j;

import java.util.List;

/**
 * This class take a list of Peaks mz values and normalize them into a vector of doubles.
 *
 * @author ypriverol
 */
@Slf4j
public class BasicMzBinner implements IMzBinner {

    public static double HIG_RES_INTERVAL = 1.0F;
    private double binnerValue;

    /**
     * Default constructor use 1.0F resolution
     * for the binning process.
     */
    public BasicMzBinner(){
        this.binnerValue = HIG_RES_INTERVAL;
    }

    /**
     * Constructor with binValue.
     * @param binValue Binning Value
     */
    public BasicMzBinner(double binValue){
        this.binnerValue = binValue;
    }

    @Override
    public int[] binDoubles(List<Double> valuesToBin) {
        // allocate the memory for the index
        int[] binIndexes = new int[valuesToBin.size()];

        // get the bin for every value
        for (int i = 0; i < valuesToBin.size(); i++) {
            Double value = valuesToBin.get(i);
            // stay in double space to prevent rounding issues by Java
            double binIndex = (value / binnerValue);
            binIndexes[i] = (int) Math.floor(binIndex);
            if (binIndexes[i] < 0) {
                binIndexes[i] = 0;
            }
        }
        return binIndexes;
    }

    @Override
    public double[] unbinValues(int[] valuesToUnbin) {
        double[] unbinned = new double[valuesToUnbin.length];

        for (int i = 0; i < valuesToUnbin.length; i++) {
            unbinned[i] = (double) valuesToUnbin[i] * binnerValue;
        }

        return unbinned;
    }
}
