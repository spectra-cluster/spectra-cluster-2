package org.spectra.cluster.io;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.spectra.cluster.model.spectra.BinarySpectrum;
import org.spectra.cluster.model.spectra.IBinarySpectrum;

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
 * @author ypriverol on 14/08/2018.
 */
public class MzSpectraReaderTest {

    MzSpectraReader spectraReader;

    @Before
    public void setUp() throws Exception {

        URI uri = Objects.requireNonNull(BinarySpectrum.class.getClassLoader().getResource("single-spectra.mgf")).toURI();
        File mgfFile = new File(uri);
        spectraReader = new MzSpectraReader(mgfFile);
    }

    @Test
    public void readBinarySpectraIterator() {

        Iterator<IBinarySpectrum> binaryIter = spectraReader.readBinarySpectraIterator();
        int count = 0;
        while(binaryIter.hasNext()){
            Assert.assertTrue(binaryIter.next().getIntensityVector().length > 0);
            count++;
        }
        Assert.assertEquals(2, count);


    }
}