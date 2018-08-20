package org.spectra.cluster.filter;

import org.spectra.cluster.model.spectra.BinaryPeak;
import org.spectra.cluster.model.spectra.BinarySpectrum;
import org.spectra.cluster.model.spectra.IBinarySpectrum;

import java.util.ArrayList;
import java.util.List;

/**
 * Spectra can only have one peak per m/z bin. After loading, this
 * can be violated. This filter always uses the highest peak per
 * m/z window.
 *
 * @author jg
 */
public class HighestPeakPerBinFilter implements IFilter {
    @Override
    public IBinarySpectrum filter(IBinarySpectrum binarySpectrum) {
        BinaryPeak[] orgPeaks = binarySpectrum.getPeaks();
        List<BinaryPeak> filteredPeaks = new ArrayList<>(orgPeaks.length);

        BinaryPeak highestPeakInCurrentWindow = null;
        int currentWindow = orgPeaks[0].getMz();

        for (BinaryPeak peak : orgPeaks) {
            if (peak.getMz() != currentWindow) {
                filteredPeaks.add(highestPeakInCurrentWindow);
                highestPeakInCurrentWindow = peak;
                currentWindow = peak.getMz();
            }

            if (highestPeakInCurrentWindow == null) {
                highestPeakInCurrentWindow = peak;
                continue;
            }

            if (peak.getIntensity() > highestPeakInCurrentWindow.getIntensity()) {
                highestPeakInCurrentWindow = peak;
            }
        }

        if (highestPeakInCurrentWindow != null) {
            filteredPeaks.add(highestPeakInCurrentWindow);
        }

        // create a copy
        BinarySpectrum filtered = BinarySpectrum.builder()
                .precursorCharge(binarySpectrum.getPrecursorCharge())
                .precursorMZ(binarySpectrum.getPrecursorMz())
                .uui(binarySpectrum.getUUI())
                .peaks(filteredPeaks.toArray(new BinaryPeak[filteredPeaks.size()]))
                .build();

        return filtered;
    }
}
