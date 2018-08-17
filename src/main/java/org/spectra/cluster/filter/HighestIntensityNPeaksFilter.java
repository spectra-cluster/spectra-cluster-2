package org.spectra.cluster.filter;

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
public class HighestIntensityNPeaksFilter implements IFilter {

    public static int DEFAULT_N_PEAKS = 50;

    @Override
    public IBinarySpectrum filter(IBinarySpectrum binarySpectrum) {
        return binarySpectrum;
    }
}
