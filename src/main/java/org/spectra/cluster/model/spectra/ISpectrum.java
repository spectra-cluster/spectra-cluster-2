package org.spectra.cluster.model.spectra;

import org.spectra.cluster.model.commons.ITuple;

import java.util.Set;

public interface ISpectrum extends IBinarySpectrum {

    Set<? extends ITuple> getPeaks();

    float getPrecursorMZ();

    int getPrecursorCharge();

}
