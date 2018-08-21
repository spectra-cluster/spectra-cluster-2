package org.spectra.cluster.model.spectra;


import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import uk.ac.ebi.pride.tools.jmzreader.JMzReaderException;
import uk.ac.ebi.pride.tools.jmzreader.model.Spectrum;
import uk.ac.ebi.pride.tools.mgf_parser.MgfFile;

import java.io.File;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Iterator;
import java.util.Objects;

public class BinarySpectrumTest {

    Iterator<Spectrum> specIt = null;
    IBinarySpectrum binarySpectrum = new BinarySpectrum(345567, 2, new BinaryPeak[6]);


    @Before
    public void setUp() throws JMzReaderException, URISyntaxException {

        URI uri = Objects.requireNonNull(BinarySpectrum.class.getClassLoader().getResource("single-spectra.mgf")).toURI();
        MgfFile mgfFile = new MgfFile(new File(uri));
        specIt = mgfFile.getSpectrumIterator();

    }

    @Test
    public void readBinarySpectrum() {

        Spectrum spectrum = specIt.next();
        BinarySpectrum binarySpectrum = new BinarySpectrum((int)spectrum.getPrecursorMZ().doubleValue(), spectrum.getPrecursorCharge(), new BinaryPeak[0]);
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



}
