package org.spectra.cluster.filter.rawpeaks;

import java.util.*;

/**
 * Implements the intensity normalisation step used f.e. by Tide.
 * This function is adapted from the spectrum_preprocess2.cc source
 * file from the Tide source code.
 *
 * The sqrt of each peak intensity is taken, then peaks are
 * divided into 10 regions and normalized to have a maximum intensity of 50
 * within each region.
 */
public class TideRawPeaksIntensityNormalizer implements IRawPeakFunction {
    private final int N_REGIONS = 10;
    private final double MAX_REGION_INTENSITY = 50;

    @Override
    public Map<Double, Double> apply(Map<Double, Double> peaks) {
        if (peaks.size() < 1) {
            return peaks;
        }

        // convert to sqrt
        peaks.entrySet().forEach(e -> e.setValue(Math.sqrt(e.getValue())));

        // remove all peaks with less than 5% of the max intensity
        double maxIntensity = peaks.entrySet().stream().mapToDouble(Map.Entry::getValue).max().getAsDouble();
        double minIntensity = maxIntensity * 0.05;

        double maxMz = peaks.entrySet().stream().mapToDouble(Map.Entry::getKey).max().getAsDouble();
        double regionSize = maxMz / N_REGIONS;

        // sort the peaks based on m/z
        List<Map.Entry<Double, Double>> sortedPeaksList = new ArrayList<>(peaks.entrySet());
        sortedPeaksList.sort(Comparator.comparingDouble(Map.Entry::getKey));

        // normalise based on the region size
        int peakIndex = 0;
        Map<Double, Double> filteredPeaks = new HashMap<>(peaks.size());

        for (int regionIndex = 0; regionIndex < N_REGIONS; regionIndex++) {
            double maxRegionMz = regionSize * (regionIndex + 1);
            double maxRegionIntensity = 0;
            // save the index of the first spectrum in this region
            int regionStartIndex = peakIndex;

            // get the total intensity within the region
            for (; peakIndex < sortedPeaksList.size(); peakIndex++) {
                Map.Entry<Double, Double> peak = sortedPeaksList.get(peakIndex);

                if (peak.getKey() > maxRegionMz) {
                    break;
                }

                if (peak.getValue() > maxRegionIntensity) {
                    maxRegionIntensity = peak.getValue();
                }
            }

            double normFactor = MAX_REGION_INTENSITY / maxRegionIntensity;

            // normalize the peaks within the region and copy them to the filtered list
            for (int i = regionStartIndex; i < peakIndex; i++) {
                Map.Entry<Double, Double> peak = sortedPeaksList.get(i);

                if (peak.getValue() < minIntensity) {
                    continue;
                }

                filteredPeaks.put(peak.getKey(), peak.getValue() * normFactor);
            }

        }

        return filteredPeaks;
    }
}
