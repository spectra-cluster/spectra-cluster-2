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
    IBinarySpectrum binarySpectrum = BinarySpectrum.builder()
            .precursorMZ(345567)
            .precursorCharge(2)
            .peaks(new BinaryPeak[6]).build();


    @Before
    public void setUp() throws JMzReaderException, URISyntaxException {

        URI uri = Objects.requireNonNull(BinarySpectrum.class.getClassLoader().getResource("single-spectra.mgf")).toURI();
        MgfFile mgfFile = new MgfFile(new File(uri));
        specIt = mgfFile.getSpectrumIterator();

    }

    @Test
    public void readBinarySpectrum() {

        Spectrum spectrum = specIt.next();
        BinarySpectrum binarySpectrum = BinarySpectrum.builder()
                .precursorMZ((int)spectrum.getPrecursorMZ().doubleValue())
                .precursorCharge(spectrum.getPrecursorCharge())
                .build();
        Assert.assertEquals(2, binarySpectrum.getPrecursorCharge());

    }

    @Test
    public void getUUI() {
        Assert.assertTrue(!binarySpectrum.getUUI().isEmpty());
    }

    @Test
    public void getPeaks() {
        Assert.assertTrue(binarySpectrum.getPeaks().length == 6);
    }

    @Test
    public void getNumberOfPeaks() {
        Assert.assertTrue(binarySpectrum.getPeaks().length == 6);
    }

    @Test
    public void getPrecursorCharge() {
        Assert.assertTrue(binarySpectrum.getPrecursorCharge() == 2);
    }

    @Test
    public void getPrecursorMz() {
        Assert.assertTrue(binarySpectrum.getPrecursorMz() == 345567);
    }



}
