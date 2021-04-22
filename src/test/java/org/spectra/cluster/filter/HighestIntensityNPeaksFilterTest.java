package org.spectra.cluster.filter;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.spectra.cluster.filter.binaryspectrum.HighestIntensityNPeaksFunction;
import org.spectra.cluster.io.spectra.MzSpectraReader;
import org.spectra.cluster.model.spectra.BinarySpectrum;
import org.spectra.cluster.model.spectra.IBinarySpectrum;
import org.spectra.cluster.util.ClusteringParameters;

import java.io.File;
import java.net.URI;
import java.util.Iterator;
import java.util.Objects;

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
        MzSpectraReader parser = new MzSpectraReader(new ClusteringParameters(), new File(uri));
        specIt = parser.readBinarySpectraIterator();

    }

    @Test
    public void filter() {
        HighestIntensityNPeaksFunction highestIntensityNPeaksFilter = new HighestIntensityNPeaksFunction(40);

        while(specIt.hasNext()){
            IBinarySpectrum spec = specIt.next();
            spec = highestIntensityNPeaksFilter.apply(spec);
            Assert.assertEquals(40, spec.getPeaks().length);
            Assert.assertTrue(spec.getPeaks()[0].getMz() < spec.getPeaks()[39].getMz());
        }

    }




}