package org.spectra.cluster.model.spectra;

import lombok.Builder;
import lombok.Data;


@Data
@Builder
public class BinarySpectrum implements IBinarySpectrum {

    long uui;
    int precursortMZ;
    int precursorCharge;

    private int[] mzPeaksVector;
    private int[] intensityPeaksVector;

    @Override
    public long getUUI() {
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
        return precursortMZ;
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
