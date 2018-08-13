package org.spectra.cluster.model.spectra;

import java.util.Collection;

/**
 * This code is licensed under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 *
 * ==Overview==
 *
 * @author ypriverol on 09/08/2018.
 */

interface IBinarySpectrum extends ISpectrum{

    /** Get Precursor MZ in integer **/
    int getPrecursorMz();

    /** Get the vector of the peaks mz Values **/
    Collection<Integer> getMzVector();

    /** Get sparse matrix of Intensities **/
    Collection<Integer> getIntensityVector();

}
