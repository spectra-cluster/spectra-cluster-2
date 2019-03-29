package org.spectra.cluster.normalizer;

import cern.colt.bitvector.BitVector;
import org.bigbio.pgatk.io.common.Spectrum;
import org.bigbio.pgatk.io.mgf.MgfIterableReader;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.spectra.cluster.model.spectra.BinarySpectrum;

import java.io.File;
import java.net.URI;
import java.util.*;
import java.util.stream.Collectors;

public class MzValuesToBitVectorConverterTest {
    private Spectrum testSpectrum;

    @Before
    public void setUp() throws Exception {
        URI uri = Objects.requireNonNull(BinarySpectrum.class.getClassLoader().getResource("single-spectra.mgf")).toURI();
        MgfIterableReader mgfFile = new MgfIterableReader(new File(uri), true, false, true);

       testSpectrum = mgfFile.next();
    }

    @Test
    public void testManualBinning() {
        double[] valuesToBin = {0.12, 1.19, 10, 100.0, 2010};
        List<Double> doubleList = Arrays.stream(valuesToBin).boxed().collect(Collectors.toList());

        MzValuesToBitVectorConverter converter = new MzValuesToBitVectorConverter(new SequestBinner());

        BitVector vector = converter.mzToBitVector(doubleList);

        // make sure it worked
        Assert.assertEquals(2001, vector.size());
        Assert.assertEquals(4, vector.cardinality());
        Assert.assertTrue(vector.get(0));
        Assert.assertTrue(vector.get(2));
        Assert.assertTrue(vector.get(10));
        Assert.assertTrue(vector.get(100));
    }

    @Test
    public void testFirstSpectrum() {
        List<Double> doubleList = new ArrayList<>(testSpectrum.getPeakList().keySet());
        MzValuesToBitVectorConverter converter = new MzValuesToBitVectorConverter(new SequestBinner());

        BitVector vector = converter.mzToBitVector(doubleList);

        // 88 peaks
        Assert.assertEquals(87, vector.cardinality());

        int[] peakBins = {126, 249, 301, 380, 541, 1607};

        for (int bin : peakBins) {
            Assert.assertTrue(String.format("Bin %d not set", bin), vector.get(bin));
        }

        for (int bin = 1608; bin < 2000; bin++) {
            Assert.assertFalse(vector.get(bin));
        }

        for (int bin = 0; bin < 125; bin++) {
            Assert.assertFalse(vector.get(bin));
        }
    }

    @Test
    public void testMinMz() {
        List<Double> doubleList = new ArrayList<>(testSpectrum.getPeakList().keySet());
        MzValuesToBitVectorConverter converter = new MzValuesToBitVectorConverter(new SequestBinner(), 150.5,
                MzValuesToBitVectorConverter.DEFAULT_MAX_MZ);

        BitVector vector = converter.mzToBitVector(doubleList);

        // only 1850 bit
        Assert.assertEquals(1850, vector.size());

        // 83 peaks
        Assert.assertEquals(83, vector.cardinality());

        int[] orgBins = {248, 300, 379, 540, 1606};

        for (int bin : orgBins) {
            Assert.assertTrue(vector.get(bin - 150));
        }
    }
}
