package org.spectra.cluster.model.spectra;

import lombok.Builder;
import lombok.Data;

import java.util.Arrays;
import java.util.UUID;


@Data
@Builder
public class BinarySpectrum implements IBinarySpectrum {
    private final String uui;
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

    /**
     * Return the precursor charge
     * @return precursor charge
     */
    @Override
    public int getPrecursorCharge() {
        return precursorCharge;
    }

    /**
     * Get the precursor mz
     * @return precursor mz
     */
    @Override
    public int getPrecursorMz() {
        return precursorMZ;
    }

    /**
     * Get a COPY of the peak mz values as an array.
     * @return Array of peaks mz values
     */
    @Override
    public int[] getMzVector() {
        return Arrays.stream(peaks).mapToInt(BinaryPeak::getMz).toArray();
    }

    /**
     * Get a COPY of the peak intensity values as an array.
     * @return Array of peaks intensity values
     */
    @Override
    public int[] getIntensityVector() {
        return Arrays.stream(peaks).mapToInt(BinaryPeak::getIntensity).toArray();
    }


}
