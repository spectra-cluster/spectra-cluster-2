package org.spectra.cluster.model.spectra;

import lombok.Data;

import java.io.Serializable;
import java.util.Objects;

/**
 * This code is licensed under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 *
 *  Peaks can be define as a combination of an intensity value and mz value and store a rank.
 *
 * @author ypriverol on 16/08/2018.
 * @author jg
 */

@Data
public class BinaryPeak implements Serializable {

    protected int mz;
    protected int intensity;
    /** The peaks' rank in the spectrum where 1 is the highest peak and 0 if the rank isn't set **/
    protected int rank = 0;
    protected int mzHash;

    public BinaryPeak() {
    }

    public BinaryPeak(int mz, int intensity) {
        this.mz = mz;
        this.intensity = intensity;
        this.mzHash = Objects.hash(this.mz);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof BinaryPeak)) return false;
        BinaryPeak that = (BinaryPeak) o;
        return mz == that.mz;
    }

    @Override
    public int hashCode() {
        return mzHash;
    }

    /**
     * Create a copy of the peak
     * @return A new BinaryPeak object
     */
    public BinaryPeak copy() {
        BinaryPeak copy = new BinaryPeak(mz, intensity);
        copy.setRank(rank);
        return copy;
    }
}
