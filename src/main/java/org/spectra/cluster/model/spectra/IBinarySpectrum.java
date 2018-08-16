package org.spectra.cluster.model.spectra;

import java.io.Serializable;

/**
 * This code is licensed under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 *
 * This interface structured the spectra for clustering in Binary format. Binary means that every
 * value precursor mz, charge, etc. is encoded as integers.
 *
 *
 * @author ypriverol on 09/08/2018.
 */

public interface IBinarySpectrum extends Serializable, Cloneable {


    /**
     * Get Precursor MZ in integer
     * @return Precursor mz
     */
    int getPrecursorMz();


    /**
     * Get the precursor charge
     * @return Precursor Charge
     */
    int getPrecursorCharge();


    /**
     * Get the vector of the peaks mz Values
     * @return MZ Vector of integer
     */
    int[] getMzVector();

    /**
     * Get the Vector of intensity Values
     * @return Get intensity Vector
     */
    int[] getIntensityVector();

    /**
     * Get number of peaks in the Spectrum
     * @return Number of Peaks
     */
    int getNumberOfPeaks();

    /**
     * Get the Unique identifier
     * @return identifier
     */
    String getUUI();

}
