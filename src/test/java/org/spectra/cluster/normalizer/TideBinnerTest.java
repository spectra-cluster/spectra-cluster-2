package org.spectra.cluster.normalizer;


import org.bigbio.pgatk.io.common.spectra.Spectrum;
import org.bigbio.pgatk.io.mgf.MgfIterableReader;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.net.URI;
import java.util.*;
import java.util.stream.Collectors;

public class TideBinnerTest {
    private Spectrum testSpectrum;
    private List<Spectrum> allSpectra = new ArrayList<>(100);
    private final static boolean verbose = false;

    @Before
    public void setUp() throws Exception {
        URI uri = Objects.requireNonNull(TideBinnerTest.class.getClassLoader().getResource("single-spectra.mgf")).toURI();
        MgfIterableReader mgfFile = new MgfIterableReader(new File(uri), true, false, true);

        while (mgfFile.hasNext()) {
            allSpectra.add(mgfFile.next());
        }
        testSpectrum = allSpectra.get(0);
    }

    @Test
    public void testManualBinning() {
        double[] doublesToBin = {0.01, 1.18, 1.19, 18.9};
        List<Double> doubleList = Arrays.stream(doublesToBin).boxed().collect(Collectors.toList());
        TideBinner binner = new TideBinner();

        int[] indexes = binner.binDoubles(doubleList);

        Assert.assertEquals(doublesToBin.length, indexes.length);
        Assert.assertEquals(0, indexes[0]);
        Assert.assertEquals(1, indexes[1]);
        Assert.assertEquals(1, indexes[2]);
        Assert.assertEquals(19, indexes[3]);
    }

    @Test
    public void testBinFirstPeaks() {
        List<Double> doubleList = new ArrayList<>(testSpectrum.getPeakList().keySet());
        Collections.sort(doubleList);
        TideBinner binner = new TideBinner();

        int[] bins = binner.binDoubles(doubleList);

        Assert.assertEquals(bins.length, doubleList.size());
        Assert.assertEquals(126, bins[0]);
        Assert.assertEquals(129, bins[1]);
        Assert.assertEquals(130, bins[2]);
    }

    @Test
    public void testUnbinning() {
        double[] valuesToBin = {0.01, 10.123, 100.123, 382.1231, 2982.1231};

        TideBinner binner = new TideBinner();
        int[] bins = binner.binDoubles(Arrays.stream(valuesToBin).boxed().collect(Collectors.toList()));
        double[] unbinned = binner.unbinValues(bins);

        Assert.assertEquals(valuesToBin.length, unbinned.length);
        Assert.assertEquals(0.01, unbinned[0], 0.01);
        Assert.assertEquals(10.1, unbinned[1], 0.1);
        Assert.assertEquals(100.1, unbinned[2], 0.05);
        Assert.assertEquals(382.1, unbinned[3], 0.1);
        Assert.assertEquals(2982, unbinned[4], 2);
    }
}
