package org.spectra.cluster.normalizer;

import java.util.List;
import java.util.stream.Collectors;

/**
 * This code is licensed under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * ==Overview==
 *
 * Cumulative Normalizer for the intensity peaks as proposed by
 * https://pubs.acs.org/doi/abs/10.1021/pr0603248
 *
 * @author ypriverol on 21/08/2018.
 */
public class CumulativeIntensityNormalizer implements IIntensityNormalizer {
    public static final int MAX_INTENSITY = 100000;
    private final int precision;

    public CumulativeIntensityNormalizer(){
        this.precision = MAX_INTENSITY;
    }

    public CumulativeIntensityNormalizer(int precision){
        this.precision = precision;
    }

    /**
     * This function takes a list of peak intensities and return a new set of
     * values with the the cumulative intensity values.
     *
     * @param valuesToBin The values that should be binned
     * @return intensity values.
     */

    @Override
    public int[] binDoubles(List<Double> valuesToBin) {

        List<Double> orderedPeaks = valuesToBin.stream().sorted((v1, v2) -> Double.compare(v2, v1)).collect(Collectors.toList());
        double tic = orderedPeaks.stream().mapToDouble(Double::doubleValue).sum();

        int[] resultPeaks = new int[orderedPeaks.size()];
        double currentSum = 0;
        for(int i = orderedPeaks.size() -1 ; i > -1; i--){
            currentSum = currentSum + (orderedPeaks.get(i)/tic);
            int index = valuesToBin.indexOf(orderedPeaks.get(i));
            resultPeaks[index] = (int) Math.round(currentSum * precision);
        }

        return resultPeaks;
    }
}
