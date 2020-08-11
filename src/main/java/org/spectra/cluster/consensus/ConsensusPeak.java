package org.spectra.cluster.consensus;

import lombok.Data;

/**
 * A peak in a consensus spectrum (in Double space) that
 * keeps track of its count and supports merging of peaks.
 */
@Data
public class ConsensusPeak {
    private Double mz;
    private Double intensity;
    private int count;

    /**
     * Initializes a new ConsensusPeak
     */
    public ConsensusPeak(Double mz, Double intensity) {
        this.mz = mz;
        this.intensity = intensity;
        this.count = 1;
    }

    public Double getMz() {
        return mz;
    }

    public Double getIntensity() {
        return intensity;
    }

    /**
     * Merge a peak. Both, m/z and intensity are
     * averaged weighted based on the respective
     * counts.
     * @param peak The peak to merge.
     */
    public void mergePeak(ConsensusPeak peak) {
        // calculate the weight first
        int totalCount = this.count + peak.count;
        double thisWeight = this.count / (double) totalCount;
        double peakWeight = peak.count / (double) totalCount;

        // calculate the new m/z and intensities
        double newMz = this.mz * thisWeight + peak.mz * peakWeight;
        double newIntensity = this.intensity * thisWeight + peak.intensity * peakWeight;

        // update this variables
        this.mz = newMz;
        this.intensity = newIntensity;

        // update the count
        this.count = totalCount;
    }
}
