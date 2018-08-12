package org.spectra.cluster.model.spectra;

import cern.colt.matrix.impl.SparseDoubleMatrix1D;

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

public interface IBinarySpectrum {

    /** A unique auto generated id **/
    long getUUI();

    /** Get Precursor MZ in integer **/
    int getIntPrecursorMz();

    /** Get precursor charge **/
    int getPrecursorCharge();

    /** Get the vector of the peaks mz Values **/
    SparseDoubleMatrix1D getIntMzVector();

    /** Get sparse matrix of Intensities **/
    SparseDoubleMatrix1D getIntIntensityVector();



}
