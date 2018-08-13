package org.spectra.cluster.normalizer;

import java.util.*;

/**
 * This class take a list of Peaks mz values and normalize them into a vector of doubles
 *
 * @author ypriverol
 */
public class MzPeaksBinnedNormalizer {

    private static  float MIN_MZ = 200F;
    private static  float MAX_MZ = 7000F;
    private static  double HIG_RES_INTERVAL = 1.0F;

    public static double[] binnedHighResMzPeaks(Collection<Float> mzPeaks){
        Iterator<Float> peakIt = mzPeaks.stream().sorted().iterator();
        int currentPeak = (int) peakIt.next().floatValue();
        Integer currentMZ = (int) MIN_MZ;
        double[] intervals = new double[(int) (MAX_MZ - MIN_MZ / HIG_RES_INTERVAL)];
        while(currentPeak < currentMZ && peakIt.hasNext())
            currentPeak = (int) peakIt.next().floatValue();

        for(int i = 0; i < intervals.length ; i++){
            if(currentMZ == currentPeak){
                intervals[i] = currentPeak;
                currentPeak = (int) peakIt.next().floatValue();
            }
            currentMZ ++;
        }
        return intervals;
    }
}
