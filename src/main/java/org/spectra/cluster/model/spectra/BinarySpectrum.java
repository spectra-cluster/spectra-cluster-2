package org.spectra.cluster.model.spectra;

import lombok.Builder;
import lombok.Data;

import java.util.Arrays;
import java.util.UUID;


@Data
@Builder
public class BinarySpectrum implements IBinarySpectrum {
    private final String uui = UUID.randomUUID().toString();
    private final int precursorMZ;
    private final int precursorCharge;

    private BinaryPeak[] peaks;

    @Override
    public String getUUI() {
        return uui;
    }

    @Override
    public BinaryPeak[] getPeaks() {
        return peaks;
    }

    @Override
    public int getNumberOfPeaks() {
        int count = 0 ;
        if(peaks != null)
            count = peaks.length;
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
        return Arrays.stream(peaks).mapToInt(BinaryPeak::getMz).toArray();
    }

    @Override
    public int[] getIntensityVector() {
        return Arrays.stream(peaks).mapToInt(BinaryPeak::getIntensity).toArray();
    }


}
