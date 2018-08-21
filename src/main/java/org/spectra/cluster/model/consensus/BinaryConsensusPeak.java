package org.spectra.cluster.model.consensus;

import lombok.Getter;
import org.spectra.cluster.model.spectra.BinaryPeak;

public class BinaryConsensusPeak extends BinaryPeak {
    @Getter
    protected final int count;

    public BinaryConsensusPeak(int mz, int intensity, int count) {
        super(mz, intensity);
        this.count = count;
    }

    public BinaryConsensusPeak(BinaryPeak peak) {
        this(peak.getMz(), peak.getIntensity(), 1);
    }

    /**
     * Constructor to copy an existing peak
     * @param peak
     */
    public BinaryConsensusPeak(BinaryConsensusPeak peak) {
        this(peak.getMz(), peak.getIntensity(), peak.getCount());
    }
}
