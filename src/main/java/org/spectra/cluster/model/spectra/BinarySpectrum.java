package org.spectra.cluster.model.spectra;

import lombok.Builder;
import lombok.Data;

import java.util.Collection;


@Data
@Builder
public class BinarySpectrum implements IBinarySpectrum {

    long uui;
    int precursortMZ;
    int precursorCharge;

    private Collection<Integer> mzPeaksVector;
    private Collection<Integer> intensityPeaksVector;


    @Override
    public long getUUI() {
        return uui;
    }

    @Override
    public int getNumberOfPeaks() {
        int count = 0 ;
        if(mzPeaksVector != null)
            count = mzPeaksVector.size();
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
    public Collection<Integer> getMzVector() {
        return mzPeaksVector;
    }

    @Override
    public Collection<Integer> getIntensityVector() {
        return intensityPeaksVector;
    }
}
