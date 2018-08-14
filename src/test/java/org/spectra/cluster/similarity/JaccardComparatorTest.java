package org.spectra.cluster.similarity;


import cern.colt.matrix.impl.SparseDoubleMatrix1D;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.spectra.cluster.model.spectra.BinarySpectrum;
import org.spectra.cluster.normalizer.MzPeaksBinnedNormalizer;
import uk.ac.ebi.pride.tools.jmzreader.JMzReaderException;
import uk.ac.ebi.pride.tools.jmzreader.model.Spectrum;
import uk.ac.ebi.pride.tools.mgf_parser.MgfFile;

import java.io.File;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Arrays;
import java.util.Iterator;
import java.util.stream.Collectors;

public class JaccardComparatorTest {

    BinarySpectrum binarySpectrum1 = null;
    BinarySpectrum binarySpectrum2 = null;

    Spectrum spectrum1 = null;
    Spectrum spectrum2 = null;

    Iterator<Spectrum> specIt = null;

    @Before
    public void setUp() throws JMzReaderException, URISyntaxException {

        URI uri = BinarySpectrum.class.getClassLoader().getResource("single-spectra.mgf").toURI();
        MgfFile mgfFile = new MgfFile(new File(uri));
        specIt = mgfFile.getSpectrumIterator();

        spectrum1 = specIt.next();

        binarySpectrum1 = BinarySpectrum.builder()
                .precursorMZ((int)spectrum1.getPrecursorMZ().doubleValue())
                .precursorCharge(spectrum1.getPrecursorCharge())
                .mzPeaksVector(MzPeaksBinnedNormalizer.binnedHighResMzPeaks(spectrum1.getPeakList().entrySet().stream().map(x->x.getKey()).collect(Collectors.toList())))
                .intensityPeaksVector(MzPeaksBinnedNormalizer.binnedHighResMzPeaks(spectrum1.getPeakList().entrySet().stream().map(x->x.getValue()).collect(Collectors.toList())))
                .build();

        spectrum2 = specIt.next();

        binarySpectrum2 = BinarySpectrum.builder()
                .precursorMZ((int)spectrum2.getPrecursorMZ().doubleValue())
                .precursorCharge(spectrum2.getPrecursorCharge())
                .mzPeaksVector(MzPeaksBinnedNormalizer.binnedHighResMzPeaks(spectrum2.getPeakList().entrySet().stream().map(x->x.getKey()).collect(Collectors.toList())))
                .intensityPeaksVector(MzPeaksBinnedNormalizer.binnedHighResMzPeaks(spectrum2.getPeakList().entrySet().stream().map(x->x.getValue()).collect(Collectors.toList())))
                .build();
    }

    @Test
    public void computeVectorJaccard() {

        double similarity = JaccardComparator.computeVectorJaccard(binarySpectrum1.getMzPeaksVector(), binarySpectrum2.getMzPeaksVector());
        Assert.assertTrue(similarity - 0.99f < 0.1);
    }


    @Test
    public void computeSparseMatrixJaccard() {

        SparseDoubleMatrix1D sparseMatrix1 = new SparseDoubleMatrix1D(Arrays.stream(MzPeaksBinnedNormalizer.binnedHighResMzPeaks(spectrum1.getPeakList().entrySet().stream().map(x->x.getKey()).collect(Collectors.toList()))).asDoubleStream().toArray());
        SparseDoubleMatrix1D sparseMatrix2 = new SparseDoubleMatrix1D(Arrays.stream(MzPeaksBinnedNormalizer.binnedHighResMzPeaks(spectrum2.getPeakList().entrySet().stream().map(x->x.getKey()).collect(Collectors.toList()))).asDoubleStream().toArray());

        double similarity = JaccardComparator.computeSparseMatrixJaccard(sparseMatrix1, sparseMatrix2);

        Assert.assertTrue(similarity - 0.91f < 0.1);

    }
}
