package org.spectra.cluster.normalizer;

import cern.colt.bitvector.BitVector;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.spectra.cluster.model.spectra.BinarySpectrum;
import uk.ac.ebi.pride.tools.jmzreader.model.Spectrum;
import uk.ac.ebi.pride.tools.mgf_parser.MgfFile;

import java.io.File;
import java.net.URI;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Collectors;

public class MzValuesToBitVectorConverterTest {
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
    public void testManualBinning() throws Exception {
        double[] valuesToBin = {0.12, 1.19, 10, 100.0, 2010};
        List<Double> doubleList = Arrays.stream(valuesToBin).boxed().collect(Collectors.toList());

        MzValuesToBitVectorConverter converter = new MzValuesToBitVectorConverter(new SequestBinner());

        BitVector vector = converter.mzToBitVector(doubleList);

        // make sure it worked
        Assert.assertEquals(2000, vector.size());
        Assert.assertEquals(4, vector.cardinality());
        Assert.assertTrue(vector.get(0));
        Assert.assertTrue(vector.get(1));
        Assert.assertTrue(vector.get(9));
        Assert.assertTrue(vector.get(99));
    }

    @Test
    public void testFirstSpectrum() throws Exception {
        List<Double> doubleList = new ArrayList<>(testSpectrum.getPeakList().keySet());
        MzValuesToBitVectorConverter converter = new MzValuesToBitVectorConverter(new SequestBinner());

        BitVector vector = converter.mzToBitVector(doubleList);

        // 88 peaks
        Assert.assertEquals(87, vector.cardinality());

        int[] peakBins = {125, 248, 300, 379, 540, 1606};

        for (int bin : peakBins) {
            Assert.assertTrue(String.format("Bin %d not set", bin), vector.get(bin));
        }

        for (int bin = 1607; bin < 2000; bin++) {
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

    @Test
    public void testBenchmark() {
        List<Double> doubleList = new ArrayList<>(testSpectrum.getPeakList().keySet());
        MzValuesToBitVectorConverter converter = new MzValuesToBitVectorConverter(new SequestBinner());

        // create 1M vectors
        int nInstances = 1000000;

        // test how long it takes for 1M vectors
        long timeStart = System.currentTimeMillis();

        List<BitVector> vectors = new ArrayList<>(nInstances);

        for (int i = 0; i < nInstances; i++) {
            BitVector v = converter.mzToBitVector(doubleList);
            vectors.add(v);
        }

        // get the memory now
        long timeUsed = System.currentTimeMillis() - timeStart;

        // must take under 4 sec
        Assert.assertTrue(String.format("%d mil sec used", timeUsed), timeUsed < 7000);
    }
}
