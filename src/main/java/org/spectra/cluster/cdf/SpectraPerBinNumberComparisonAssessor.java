package org.spectra.cluster.cdf;

import org.spectra.cluster.io.spectra.ISpectrumListener;
import org.spectra.cluster.model.spectra.IBinarySpectrum;

/**
 * This INumberOfComparisonAssessor assess the number of
 * comparisons based on all spectra within a bin. For this
 * to work, all spectra must be pre-scanned to derive
 * the number of spectra per bin. Once a spectrum is observed,
 * this spectrum can be "counted" using this classes countSpectrum
 * function.
 *
 * Created by jg on 13.10.17.
 */
public class SpectraPerBinNumberComparisonAssessor implements INumberOfComparisonAssessor, ISpectrumListener {
    private final double windowSize;
    private final int[] spectraPerBin;
    private final int maxBin;
    private final int minSpectra;

    /**
     * Create a new SpectraPerBinNumberComparisonAssessor.
     * @param windowSize This window size must correspond to the actual integer numbers
     *                   of the precursor m/z values. For example, if a binning approach
     *                   was used for the precursor's and the number of spectra should be
     *                   collected over 5 bins, windowSize needs to be set to 5.
     * @param minSpectra The minimum number of spectra always to return.
     * @param maxPrecursor The maximum precursor possible.
     */
    public SpectraPerBinNumberComparisonAssessor(int windowSize, int minSpectra, int maxPrecursor) {
        this.windowSize = windowSize;
        this.minSpectra = minSpectra;

        // initiate the bins
        int nBins = (int) Math.ceil(maxPrecursor / this.windowSize);
        maxBin = nBins - 1;
        spectraPerBin = new int[nBins];

        for (int i = 0; i < nBins; i++) {
            spectraPerBin[i] = 0;
        }
    }

    /**
     * Count this spectrum to know how many spectra exist per
     * bin. This function is thread safe.
     * @param precursorMz The spectrum's precursor m/z
     */
    public synchronized void countSpectrum(int precursorMz) {
        int bin = getBinForSpectrum(precursorMz);
        spectraPerBin[bin] += 1;
    }

    @Override
    public void onNewSpectrum(IBinarySpectrum spectrum) {
        countSpectrum(spectrum.getPrecursorMz());
    }

    /**
     * Get the bin this spectrum belongs to.
     * @param precursorMz The spectrum's precursor m/z
     * @return 0-based bin.
     */
    private int getBinForSpectrum(int precursorMz) {
        int bin = (int) Math.floor(precursorMz / windowSize);
        if (bin > maxBin) {
            return maxBin;
        } else {
            return bin;
        }
    }

    @Override
    public int getNumberOfComparisons(int precursorMz, int nCurrentClusters) {
        int bin = getBinForSpectrum(precursorMz);

        int count;

        // never return anything lower than 1
        if (spectraPerBin[bin] <= 1)
            count = 1;
        else
            count = spectraPerBin[bin];

        if (count < minSpectra)
            return minSpectra;
        else
            return count;
    }
}
