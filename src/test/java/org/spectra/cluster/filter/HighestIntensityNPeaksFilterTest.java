package org.spectra.cluster.filter;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.spectra.cluster.io.MzSpectraReader;
import org.spectra.cluster.model.spectra.BinarySpectrum;
import org.spectra.cluster.model.spectra.IBinarySpectrum;
import org.spectra.cluster.normalizer.BasicIntegerNormalizer;
import org.spectra.cluster.normalizer.FactoryNormalizer;
import org.spectra.cluster.normalizer.SequestBinner;
import uk.ac.ebi.pride.tools.jmzreader.model.Spectrum;
import uk.ac.ebi.pride.tools.mgf_parser.MgfFile;

import java.io.File;
import java.net.URI;
import java.util.Iterator;
import java.util.Objects;

import static org.junit.Assert.*;

/**
 * This code is licensed under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * ==Overview==
 *
 * @author ypriverol on 16/08/2018.
 */
public class HighestIntensityNPeaksFilterTest {


    Iterator<IBinarySpectrum> specIt;

    @Before
    public void setUp() throws Exception {

        URI uri = Objects.requireNonNull(BinarySpectrum.class.getClassLoader().getResource("single-spectra.mgf")).toURI();
        MzSpectraReader parser = new MzSpectraReader(new File(uri), new SequestBinner(), new BasicIntegerNormalizer(), new BasicIntegerNormalizer(), new HighestPeakPerBinFilter());
        specIt = parser.readBinarySpectraIterator();

    }

    @Test
    public void filter() {
        HighestIntensityNPeaksFilter highestIntensityNPeaksFilter = new HighestIntensityNPeaksFilter(40);

        while(specIt.hasNext()){
            IBinarySpectrum spec = specIt.next();
            spec = highestIntensityNPeaksFilter.filter(spec);
            Assert.assertTrue(spec.getPeaks().length == 40);
            Assert.assertTrue(spec.getPeaks()[0].getIntensity() > spec.getPeaks()[39].getIntensity());
        }

    }

}