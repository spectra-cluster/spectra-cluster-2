package org.spectra.cluster.filter.rawpeaks;

import io.github.bigbio.pgatk.io.common.spectra.Spectrum;
import org.spectra.cluster.util.Masses;
import java.util.HashMap;
import java.util.Map;

/**
 * Implements the removal of precursor peaks.
 *
 * <b>Warning:</b> This class alters the original object!
 */
public class RemovePrecursorPeaksFunction implements IRawSpectrumFunction {
    private final Double fragmentTolerance;

    public RemovePrecursorPeaksFunction(Double fragmentTolerance) {
        this.fragmentTolerance = fragmentTolerance;
    }

    @Override
    public Spectrum apply(Spectrum spectrum) {
        // this filter only works if the spectrum's charge is known
        if (spectrum.getPrecursorCharge() < 1) {
            return spectrum;
        }

        // calculate m/z of neutral losses
        final double doubleCharge    = (double) spectrum.getPrecursorCharge();
        final double waterLoss       = spectrum.getPrecursorMZ() - (Masses.WATER_MONO / doubleCharge);
        final double doubleWaterLoss = spectrum.getPrecursorMZ() - (2.0F * Masses.WATER_MONO / doubleCharge);
        final double ammoniumLoss    = spectrum.getPrecursorMZ() - (Masses.AMMONIA_MONO / doubleCharge);
        final double mtaLoss         = spectrum.getPrecursorMZ() - (Masses.MTA / doubleCharge);

        // calculate range based on fragmentIonTolerance
        final double minWaterLoss        = waterLoss - fragmentTolerance;
        final double maxWaterLoss        = waterLoss + fragmentTolerance;
        final double minDoubleWaterLoss  = doubleWaterLoss - fragmentTolerance;
        final double maxDoubleWaterLoss  = doubleWaterLoss + fragmentTolerance;
        final double minAmmoniumLoss     = ammoniumLoss - fragmentTolerance;
        final double maxAmmoniumLoss     = ammoniumLoss + fragmentTolerance;
        final double minMtaLoss          = mtaLoss - fragmentTolerance;
        final double maxMtaLoss          = mtaLoss + fragmentTolerance;

        // also filter the default precursor
        final double minPrecursor = spectrum.getPrecursorMZ() - fragmentTolerance;
        final double maxPrecursor = spectrum.getPrecursorMZ() + fragmentTolerance;
        final double minPrecursorC1 = spectrum.getPrecursorMZ() + (Masses.C13_DIFF / doubleCharge) - fragmentTolerance;
        final double maxPrecursorC1 = spectrum.getPrecursorMZ() + (Masses.C13_DIFF / doubleCharge) + fragmentTolerance;
        final double minPrecursorC2 = spectrum.getPrecursorMZ() + (Masses.C13_DIFF * 2) / doubleCharge - fragmentTolerance;
        final double maxPrecursorC2 = spectrum.getPrecursorMZ() + (Masses.C13_DIFF * 2) / doubleCharge + fragmentTolerance;

        Map<Double, Double> filteredPeakList = new HashMap<>(spectrum.getPeakList().size());

        for (Map.Entry<Double, Double> peak : spectrum.getPeakList().entrySet()) {
            final double peakMz = peak.getKey();

            // ignore any peak that could be a neutral loss
            if (isWithinRange(minWaterLoss, maxWaterLoss, peakMz))
                continue;
            if (isWithinRange(minDoubleWaterLoss, maxDoubleWaterLoss, peakMz))
                continue;
            if (isWithinRange(minAmmoniumLoss, maxAmmoniumLoss, peakMz))
                continue;
            if (isWithinRange(minPrecursor, maxPrecursor, peakMz))
                continue;
            if (isWithinRange(minPrecursorC1, maxPrecursorC1, peakMz))
                continue;
            if (isWithinRange(minPrecursorC2, maxPrecursorC2, peakMz))
                continue;
            if (isWithinRange(minMtaLoss, maxMtaLoss, peakMz))
                continue;

            filteredPeakList.put(peak.getKey(), peak.getValue());
        }

        spectrum.getPeakList().clear();
        spectrum.getPeakList().putAll(filteredPeakList);

        return spectrum;
    }

    private boolean isWithinRange(double min, double max, double value) {
        return (value >= min && value <= max);
    }
}
