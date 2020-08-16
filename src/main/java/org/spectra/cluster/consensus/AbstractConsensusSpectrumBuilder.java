package org.spectra.cluster.consensus;

import io.github.bigbio.pgatk.io.common.PgatkIOException;
import io.github.bigbio.pgatk.io.common.spectra.Spectrum;
import io.github.bigbio.pgatk.io.properties.IPropertyStorage;
import io.github.bigbio.pgatk.io.properties.StoredProperties;
import org.spectra.cluster.model.cluster.ICluster;
import org.spectra.cluster.normalizer.IIntensityNormalizer;
import org.spectra.cluster.normalizer.MaxPeakNormalizer;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Collection of commonly required functions for all
 * consensus spectrum builders.
 */
public abstract class AbstractConsensusSpectrumBuilder implements IConsensusSpectrumBuilder {
    private static final IIntensityNormalizer intensityNormalizer = new MaxPeakNormalizer();

    @Override
    public abstract Spectrum createConsensusSpectrum(ICluster cluster, IPropertyStorage spectrumPropertyStorage);

    // TODO: Load original scores based on https://github.com/bigbio/pia/blob/master/src/main/java/de/mpc/pia/modeller/score/ScoreModelEnum.javay

    /**
     * Merge consensus peaks that are within the specified m/z tolerance.
     *
     * Similar peaks are merged using the ConsensusPeak::mergePeak function where
     * the weighted average m/z and weighted average intensity is used.
     *
     * @param peaks The peaks to process.
     * @param mzTolerance The tolerance within which to merge peaks.
     * @return A list of merged peaks.
     */
    static protected List<ConsensusPeak> mergeConsensusPeaks(List<ConsensusPeak> peaks, double mzTolerance) {
        // sort the peaks according to m/z
        peaks.sort(Comparator.comparingDouble(ConsensusPeak::getMz));

        // iterate over the peaks
        List<ConsensusPeak> sortedPeaks = new ArrayList<>(peaks.size());
        ConsensusPeak lastPeak = null;

        for (ConsensusPeak currentPeak : peaks) {
            // if within the tolerance, merge into the last peak
            if (lastPeak != null && currentPeak.getMz() - lastPeak.getMz() <= mzTolerance) {
                lastPeak.mergePeak(currentPeak);
            } else {
                // add as new peak
                sortedPeaks.add(currentPeak);
                lastPeak = currentPeak;
            }
        }

        return sortedPeaks;
    }

    /**
     * Loads all original spectra from the property storage and returns them as one
     * crowded peak list.
     *
     * @param cluster The cluster to load the spectra for.
     * @param propertyStorage The spectra's property storage.
     * @param normalizeIntensity If set, the intensity is automatically normalized based on the hightest peak intensity.
     * @return A list of ConsensusPeakS
     * @throws PgatkIOException If the loading of properties failed.
     */
    static protected List<ConsensusPeak> loadOriginalPeaks(ICluster cluster, IPropertyStorage propertyStorage,
                                                           boolean normalizeIntensity) throws PgatkIOException  {
        // load a max of 50 peaks per spectrum
        List<ConsensusPeak> peaks = new ArrayList<>(cluster.getClusteredSpectraCount() * 50);

        for (String spectrumId : cluster.getClusteredSpectraIds()) {
            // load the m/z and intensity values
            String mzString = propertyStorage.get(spectrumId, StoredProperties.ORIGINAL_PEAKS_MZ);
            String intensityString = propertyStorage.get(spectrumId, StoredProperties.ORIGINAL_PEAKS_INTENS);

            double[] mz = convertDoubleString(mzString);
            double[] intens = convertDoubleString(intensityString);

            // sanity check
            if (mz.length != intens.length) {
                throw new PgatkIOException("Number of m/z and intensity values differs for spectrum " + spectrumId);
            }

            // normalize the intensity
            if (normalizeIntensity) {
                // convert to double list
                List<Double> intensList = Arrays.stream(intens).boxed().collect(Collectors.toList());

                // normalize
                intens = Arrays.stream(intensityNormalizer.binDoubles(intensList))
                        .mapToDouble(Double::new).toArray();
            }

            for (int i = 0; i < mz.length; i++) {
                peaks.add(new ConsensusPeak(mz[i], intens[i]));
            }
        }

        return peaks;
    }

    /**
     * Convert a string of "," delimited double values into an
     * array of doubles.
     * @param valueString The string to convert.
     * @return An array of doubles.
     */
    static private double[] convertDoubleString(String valueString) {
        return Arrays.stream(valueString.split(","))
                .mapToDouble(Double::valueOf)
                .toArray();
    }
}
