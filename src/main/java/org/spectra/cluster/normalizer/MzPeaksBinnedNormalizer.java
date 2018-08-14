package org.spectra.cluster.normalizer;

import lombok.Builder;
import lombok.extern.slf4j.Slf4j;

import java.util.*;

/**
 * This class take a list of Peaks mz values and normalize them into a vector of doubles.
 *
 * @author ypriverol
 */
@Slf4j
public class MzPeaksBinnedNormalizer implements IIntegerNormalizer{

    private static  float MIN_MZ = 70F;

    private static  float MAX_MZ = 5000F;
    private static  double HIG_RES_INTERVAL = 1.0F;

    @Override
    public int[] binDoubles(List<Double> valuesToBin) {
        Iterator<Double> peakIt = valuesToBin.stream().sorted().iterator();
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
                if(peakIt.hasNext())
                    currentPeak = (int) peakIt.next().floatValue();
            }
            currentMZ ++;
        }


        return intervals;
    }
}
