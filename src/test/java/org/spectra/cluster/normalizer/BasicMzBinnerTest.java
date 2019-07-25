package org.spectra.cluster.normalizer;


import org.bigbio.pgatk.io.common.PgatkIOException;
import org.bigbio.pgatk.io.common.spectra.Spectrum;
import org.bigbio.pgatk.io.mgf.MgfIterableReader;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.spectra.cluster.model.spectra.BinarySpectrum;

import java.io.File;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Arrays;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

public class BasicMzBinnerTest {
    private MgfIterableReader mgfFile;

    @Before
    public void setUp() throws URISyntaxException, PgatkIOException {
        URI uri = Objects.requireNonNull(BinarySpectrum.class.getClassLoader().getResource("single-spectra.mgf")).toURI();
        mgfFile = new MgfIterableReader(new File(uri), true, false, true);
    }

    @Test
    public void binnedHighResMzPeaks() {
        if (mgfFile.hasNext()) {
            Spectrum spectrum = mgfFile.next();
            BasicMzBinner binner = new BasicMzBinner();

            int[] values = binner.binDoubles(spectrum.getPeakList().entrySet().stream().map(Map.Entry::getKey).collect(Collectors.toList()));
            Assert.assertEquals(88, values.length);
        }
    }

    @Test
    public void testUnbinning() {
        double[] valuesToBin = {0.01, 10.123, 100.123, 382.1231, 2982.1231};

        BasicMzBinner binner = new BasicMzBinner();
        int[] bins = binner.binDoubles(Arrays.stream(valuesToBin).boxed().collect(Collectors.toList()));
        double[] unbinned = binner.unbinValues(bins);

        Assert.assertEquals(valuesToBin.length, unbinned.length);
        Assert.assertEquals(valuesToBin.length, unbinned.length);
        Assert.assertEquals(0, unbinned[0], 0.001);
        Assert.assertEquals(10, unbinned[1], 0.001);
        Assert.assertEquals(100, unbinned[2], 0.001);
        Assert.assertEquals(382, unbinned[3], 0.001);
        Assert.assertEquals(2982, unbinned[4], 0.001);
    }
}
