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
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * This code is licensed under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * ==Overview==
 *
 * @author ypriverol on 21/08/2018.
 */
public class CumulativeIntensityNormalizerTest {

    private Spectrum testSpectrum;

    @Before
    public void setUp() throws Exception {
        URI uri = Objects.requireNonNull(BinarySpectrum.class.getClassLoader().getResource("single-spectra.mgf")).toURI();
        MgfFile mgfFile = new MgfFile(new File(uri));
        Iterator<Spectrum> specIt = mgfFile.getSpectrumIterator();

        if (!specIt.hasNext()) {
            throw new Exception("Failed to load spectra");
        }

        testSpectrum = specIt.next();
    }

    @Test
    public void binDoubles() {
        CumulativeIntensityNormalizer normalizer = new CumulativeIntensityNormalizer(100000);
        int[] intensityPeaks = normalizer.binDoubles(testSpectrum.getPeakList().entrySet().stream().map(Map.Entry::getValue).collect(Collectors.toList()));
        Assert.assertEquals(intensityPeaks.length, testSpectrum.getPeakList().size());
    }

    @Test
    public void CummulativeVslogVsIntegerNormalizer() {
        LogNormalizer logNormalizer = new LogNormalizer(100000);
        BasicIntegerNormalizer integerNormalizer  = new BasicIntegerNormalizer(100000);
        CumulativeIntensityNormalizer cumulativeIntensityNormalizer = new CumulativeIntensityNormalizer(100000);
        int[] logPeaks = logNormalizer.binDoubles(testSpectrum.getPeakList().entrySet().stream().map(Map.Entry::getValue).collect(Collectors.toList()));
        int[] intPeaks = integerNormalizer.binDoubles(testSpectrum.getPeakList().entrySet().stream().map(Map.Entry::getValue).collect(Collectors.toList()));
        int[] cumulativePeaks = cumulativeIntensityNormalizer.binDoubles(testSpectrum.getPeakList().entrySet().stream().map(Map.Entry::getValue).collect(Collectors.toList()));

        Variance variance = new Variance();
        double logVariance = variance.evaluate(Arrays.stream(logPeaks).asDoubleStream().toArray());
        double intVariance = variance.evaluate(Arrays.stream(intPeaks).asDoubleStream().toArray());
        double cummulativeVariance = variance.evaluate(Arrays.stream(cumulativePeaks).asDoubleStream().toArray());
        Assert.assertTrue( logVariance < intVariance);
        Assert.assertTrue(cummulativeVariance < logVariance);

    }
}