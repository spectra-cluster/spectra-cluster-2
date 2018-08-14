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

public class BinarySpectrumTest {

    Iterator<Spectrum> specIt = null;

    @Before
    public void setUp() throws JMzReaderException, URISyntaxException {

        URI uri = BinarySpectrum.class.getClassLoader().getResource("single-spectra.mgf").toURI();
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
        Assert.assertTrue(binarySpectrum.getPrecursorCharge() == 2);


    }
}
