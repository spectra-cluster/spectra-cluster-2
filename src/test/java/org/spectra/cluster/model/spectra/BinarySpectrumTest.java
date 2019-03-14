package org.spectra.cluster.model.spectra;


import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.spectra.cluster.engine.GreedyClusteringEngine;
import org.spectra.cluster.io.spectra.MzSpectraReader;
import uk.ac.ebi.pride.tools.jmzreader.JMzReaderException;
import uk.ac.ebi.pride.tools.jmzreader.model.Spectrum;
import uk.ac.ebi.pride.tools.mgf_parser.MgfFile;

import java.io.File;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Iterator;
import java.util.Objects;

public class BinarySpectrumTest {

    Iterator<Spectrum> specIt = null;
    IBinarySpectrum binarySpectrum = new BinarySpectrum(345567, 2, new BinaryPeak[6], GreedyClusteringEngine.COMPARISON_FILTER);


    @Before
    public void setUp() throws JMzReaderException, URISyntaxException {

        URI uri = Objects.requireNonNull(BinarySpectrum.class.getClassLoader().getResource("single-spectra.mgf")).toURI();
        MgfFile mgfFile = new MgfFile(new File(uri));
        specIt = mgfFile.getSpectrumIterator();

    }

    @Test
    public void readBinarySpectrum() {

        Spectrum spectrum = specIt.next();
        BinarySpectrum binarySpectrum = new BinarySpectrum((int)spectrum.getPrecursorMZ().doubleValue(), spectrum.getPrecursorCharge(), new BinaryPeak[0], GreedyClusteringEngine.COMPARISON_FILTER);
        Assert.assertEquals(2, binarySpectrum.getPrecursorCharge());

    }

    @Test
    public void getUUI() {
        Assert.assertTrue(!binarySpectrum.getUUI().isEmpty());
    }

    @Test
    public void getPeaks() {
        Assert.assertEquals(6, binarySpectrum.getPeaks().length);
    }

    @Test
    public void getNumberOfPeaks() {
        Assert.assertEquals(6, binarySpectrum.getPeaks().length);
    }

    @Test
    public void getPrecursorCharge() {
        Assert.assertEquals(2, binarySpectrum.getPrecursorCharge());
    }

    @Test
    public void getPrecursorMz() {
        Assert.assertEquals(345567, binarySpectrum.getPrecursorMz());
    }

    @Test
    public void testPeakSortOrder() throws Exception{
        URI uri = Objects.requireNonNull(BinarySpectrum.class.getClassLoader().getResource("single-spectra.mgf")).toURI();
        MzSpectraReader reader = new MzSpectraReader(new File(uri), GreedyClusteringEngine.COMPARISON_FILTER);

        Iterator<IBinarySpectrum> spectrumIterator = reader.readBinarySpectraIterator();
        IBinarySpectrum spectrum = spectrumIterator.next();

        Assert.assertNotNull(spectrum);

        // make sure peaks are sorted
        for (int i = 0; i < spectrum.getPeaks().length - 1; i++) {
            Assert.assertTrue(spectrum.getPeaks()[i].getMz() < spectrum.getPeaks()[i + 1].getMz());
        }

        // change the peaks
        // Note: sorting does not change the sort order only if a copy is used.
        BinaryPeak[] peaks = Arrays.copyOf(spectrum.getPeaks(), spectrum.getPeaks().length);
        Arrays.sort(peaks, Comparator.comparingInt(BinaryPeak::getIntensity).reversed());

        // make sure peaks are sorted
        for (int i = 0; i < spectrum.getPeaks().length - 1; i++) {
            Assert.assertTrue("Sorting changed order", spectrum.getPeaks()[i].getMz() < spectrum.getPeaks()[i + 1].getMz());
        }
    }

}
