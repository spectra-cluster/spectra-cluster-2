package org.spectra.cluster.filter.binaryspectrum;

import org.spectra.cluster.model.spectra.BinaryPeak;
import org.spectra.cluster.model.spectra.BinarySpectrum;
import org.spectra.cluster.model.spectra.IBinarySpectrum;

import java.util.Arrays;
import java.util.Comparator;

/**
 * This code is licensed under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 *
 * This function return the most intense N peaks of one {@link org.spectra.cluster.model.spectra.IBinarySpectrum}
 *
 *
 *
 * @author ypriverol on 16/08/2018.
 */

public class HighestIntensityNPeaksFunction implements IBinarySpectrumFunction {

    public int numberOfPeaks;

    /**
     * Constructor with the Number of highestPeaks
     * @param numberOfPeaks number of peaks to keep
     */
    public HighestIntensityNPeaksFunction(int numberOfPeaks) {
        this.numberOfPeaks = numberOfPeaks;
    }

    @Override
    public IBinarySpectrum apply(IBinarySpectrum binarySpectrum) {

        if(binarySpectrum.getPeaks().length < numberOfPeaks)
            return binarySpectrum;

        Arrays.parallelSort(binarySpectrum.getPeaks(), (o1, o2) -> Integer.compare(o2.getIntensity(), o1.getIntensity()));
        BinaryPeak[] peaks = Arrays.copyOfRange(binarySpectrum.getPeaks(), 0, numberOfPeaks );

        // sort according to m/z again
        Arrays.sort(peaks, Comparator.comparingInt(BinaryPeak::getMz));

        return new BinarySpectrum(binarySpectrum, peaks);
    }
}
