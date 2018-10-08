package org.spectra.cluster.filter.rawpeaks;

import uk.ac.ebi.pride.tools.jmzreader.model.Spectrum;

import java.io.Serializable;
import java.util.function.Function;

public interface IRawSpectrumFunction  extends Serializable, Cloneable, Function<Spectrum, Spectrum> {
    @Override
    Spectrum apply(Spectrum spectrum);
}
