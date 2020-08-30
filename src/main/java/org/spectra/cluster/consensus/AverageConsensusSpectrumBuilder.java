package org.spectra.cluster.consensus;

import io.github.bigbio.pgatk.io.common.DefaultSpectrum;
import io.github.bigbio.pgatk.io.common.spectra.Spectrum;
import io.github.bigbio.pgatk.io.properties.IPropertyStorage;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.spectra.cluster.model.cluster.ICluster;
import org.spectra.cluster.util.ClusteringParameters;

import java.util.*;
import java.util.stream.Collectors;

/**
 * ConsensusSpectrumBuilder that is similar to the consensus
 * spectrum builder used by the original spectra-cluster 1.0
 * algorithm.
 *
 * @author jg
 */
@Slf4j
@Data
public class AverageConsensusSpectrumBuilder extends AbstractConsensusSpectrumBuilder {
    private final ClusteringParameters clusteringParameters;

    @Override
    public Spectrum createConsensusSpectrum(ICluster cluster, IPropertyStorage spectrumPropertyStorage) {
        try {
            // load all peaks
            List<ConsensusPeak> peaks = loadOriginalPeaks(cluster, spectrumPropertyStorage, true);

            // merge the peaks
            double fragmentTolerance = (clusteringParameters.getFragmentIonPrecision().equalsIgnoreCase("high") ? 0.01 : 0.5);
            double mzThresholdStep = fragmentTolerance / 5;

            // merge similar peaks with increasing tolerance
            for (double currentTolerance = mzThresholdStep; currentTolerance < fragmentTolerance; currentTolerance += mzThresholdStep) {
                peaks = mergeConsensusPeaks(peaks, currentTolerance);
            }

            // adapt peak intensities based on the probability that they are observed
            List<ConsensusPeak> adaptedIntensityPeaks = adaptPeakIntensities(peaks, cluster.getClusteredSpectraCount());

            // apply the noise filter
            List<ConsensusPeak> filteredPeaks = filterNoise(adaptedIntensityPeaks, 100.0, 5, 20);

            // create the peak list as Map<Double, Double>
            Map<Double, Double> consensusPeaks = new HashMap<>(filteredPeaks.size());

            for (ConsensusPeak p : filteredPeaks)
                consensusPeaks.put(p.getMz(), p.getIntensity());

            // create the spectrum object
            return new DefaultSpectrum(
                    UUID.randomUUID().toString(), // create a new unique id
                    1L,
                    cluster.getPrecursorCharge(),
                    getAveragePrecursorMz(cluster, spectrumPropertyStorage),
                    1.0,
                    consensusPeaks,
                    2,
                    Collections.emptyList());
        } catch (Exception e) {
            log.error(("Failed to build consensus spectrum: " + e.toString()));
            return null;
        }
    }

    /**
     * Adapt the peak intensities in consensusPeaks using the following formula:
     * I = I * (0.95 + 0.05 * (1 + pi))^5
     * where pi is the peaks probability
     */
    protected static List<ConsensusPeak> adaptPeakIntensities(List<ConsensusPeak> peaks, int nSpectra) {
        List<ConsensusPeak> adaptedPeaks = new ArrayList<ConsensusPeak>(peaks);

        for (int i = 0; i < adaptedPeaks.size(); i++) {
            ConsensusPeak peak = adaptedPeaks.get(i);
            double peakProbability = (double) peak.getCount() / (double) nSpectra;
            double newIntensity = peak.getIntensity() * (0.95 + 0.05 * Math.pow(1 + peakProbability, 5));

            adaptedPeaks.set(i, new ConsensusPeak(peak.getMz(), newIntensity, peak.getCount()));
        }

        return adaptedPeaks;
    }

    /**
     * Filters the consensus spectrum keeping only the top N peaks per M m/z
     *
     * @param peaks The peaks to filter
     * @param mzWindowSize The window size to use to filter peaks in m/z
     * @param maxPeaksPerWindow Number of peaks per m/z window
     * @param minTotalPeaks Minimum number of total peaks before filtering is done
     */
    protected static List<ConsensusPeak> filterNoise(List<ConsensusPeak> peaks, double mzWindowSize, int maxPeaksPerWindow, int minTotalPeaks) {
        if (peaks.size() <= minTotalPeaks)
            return peaks;

        // under certain conditions (averaging m/z values) the order of peaks can be disrupted
        peaks.sort(Comparator.comparingDouble(ConsensusPeak::getMz));

        List<ConsensusPeak> filteredPeaks = new ArrayList<>(peaks);
        double maxMz = peaks.stream().mapToDouble(ConsensusPeak::getMz).max().getAsDouble();

        for (double minMz = 0; minMz <= maxMz; minMz += mzWindowSize) {
            final double lowerWindowMz = minMz;

            // extract all peaks
            List<ConsensusPeak> filteredWindowPeaks = peaks.stream()
                    // keep peaks within the window
                    .filter(p -> p.getMz() >= lowerWindowMz && p.getMz() < lowerWindowMz + mzWindowSize)
                    // sort based on intensity
                    .sorted(Comparator.comparingDouble(ConsensusPeak::getIntensity).reversed())
                    // retain max N peaks
                    .limit(maxPeaksPerWindow)
                    .collect(Collectors.toList());

            filteredPeaks.addAll(filteredWindowPeaks);
        }

        return filteredPeaks;
    }
}
