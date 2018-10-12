package org.spectra.cluster.filter.rawpeaks;

import edu.emory.mathcs.backport.java.util.Collections;
import uk.ac.ebi.pride.tools.jmzreader.model.Spectrum;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

public class DeisotoperFunction implements IRawSpectrumFunction {
    private final double ISOTOPE_SPACING = 1.003355;
    private final double deisotopeThreshold;

    /**
     * Create a new RawSpectrumDeisotoperFunction
     * @param deisotopeThreshold The threshold to use to find isotopic peaks in ppm.
     */
    public DeisotoperFunction(double deisotopeThreshold) {
        this.deisotopeThreshold = deisotopeThreshold;
    }

    @Override
    public Spectrum apply(Spectrum spectrum) {
        // use a sorted peaklist to work with
        List<Map.Entry<Double, Double>> sortedPeaks = new ArrayList<>(spectrum.getPeakList().entrySet());
        Collections.sort(sortedPeaks, Comparator.comparingDouble(value -> ((Map.Entry<Double, Double>) value).getKey()));

        List<Double> peaksToRemove = new ArrayList<>(sortedPeaks.size() / 2);

        for (int peakIndex = 0; peakIndex < sortedPeaks.size(); peakIndex++) {
            if (peakIndex == 0) {
                continue;
            }

            // deisotope_threshold in ppm
            double location = sortedPeaks.get(peakIndex).getKey();
            double intensity = sortedPeaks.get(peakIndex).getValue();

            for (int fragmentCharge = 1; fragmentCharge < spectrum.getPrecursorCharge(); fragmentCharge++) {
                double isotopeLocation = location - (ISOTOPE_SPACING / fragmentCharge);
                double ppmDiffernce = (location * deisotopeThreshold) / 1e6;

                double isotopicIntensity = maxPeakInRange(sortedPeaks, isotopeLocation - ppmDiffernce, isotopeLocation + ppmDiffernce, peakIndex);

                if (intensity < isotopicIntensity) {
                    // remove this peak
                    peaksToRemove.add(sortedPeaks.get(peakIndex).getKey());
                }
            }
        }

        peaksToRemove.forEach(mz -> spectrum.getPeakList().remove(mz));

        return spectrum;
    }

    /**
     * Returns the intensity of the highest peak within the defined m/z region.
     * @param peaks The list of m/z sorted peaks.
     * @param minMz The minimum m/z the retrieved peak must have.
     * @param maxMz The maximum m/z the retrieved peak must have.
     * @param centerPeakIndex The index of the center peak within that region.
     * @return The found highest intensity within the region.
     */
    private double maxPeakInRange(List<Map.Entry<Double, Double>> peaks, double minMz, double maxMz, int centerPeakIndex) {
        double maxIntensity = 0;

        // only look "below" the center peak
        for (int i = centerPeakIndex; i >= 0; i--) {
            double currentMz = peaks.get(i).getKey();

            if (currentMz > maxMz) {
                continue;
            }
            if (currentMz < minMz) {
                break;
            }

            double intensity = peaks.get(i).getValue();

            if (intensity > maxIntensity) {
                maxIntensity = intensity;
            }
        }

        return maxIntensity;
    }
}
