package org.spectra.cluster.filter.binaryspectrum;

import org.spectra.cluster.model.spectra.BinaryPeak;
import org.spectra.cluster.model.spectra.BinarySpectrum;
import org.spectra.cluster.model.spectra.IBinarySpectrum;

import java.util.Arrays;
import java.util.Comparator;

/**
 * This filter function retains the amount of peaks required to
 * explain the specified fraction of the total ion current observed
 * within the spectrum.
 */
public class FractionTicFilterFunction implements IBinarySpectrumFunction {
    public static final float DEFAULT_FRACTION_TIC = 0.5f;
    public static final int DEFAULT_MIN_PEAKS = 20;

    private final float fractionTic;
    private final int minPeaksToKeep;

    /**
     * Create a new peak filter based on the fraction of TIC that
     * should be explained.
     * @param fractionTic The relative amount of the spectrum's ion current that should be explained.
     * @param minPeaksToKeep The minimum number of peaks always to retain.
     */
    public FractionTicFilterFunction(float fractionTic, int minPeaksToKeep) {
        this.fractionTic = fractionTic;
        this.minPeaksToKeep = minPeaksToKeep;
    }

    /**
     * Create a default FractionTicFilterFunction.
     */
    public FractionTicFilterFunction() {
        this(DEFAULT_FRACTION_TIC, DEFAULT_MIN_PEAKS);
    }

    @Override
    public IBinarySpectrum apply(IBinarySpectrum binarySpectrum) {
        // make sure there are enough peaks to filter
        if (binarySpectrum.getPeaks().length <= minPeaksToKeep) {
            return binarySpectrum;
        }

        // sort according to intensity
        BinaryPeak[] peaklist = binarySpectrum.getCopyPeaks();
        Arrays.sort(peaklist, Comparator.comparingInt(BinaryPeak::getIntensity).reversed());

        // get the total intensity
        double totalIntensity = Arrays.stream(peaklist).mapToInt(BinaryPeak::getIntensity).sum();

        BinaryPeak[] filteredPeaks = new BinaryPeak[peaklist.length];
        int filteredPeaksSize = 0;
        int explainedTic = 0;

        for (BinaryPeak aPeaklist : peaklist) {
            explainedTic += aPeaklist.getIntensity();
            filteredPeaks[filteredPeaksSize++] = aPeaklist;

            double relExplainedTic = explainedTic / totalIntensity;

            // stop if enough TIC is explained and more then the minimum number of peaks were added
            if (relExplainedTic > fractionTic && filteredPeaksSize >= minPeaksToKeep) {
                break;
            }
        }

        // truncate the array
        filteredPeaks = Arrays.copyOf(filteredPeaks, filteredPeaksSize);

        // re-sort according to m/z
        Arrays.sort(filteredPeaks, Comparator.comparingInt(BinaryPeak::getMz));

        return new BinarySpectrum(binarySpectrum, filteredPeaks);
    }
}
