package org.spectra.cluster.filter.binaryspectrum;

import org.spectra.cluster.model.spectra.BinaryPeak;
import org.spectra.cluster.model.spectra.BinarySpectrum;
import org.spectra.cluster.model.spectra.IBinarySpectrum;

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
    private int numberOfPeaks;

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

        BinaryPeak[] retainedPeaks = new BinaryPeak[numberOfPeaks];
        int storedPeaks = 0;

        // this does not change the m/z order
        for (BinaryPeak p : binarySpectrum.getPeaks()) {
            if (p.getRank() <= numberOfPeaks) {
                retainedPeaks[storedPeaks++] = p;
            }
        }

        // create the filtered spectrum - peak ranks do not change
        return new BinarySpectrum(binarySpectrum, retainedPeaks, false);
    }
}
