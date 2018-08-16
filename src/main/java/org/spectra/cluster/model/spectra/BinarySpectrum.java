package org.spectra.cluster.model.spectra;

import jdk.nashorn.internal.runtime.BitVector;
import lombok.Builder;
import lombok.Data;

import java.util.UUID;


@Data
@Builder
public class BinarySpectrum implements IBinarySpectrum {
    private final String uui;
    private final int precursorMZ;
    private final int precursorCharge;

    private final int[] mzPeaksVector;
    private final int[] intensityPeaksVector;

    // this is intended for later use
    // private final BitVector lowResMzBitVector;

    @Override
    public String getUUI() {
        return uui;
    }

    @Override
    public int getNumberOfPeaks() {
        int count = 0 ;
        if(mzPeaksVector != null)
            count = mzPeaksVector.length;
        return count;
    }

    @Override
    public int getPrecursorCharge() {
        return precursorCharge;
    }

    @Override
    public int getPrecursorMz() {
        return precursorMZ;
    }

    @Override
    public int[] getMzVector() {
        return mzPeaksVector;
    }

    @Override
    public int[] getIntensityVector() {
        return intensityPeaksVector;
    }


}
