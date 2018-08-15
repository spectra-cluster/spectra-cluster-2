package org.spectra.cluster.similarity;


import cern.colt.matrix.impl.SparseDoubleMatrix1D;
import info.debatty.java.lsh.MinHash;
import lombok.extern.slf4j.Slf4j;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.spectra.cluster.model.spectra.BinarySpectrum;
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
    SequestBinner binnerNormalizer = new SequestBinner();


    @Before
    public void setUp() throws JMzReaderException, URISyntaxException {

        URI uri = Objects.requireNonNull(BinarySpectrum.class.getClassLoader().getResource("single-spectra.mgf")).toURI();
        MgfFile mgfFile = new MgfFile(new File(uri));

        specIt = mgfFile.getSpectrumIterator();

        spectrum1 = specIt.next();

        binarySpectrum1 = BinarySpectrum.builder()
                .precursorMZ((int)spectrum1.getPrecursorMZ().doubleValue())
                .precursorCharge(spectrum1.getPrecursorCharge())
                .mzPeaksVector(binnerNormalizer.binDoubles(spectrum1.getPeakList().entrySet().stream().map(Map.Entry::getKey).collect(Collectors.toList())))
                .intensityPeaksVector(binnerNormalizer.binDoubles(spectrum1.getPeakList().entrySet().stream().map(Map.Entry::getValue).collect(Collectors.toList())))
                .build();

        spectrum2 = specIt.next();

        binarySpectrum2 = BinarySpectrum.builder()
                .precursorMZ((int)spectrum2.getPrecursorMZ().doubleValue())
                .precursorCharge(spectrum2.getPrecursorCharge())
                .mzPeaksVector(binnerNormalizer.binDoubles(spectrum2.getPeakList().entrySet().stream().map(Map.Entry::getKey).collect(Collectors.toList())))
                .intensityPeaksVector(binnerNormalizer.binDoubles(spectrum2.getPeakList().entrySet().stream().map(Map.Entry::getValue).collect(Collectors.toList())))
                .build();

        /** Read the Spectra from similar files **/
        uri = Objects.requireNonNull(BinarySpectrum.class.getClassLoader().getResource("most_similar_1.mgf")).toURI();
        mgfFile = new MgfFile(new File(uri));
        specIt = mgfFile.getSpectrumIterator();


    }

    @Test
    public void jaccardCorrelationTest() {

        JaccardCorrelation correlation = new JaccardCorrelation();

        double similarity = correlation.correlation(binarySpectrum1.getMzPeaksVector(), binarySpectrum2.getMzPeaksVector());
        Assert.assertTrue(similarity - 0.99f < 0.1);
    }

    @Test
    public void lshBinnerJaccardCorrelationTest(){
        LSHBinner lshBinner = new LSHBinner();
        JaccardCorrelation correlation = new JaccardCorrelation();

        int[] vector1 = lshBinner.binVector(binarySpectrum1.getMzPeaksVector());
        int[] vector2 = lshBinner.binVector(binarySpectrum2.getMzPeaksVector());

        double similarity = correlation.correlation(vector1, vector2);
        Assert.assertTrue(similarity - 0.99f < 0.1);

    }


}
