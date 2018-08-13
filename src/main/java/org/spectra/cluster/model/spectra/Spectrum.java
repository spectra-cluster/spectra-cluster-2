package org.spectra.cluster.model.spectra;

import lombok.Builder;
import lombok.Data;
import org.spectra.cluster.model.commons.Tuple;

import java.util.Set;

@Data
@Builder
public class Spectrum implements ISpectrum {

    Set<Tuple<Float, Float>> peaks;

    float precursorMZ;

    int precursorCharge;

    long uui;

    @Override
    public long getUUI() {
        return uui;
    }

    @Override
    public int getNumberOfPeaks() {
        int count = 0;
        if(peaks != null)
            count = peaks.size();
        return count;
    }
}
