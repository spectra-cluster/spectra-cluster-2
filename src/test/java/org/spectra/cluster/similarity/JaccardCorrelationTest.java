package org.spectra.cluster.similarity;


import lombok.extern.slf4j.Slf4j;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.spectra.cluster.model.spectra.BinarySpectrum;
import org.spectra.cluster.normalizer.BasicIntegerNormalizer;
import org.spectra.cluster.normalizer.FactoryNormalizer;
import org.spectra.cluster.normalizer.LSHBinner;
import org.spectra.cluster.normalizer.SequestBinner;
import uk.ac.ebi.pride.tools.jmzreader.JMzReaderException;
import uk.ac.ebi.pride.tools.jmzreader.model.Spectrum;
import uk.ac.ebi.pride.tools.mgf_parser.MgfFile;

import java.io.File;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
public class JaccardCorrelationTest {

    BinarySpectrum binarySpectrum1 = null;
    BinarySpectrum binarySpectrum2 = null;

    Spectrum spectrum1 = null;
    Spectrum spectrum2 = null;

    Iterator<Spectrum> specIt;
    FactoryNormalizer binnerNormalizer = new FactoryNormalizer(new SequestBinner(), new BasicIntegerNormalizer());


    @Before
    public void setUp() throws Exception {

        URI uri = Objects.requireNonNull(BinarySpectrum.class.getClassLoader().getResource("single-spectra.mgf")).toURI();
        MgfFile mgfFile = new MgfFile(new File(uri));

        specIt = mgfFile.getSpectrumIterator();

        spectrum1 = specIt.next();

        binarySpectrum1 = new BinarySpectrum((int)spectrum1.getPrecursorMZ().doubleValue(), spectrum1.getPrecursorCharge(),
                binnerNormalizer.normalizePeaks(spectrum1.getPeakList()));

        spectrum2 = specIt.next();

        binarySpectrum2 = new BinarySpectrum((int)spectrum2.getPrecursorMZ().doubleValue(), spectrum2.getPrecursorCharge(),
                binnerNormalizer.normalizePeaks(spectrum1.getPeakList()));

        /** Read the Spectra from similar files **/
        uri = Objects.requireNonNull(BinarySpectrum.class.getClassLoader().getResource("most_similar_1.mgf")).toURI();
        mgfFile = new MgfFile(new File(uri));
        specIt = mgfFile.getSpectrumIterator();


    }

    @Test
    public void jaccardCorrelationTest() {

        JaccardCorrelation correlation = new JaccardCorrelation();

        double similarity = correlation.correlation(binarySpectrum1.getMzVector(),
                binarySpectrum2.getMzVector());
        Assert.assertTrue(similarity - 0.99f < 0.1);
    }

    @Test
    public void lshBinnerJaccardCorrelationTest(){
        LSHBinner lshBinner = LSHBinner.getInstance();
        JaccardCorrelation correlation = new JaccardCorrelation();

        int[] vector1 = lshBinner.getKernels(binarySpectrum1.getMzVector());
        int[] vector2 = lshBinner.getKernels(binarySpectrum2.getMzVector());

        double similarity = correlation.correlation(vector1, vector2);
        Assert.assertTrue(similarity - 0.99f < 0.1);

    }


}
