package org.spectra.cluster.model.consensus;

import lombok.Getter;
import org.spectra.cluster.model.spectra.BinaryPeak;

public class BinaryConsensusPeak extends BinaryPeak {

    @Getter
    protected int count;

    public BinaryConsensusPeak() {
    }

    /**
     * Create a {@link BinaryConsensusPeak} with mz, intensity and count
     * @param mz  Mz in int
     * @param intensity intensity
     * @param count count (number of peaks)
     */
    public BinaryConsensusPeak(int mz, int intensity, int count) {
        super(mz, intensity);
        this.count = count;
    }

    /**
     * Construct a Consensus peak with count 1.
     * @param peak {@link BinaryPeak}
     */
    public BinaryConsensusPeak(BinaryPeak peak) {
        this(peak.getMz(), peak.getIntensity(), 1);
    }

    /**
     * Constructor to copy an existing peak
     * @param peak {@link BinaryConsensusPeak}
     */
    public BinaryConsensusPeak(BinaryConsensusPeak peak) {
        this(peak.getMz(), peak.getIntensity(), peak.getCount());
    }

    @Override
    public BinaryConsensusPeak copy() {
        BinaryConsensusPeak copy = new BinaryConsensusPeak(mz, intensity, count);
        copy.setRank(rank);
        return copy;
    }
}
