package org.spectra.cluster.filter.rawpeaks;

import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;

/**
 * Function that only retains the N highest peaks of the
 * passed RAW peaklist.
 */
public class KeepNHighestRawPeaks implements IRawPeakFunction {
    private final int maxPeaks;
    public static final int DEFAULT_MAX_PEAKS = 70;

    public KeepNHighestRawPeaks(int maxPeaks) {
        this.maxPeaks = maxPeaks;
    }

    /**
     * Initializes a KeppNHighestPeaksFunction with the
     * DEFAULT_MAX_PEAKS number of maximum peaks.
     */
    public KeepNHighestRawPeaks() {
        this(DEFAULT_MAX_PEAKS);
    }

    @Override
    public Map<Double, Double> apply(Map<Double, Double> peaks) {
        // only filter if there are too many peaks
        if (peaks.size() <= maxPeaks) {
            return peaks;
        }

        Map<Double, Double> filteredPeaks = new HashMap<>(maxPeaks);

        peaks.entrySet().stream()
                .sorted(Comparator.comparingDouble(e -> ((Map.Entry<Double, Double>) e).getValue()).reversed())
                .limit(maxPeaks)
                .forEach(peak -> filteredPeaks.put(peak.getKey(), peak.getValue()));

        return filteredPeaks;
    }
}
