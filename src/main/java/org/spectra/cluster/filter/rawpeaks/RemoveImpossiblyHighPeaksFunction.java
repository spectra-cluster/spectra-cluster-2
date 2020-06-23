package org.spectra.cluster.filter.rawpeaks;

import io.github.bigbio.pgatk.io.common.spectra.Spectrum;
import org.spectra.cluster.util.Masses;

import java.util.HashMap;
import java.util.Map;

/**
 * Removes all peaks that are too high based on
 * the spectrum's precursor m/z
 *
 * Again, this filter is adapted from the Tide source code.
 */
public class RemoveImpossiblyHighPeaksFunction implements IRawSpectrumFunction {
    @Override
    public Spectrum apply(Spectrum spectrum) {
        Map<Double, Double> filteredPeaklist = new HashMap<>(spectrum.getPeakList().size());

        double maxMz = (spectrum.getPrecursorMZ() - Masses.PROTON) * spectrum.getPrecursorCharge() + Masses.PROTON + 50;

        spectrum.getPeakList().entrySet().stream()
                .filter(peak -> peak.getKey() <= maxMz)
                .forEach(peak -> filteredPeaklist.put(peak.getKey(), peak.getValue()));

        spectrum.getPeakList().clear();
        spectrum.getPeakList().putAll(filteredPeaklist);

        return spectrum;
    }
}
