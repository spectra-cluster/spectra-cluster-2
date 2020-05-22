package org.spectra.cluster.filter.rawpeaks;

import org.bigbio.pgatk.io.common.spectra.Spectrum;

import java.io.Serializable;
import java.util.Objects;
import java.util.function.Function;

public interface IRawSpectrumFunction extends Serializable, Cloneable, Function<Spectrum, Spectrum> {
    /**
     * Joins to IRawSpectrumFunctions requiring that all
     * return a Spectrum object.
     * @param after The function to call after this one.
     * @return
     */
    default IRawSpectrumFunction specAndThen(IRawSpectrumFunction after) {
        Objects.requireNonNull(after);
        return (Spectrum t) -> after.apply(apply(t));
    }
}
