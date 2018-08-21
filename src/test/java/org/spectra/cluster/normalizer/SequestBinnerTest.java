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

public class SequestBinnerTest {
    private Spectrum testSpectrum;

    @Before
    public void setUp() throws Exception {
        URI uri = Objects.requireNonNull(BinarySpectrum.class.getClassLoader().getResource("single-spectra.mgf")).toURI();
        MgfFile mgfFile = new MgfFile(new File(uri));
        Iterator<Spectrum> specIt = mgfFile.getSpectrumIterator();

        if (!specIt.hasNext()) {
            throw new Exception("Failed to load spectra");
        }

        testSpectrum = specIt.next();
    }

    @Test
    public void testManualBinning() {
        double[] doublesToBin = {0.01, 1.18, 1.19, 18.9};
        List<Double> doubleList = Arrays.stream(doublesToBin).boxed().collect(Collectors.toList());
        SequestBinner binner = new SequestBinner();

        int[] indexes = binner.binDoubles(doubleList);

        Assert.assertEquals(doublesToBin.length, indexes.length);
        Assert.assertEquals(0, indexes[0]);
        Assert.assertEquals(0, indexes[1]);
        Assert.assertEquals(1, indexes[2]);
        Assert.assertEquals(18, indexes[3]);
    }

    @Test
    public void testBinFirstPeaks() {
        List<Double> doubleList = new ArrayList<>(testSpectrum.getPeakList().keySet());
        Collections.sort(doubleList);
        SequestBinner binner = new SequestBinner();

        int[] bins = binner.binDoubles(doubleList);

        Assert.assertEquals(bins.length, doubleList.size());
        Assert.assertEquals(125, bins[0]);
        Assert.assertEquals(128, bins[1]);
        Assert.assertEquals(129, bins[2]);
    }
}
