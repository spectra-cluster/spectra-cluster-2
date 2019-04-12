package org.spectra.cluster.normalizer;

import org.apache.commons.math3.stat.descriptive.moment.Variance;
import org.bigbio.pgatk.io.common.Spectrum;
import org.bigbio.pgatk.io.mgf.MgfIterableReader;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.spectra.cluster.model.spectra.BinarySpectrum;

import java.io.File;
import java.net.URI;
import java.util.Arrays;
import java.util.Iterator;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

public class LogNormalizerTest {

    private Spectrum testSpectrum;

    @Before
    public void setUp() throws Exception {
        URI uri = Objects.requireNonNull(BinarySpectrum.class.getClassLoader().getResource("single-spectra.mgf")).toURI();
        MgfIterableReader mgfFile = new MgfIterableReader(new File(uri), true, false, true);
        if(mgfFile.hasNext())
            testSpectrum = mgfFile.next();
    }

    @Test
    public void binDoubles() {
        LogNormalizer normalizer = new LogNormalizer(100000);
        int[] intensityPeaks = normalizer.binDoubles(testSpectrum.getPeakList().entrySet().stream().map(Map.Entry::getValue).collect(Collectors.toList()));
        Assert.assertEquals(intensityPeaks.length, testSpectrum.getPeakList().size());
    }

    @Test
    public void logVsIntegerNormalizer() {
        LogNormalizer logNormalizer = new LogNormalizer(100000);
        BasicIntegerNormalizer integerNormalizer  = new BasicIntegerNormalizer(100000);
        int[] logPeaks = logNormalizer.binDoubles(testSpectrum.getPeakList().entrySet().stream().map(Map.Entry::getValue).collect(Collectors.toList()));
        int[] intPeaks = integerNormalizer.binDoubles(testSpectrum.getPeakList().entrySet().stream().map(Map.Entry::getValue).collect(Collectors.toList()));

        Assert.assertEquals(logPeaks.length, testSpectrum.getPeakList().size());
        Assert.assertEquals(logPeaks.length, intPeaks.length);

        Variance variance = new Variance();
        double logVariance = variance.evaluate(Arrays.stream(logPeaks).asDoubleStream().toArray());
        double intVariance = variance.evaluate(Arrays.stream(intPeaks).asDoubleStream().toArray());
        Assert.assertTrue(logVariance < intVariance);

    }

}