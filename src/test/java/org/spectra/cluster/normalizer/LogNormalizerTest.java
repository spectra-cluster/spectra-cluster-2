package org.spectra.cluster.normalizer;

import org.apache.commons.math3.stat.descriptive.moment.Variance;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.spectra.cluster.model.spectra.BinarySpectrum;
import uk.ac.ebi.pride.tools.jmzreader.model.Spectrum;
import uk.ac.ebi.pride.tools.mgf_parser.MgfFile;

import java.io.File;
import java.net.URI;
import java.util.Arrays;
import java.util.Iterator;
import java.util.Map;
import java.util.stream.Collectors;

import static org.junit.Assert.*;

public class LogNormalizerTest {

    private Spectrum testSpectrum;

    @Before
    public void setUp() throws Exception {
        URI uri = BinarySpectrum.class.getClassLoader().getResource("single-spectra.mgf").toURI();
        MgfFile mgfFile = new MgfFile(new File(uri));
        Iterator<Spectrum> specIt = mgfFile.getSpectrumIterator();

        if (!specIt.hasNext()) {
            throw new Exception("Failed to load spectra");
        }

        testSpectrum = specIt.next();
    }

    @Test
    public void binDoubles() {
        LogNormalizer normalizer = new LogNormalizer(100000);
        int[] intensityPeaks = normalizer.binDoubles(testSpectrum.getPeakList().entrySet().stream().map(Map.Entry::getValue).collect(Collectors.toList()));
        Assert.assertTrue(intensityPeaks.length == testSpectrum.getPeakList().size());
    }

    @Test
    public void logVsIntegerNormalizer() {
        LogNormalizer logNormalizer = new LogNormalizer(100000);
        BasicIntegerNormalizer integerNormalizer  = new BasicIntegerNormalizer(100000);
        int[] logPeaks = logNormalizer.binDoubles(testSpectrum.getPeakList().entrySet().stream().map(Map.Entry::getValue).collect(Collectors.toList()));
        int[] intPeaks = integerNormalizer.binDoubles(testSpectrum.getPeakList().entrySet().stream().map(Map.Entry::getValue).collect(Collectors.toList()));

        Assert.assertTrue(logPeaks.length == testSpectrum.getPeakList().size());
        Assert.assertTrue(logPeaks.length == intPeaks.length);

        Variance variance = new Variance();
        double logVariance = variance.evaluate(Arrays.stream(logPeaks).asDoubleStream().toArray());
        double intVariance = variance.evaluate(Arrays.stream(intPeaks).asDoubleStream().toArray());
        Assert.assertTrue(logVariance < intVariance);

    }

}