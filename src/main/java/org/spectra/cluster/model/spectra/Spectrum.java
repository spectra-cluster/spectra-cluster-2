package org.spectra.cluster.model.spectra;

import lombok.Builder;
import lombok.Data;
import org.spectra.cluster.model.commons.ITuple;
import org.spectra.cluster.model.commons.Tuple;

import java.util.Set;

@Data
public class Spectrum extends BinarySpectrum implements ISpectrum {

    Set<Tuple<Float, Float>> peaks;

    float precursorMZ;

    public Spectrum() {
    }

    @Override
    public Set< ?extends ITuple> getPeaks() {
        return peaks;
    }

    @Override
    public float getPrecursorMZ() {
        return precursorMZ;
    }
}
