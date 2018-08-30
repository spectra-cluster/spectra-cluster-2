package org.spectra.cluster.filter.binaryspectrum;

import org.junit.Assert;
import org.junit.Test;
import org.spectra.cluster.model.spectra.BinaryPeak;
import org.spectra.cluster.model.spectra.BinarySpectrum;
import org.spectra.cluster.model.spectra.IBinarySpectrum;

public class FractionTicFilterFunctionTest {
    @Test
    public void testFractionTicFilter() {
        FractionTicFilterFunction function = new FractionTicFilterFunction();

        BinaryPeak[] testPeaks = {
                new BinaryPeak(1, 1),
                new BinaryPeak(2, 2),
                new BinaryPeak(3, 3),
                new BinaryPeak(4, 4),
                new BinaryPeak(5, 5),
                new BinaryPeak(6, 6),
                new BinaryPeak(7, 7),
                new BinaryPeak(8, 8)
        };

        BinarySpectrum spectrum = new BinarySpectrum(1, 1, testPeaks);

        IBinarySpectrum filtered = function.apply(spectrum);

        Assert.assertEquals(8, filtered.getNumberOfPeaks());

        function = new FractionTicFilterFunction(0.5f, 0);
        filtered = function.apply(spectrum);

        Assert.assertEquals(8, spectrum.getNumberOfPeaks());
        Assert.assertEquals(3, filtered.getNumberOfPeaks());
    }
}
