package org.spectra.cluster.model.spectra;

import lombok.Data;

import java.util.Arrays;
import java.util.UUID;


@Data
public class BinarySpectrum implements IBinarySpectrum {
    private final String uui;
    private final int precursorMZ;
    private final int precursorCharge;

    private BinaryPeak[] peaks;

    /**
     * Create a binary spectrum object.
     * @param uui The (unique) id to use
     * @param precursorMZ The precursor m/z as integer
     * @param precursorCharge The precursor charge
     * @param peaks The peaklist
     */
    public BinarySpectrum(String uui, int precursorMZ, int precursorCharge, BinaryPeak[] peaks) {
        this.uui = uui;
        this.precursorMZ = precursorMZ;
        this.precursorCharge = precursorCharge;
        this.peaks = Arrays.copyOf(peaks, peaks.length);
    }

    /**
     * Create a binary spectrum object.
     * @param precursorMZ The precursor m/z as integer
     * @param precursorCharge The precursor charge
     * @param peaks The peaklist
     */
    public BinarySpectrum(int precursorMZ, int precursorCharge, BinaryPeak[] peaks) {
        this(UUID.randomUUID().toString(), precursorMZ, precursorCharge, peaks);
    }

    /**
     * Creates a new binary spectrum based on an existing one
     * with a different peak list.
     * @param spectrum The IBinarySpectrum to copy the properties from.
     * @param peakList The new peaklist to use. The spectrum object will create a copy of this peaklist.
     */
    public BinarySpectrum(IBinarySpectrum spectrum, BinaryPeak[] peakList) {
        this.uui = spectrum.getUUI();
        this.precursorMZ = spectrum.getPrecursorMz();
        this.precursorCharge = spectrum.getPrecursorCharge();
        this.peaks = Arrays.copyOf(peakList, peakList.length);
    }

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
