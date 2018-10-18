package org.spectra.cluster.io.spectra;

import org.spectra.cluster.model.spectra.IBinarySpectrum;

/**
 * This interface describes classes that listen to
 * new spectra being processed.
 *
 * It is intended to be used to, for example, count all
 * spectra within a certain precursor m/z region.
 */
public interface ISpectrumListener {
    /**
     * This function is called whenever a new spectrum is
     * being processed (generally, when it is loaded).
     * @param spectrum The newly processed spectrum
     */
    void onNewSpectrum(IBinarySpectrum spectrum);
}
