package org.spectra.cluster.io;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.spectra.cluster.model.spectra.BinaryPeak;
import org.spectra.cluster.model.spectra.BinarySpectrum;
import org.spectra.cluster.model.spectra.IBinarySpectrum;

import java.io.File;
import java.net.URI;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
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
            Assert.assertTrue(binaryIter.next().getCopyIntensityVector().length > 0);
            count++;
        }
        Assert.assertEquals(2, count);


    }

    @Test
    public void testNoNullSpectra() throws Exception {
        File testFile = new File(Objects.requireNonNull(MzSpectraReaderTest.class.getClassLoader().getResource("same_sequence_cluster.mgf")).toURI());
        MzSpectraReader reader = new MzSpectraReader(testFile);
        Iterator<IBinarySpectrum> iterator = reader.readBinarySpectraIterator();

        while (iterator.hasNext()) {
            IBinarySpectrum spectrum = iterator.next();

            // make sure there are no null peaks
            BinaryPeak[] peaks = spectrum.getPeaks();

            for (BinaryPeak peak : peaks) {
                Assert.assertNotNull(peak);
            }
        }
    }

    @Test
    public void testPropertyLoading() throws Exception {
        File testFile = new File(MzSpectraReaderTest.class.getClassLoader().getResource("same_sequence_cluster.mgf").toURI());
        MzSpectraReader reader = new MzSpectraReader(testFile);

        IPropertyStorage storage = new InMemoryPropertyStorage();

        Iterator<IBinarySpectrum> iterator = reader.readBinarySpectraIterator(storage);
        List<String> specIds = new ArrayList<>();

        while (iterator.hasNext()) {
            IBinarySpectrum spectrum = iterator.next();
            specIds.add(spectrum.getUUI());
        }

        Assert.assertEquals(158, specIds.size());
        int nIdentified = 0;

        // test the properties exist
        for (String id : specIds) {
            if (storage.getProperty(id, "Sequence") != null) {
                nIdentified++;
            }

            Assert.assertNotNull("Missing retention time for " + id, storage.getProperty(id, "retention time"));
            Assert.assertNotNull("Missing title for " + id, storage.getProperty(id, "spectrum title"));
        }

        Assert.assertEquals(136, nIdentified);

        Assert.assertEquals(7, storage.getAvailableProperties().size());
    }
}