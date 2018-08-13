package org.spectra.cluster.normalizer;

import lombok.extern.slf4j.Slf4j;

import java.util.*;

/**
 * This class take a list of Peaks mz values and normalize them into a vector of doubles.
 *
 * @author ypriverol
 */
@Slf4j
public class MzPeaksBinnedNormalizer {

    private static  float MIN_MZ = 200F;

    private static  float MAX_MZ = 5000F;
    private static  double HIG_RES_INTERVAL = 1.0F;

    public static int[] binnedHighResMzPeaks(Collection<Double> mzPeaks){
        Iterator<Double> peakIt = mzPeaks.stream().sorted().iterator();
        int currentPeak = (int) peakIt.next().floatValue();
        Integer currentMZ = (int) MIN_MZ;
        int[] intervals = new int[(int) (MAX_MZ - MIN_MZ / HIG_RES_INTERVAL)];

        while(currentPeak < currentMZ && peakIt.hasNext())
            currentPeak = (int) peakIt.next().floatValue();

        if(!peakIt.hasNext()){
            log.error("The current Peaks Lists do not contains any peaks between -- " + MIN_MZ + " and " + MAX_MZ + " --");
        }

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