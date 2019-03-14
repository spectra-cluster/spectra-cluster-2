package org.spectra.cluster.model.spectra;

import lombok.Data;
import org.spectra.cluster.filter.binaryspectrum.IBinarySpectrumFunction;

import java.util.*;
import java.util.stream.Collectors;


@Data
public class BinarySpectrum implements IBinarySpectrum {
    private final String uui;
    private final int precursorMZ;
    private final int precursorCharge;
    private final IBinarySpectrumFunction comparisonFilter;
    private Set<BinaryPeak> comparisonPeakSet;
    private int minComparisonMz;
    private int maxComparisonMz;

    private BinaryPeak[] peaks;

    /**
     * Create a binary spectrum object.
     * @param uui The (unique) id to use
     * @param precursorMZ The precursor m/z as integer
     * @param precursorCharge The precursor charge
     * @param peaks The peaklist
     * @param comparisonFilter The comparison filter to apply to the spectrum
     */
    public BinarySpectrum(String uui, int precursorMZ, int precursorCharge, BinaryPeak[] peaks, IBinarySpectrumFunction comparisonFilter) {
        this.uui = uui;
        this.precursorMZ = precursorMZ;
        this.precursorCharge = precursorCharge;
        this.peaks = Arrays.copyOf(peaks, peaks.length);
        this.comparisonFilter = comparisonFilter;
    }

    /**
     * Create a binary spectrum object.
     * @param precursorMZ The precursor m/z as integer
     * @param precursorCharge The precursor charge
     * @param peaks The peaklist
     * @param comparisonFilter The comparison filter to apply to the spectrum
     */
    public BinarySpectrum(int precursorMZ, int precursorCharge, BinaryPeak[] peaks, IBinarySpectrumFunction comparisonFilter) {
        this(UUID.randomUUID().toString(), precursorMZ, precursorCharge, peaks, comparisonFilter);
    }

    /**
     * Creates a new binary spectrum based on an existing one
     * with a different peak list.
     * @param spectrum The IBinarySpectrum to copy the properties from.
     * @param peakList The new peaklist to use. The spectrum object will create a copy of this peaklist.
     */
    public BinarySpectrum(IBinarySpectrum spectrum, BinaryPeak[] peakList, boolean updateRanks) {
        this.uui = spectrum.getUUI();
        this.precursorMZ = spectrum.getPrecursorMz();
        this.precursorCharge = spectrum.getPrecursorCharge();
        this.peaks = Arrays.copyOf(peakList, peakList.length);
        this.comparisonFilter = spectrum.getComparisonFilter();

        if (updateRanks) {
            addRanks(this.peaks,true);
        }
    }

    /**
     * Adds the rank to all peaks if required
     * @param peaks The peak array to which the ranks will be added
     * @param force If set to true, ranks are updated even if the peaks already contain ranks
     */
    public static void addRanks(BinaryPeak[] peaks, boolean force) {
        if (peaks.length < 1) {
            return;
        }
        // if the first peak has a rank, assume that all peaks have one
        if (!force && peaks[0].getRank() > 0) {
            return;
        }

        // sort according to intensity
        Arrays.parallelSort(peaks, Comparator.comparingInt(BinaryPeak::getIntensity).reversed());

        for (int i = 0; i < peaks.length; i++) {
            peaks[i].setRank(i + 1);
        }

        // sort according to m/z again
        Arrays.parallelSort(peaks, Comparator.comparingInt(BinaryPeak::getMz));
    }

    @Override
    public String getUUI() {
        return uui;
    }

    @Override
    public BinaryPeak[] getPeaks() {
        addRanks(peaks,false);
        return peaks;
    }

    @Override
    public BinaryPeak[] getCopyPeaks() {
        addRanks(peaks,false);
        return Arrays.copyOf(peaks, peaks.length);
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
    public int[] getCopyMzVector() {
        return Arrays.stream(peaks).mapToInt(BinaryPeak::getMz).toArray();
    }

    /**
     * Get a COPY of the peak intensity values as an array.
     * @return Array of peaks intensity values
     */
    @Override
    public int[] getCopyIntensityVector() {
        return Arrays.stream(peaks).mapToInt(BinaryPeak::getIntensity).toArray();
    }

    @Override
    public Set<BinaryPeak> getComparisonFilteredPeaks() {
        if (comparisonPeakSet == null) {
            addRanks(peaks,false);
            IBinarySpectrum filteredSpectrum = comparisonFilter.apply(this);

            if (filteredSpectrum.getPeaks().length < 1) {
                return Collections.emptySet();
            }

            // update max and min m/z
            minComparisonMz = filteredSpectrum.getPeaks()[0].mz;
            maxComparisonMz = filteredSpectrum.getPeaks()[filteredSpectrum.getPeaks().length - 1].mz;

            // store the set
            comparisonPeakSet = Arrays.stream(filteredSpectrum.getPeaks()).collect(Collectors.toSet());
        }

        return Collections.unmodifiableSet(comparisonPeakSet);
    }

    @Override
    public int getMinComparisonMz() {
        if (comparisonPeakSet == null) {
            getComparisonPeakSet();
        }

        return minComparisonMz;
    }

    @Override
    public int getMaxComparisonMz() {
        if (comparisonPeakSet == null) {
            getComparisonPeakSet();
        }

        return maxComparisonMz;
    }
}
