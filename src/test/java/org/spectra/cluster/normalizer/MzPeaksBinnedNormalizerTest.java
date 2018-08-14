package org.spectra.cluster.normalizer;


import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.spectra.cluster.model.spectra.BinarySpectrum;
import uk.ac.ebi.pride.tools.jmzreader.JMzReaderException;
import uk.ac.ebi.pride.tools.jmzreader.model.Spectrum;
import uk.ac.ebi.pride.tools.mgf_parser.MgfFile;

import java.io.File;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Iterator;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

public class MzPeaksBinnedNormalizerTest {

    Iterator<Spectrum> specIt = null;

    @Before
    public void setUp() throws JMzReaderException, URISyntaxException {

        URI uri = Objects.requireNonNull(BinarySpectrum.class.getClassLoader().getResource("single-spectra.mgf")).toURI();
        MgfFile mgfFile = new MgfFile(new File(uri));
        specIt = mgfFile.getSpectrumIterator();

    }

    @Test
    public void binnedHighResMzPeaks() {

        Spectrum spectrum = specIt.next();

        MzPeaksBinnedNormalizer binner = new MzPeaksBinnedNormalizer();

        int[] values = binner.binDoubles(spectrum.getPeakList().entrySet().stream().map(Map.Entry::getKey).collect(Collectors.toList()));
        Assert.assertEquals(4930, values.length);

    }
}
