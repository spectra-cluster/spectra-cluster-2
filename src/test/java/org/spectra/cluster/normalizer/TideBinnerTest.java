package org.spectra.cluster.normalizer;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.spectra.cluster.model.spectra.BinarySpectrum;
import uk.ac.ebi.pride.tools.jmzreader.model.Spectrum;
import uk.ac.ebi.pride.tools.mgf_parser.MgfFile;

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
        MgfFile mgfFile = new MgfFile(new File(uri));
        Iterator<Spectrum> specIt = mgfFile.getSpectrumIterator();

        while (specIt.hasNext()) {
            allSpectra.add(specIt.next());
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
    public void compareBinners() {
        SequestBinner sequestBinner = new SequestBinner();
        TideBinner tideBinner = new TideBinner();

        for (Spectrum s : allSpectra) {
            List<Double> mzValues = new ArrayList<>(s.getPeakList().keySet());
            int[] sequestBins = sequestBinner.binDoubles(mzValues);
            int[] tideBins = tideBinner.binDoubles(mzValues);

            Assert.assertEquals(sequestBins.length, tideBins.length);

            for (int i = 0; i < sequestBins.length; i++) {
                if (sequestBins[i] != tideBins[i]) {
                    if (verbose)
                        System.out.println(String.format("Different bin for %.2f m/z %d -> %d", mzValues.get(i), sequestBins[i], tideBins[i]));
                }

                Assert.assertTrue(Math.abs(sequestBins[i] - tideBins[i]) <= 1);
            }
        }
    }
}
