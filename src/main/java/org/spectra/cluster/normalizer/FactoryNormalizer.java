package org.spectra.cluster.normalizer;

import lombok.extern.slf4j.Slf4j;
import org.spectra.cluster.model.spectra.BinaryPeak;

import java.util.Arrays;
import java.util.Comparator;
import java.util.Map;
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
 * @author ypriverol on 17/08/2018.
 */
@Slf4j
public class FactoryNormalizer {

    private IIntegerNormalizer mzBinner;
    private IIntegerNormalizer intensityBinner;

    /**
     * Factory Normalizer
     */
    public FactoryNormalizer(IIntegerNormalizer mzBinner, IIntegerNormalizer intensityBinner){
        this.mzBinner = mzBinner;
        this.intensityBinner = intensityBinner;
    }

    /**
     * This function normalize the spectrum peaks to an Array of {@link BinaryPeak}
     * @param peakList Map<{@link Double} map of double, double of peaks
     * @return Array of BinaryPeak
     * @throws Exception Exception if normalization breaks the integrity of the spectra.
     */
    public BinaryPeak[] normalizePeaks(Map<Double, Double> peakList) throws Exception {
        int[] mzValues = mzBinner.binDoubles(peakList
                .entrySet().stream()
                .map(Map.Entry::getKey)
                .collect(Collectors.toList()));

        int[] intensityValues = intensityBinner.binDoubles(peakList
                .entrySet().stream()
                .map(Map.Entry::getValue)
                .collect(Collectors.toList()));
        if(mzValues.length != intensityValues.length){
            log.error("The normalization step has destroy the spectrum structure");
            throw new Exception("The normalization step has destroy the spectrum");
        }

        BinaryPeak[] peaks = new BinaryPeak[intensityValues.length];
        for(int i = 0; i < mzValues.length; i++)
            peaks[i] = new BinaryPeak(mzValues[i], intensityValues[i]);

        // sort the peaks
        Arrays.sort(peaks, Comparator.comparingInt(BinaryPeak::getMz));

        return peaks;
    }

}
