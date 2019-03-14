package org.spectra.cluster.utils.performance;

import org.ehcache.sizeof.SizeOf;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.spectra.cluster.engine.GreedyClusteringEngine;
import org.spectra.cluster.model.spectra.BinarySpectrum;
import org.spectra.cluster.model.spectra.IBinarySpectrum;
import org.spectra.cluster.normalizer.BasicIntegerNormalizer;
import org.spectra.cluster.normalizer.FactoryNormalizer;
import org.spectra.cluster.normalizer.SequestBinner;
import uk.ac.ebi.pride.tools.jmzreader.model.Spectrum;
import uk.ac.ebi.pride.tools.mgf_parser.MgfFile;

import java.io.File;
import java.net.URI;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;

/**
 * This Test check the size of an {@link IBinarySpectrum} List and compare with a List of {@link Spectrum}. The test
 * expect that the size is half of the original size of an spectrum.
 *
 * @author ypriverol
 */
public class ObjectSizeFetcherTest {

    List<Spectrum> spectrumList;
    BinarySpectrum[] binarySpectrumList;


    @Before
    public void setUp() throws Exception {

        URI uri = Objects.requireNonNull(BinarySpectrum.class.getClassLoader().getResource("single-spectra.mgf")).toURI();
        MgfFile mgfFile = new MgfFile(new File(uri));
        Iterator<Spectrum> specIt = mgfFile.getSpectrumIterator();
        spectrumList = new ArrayList<>();
        binarySpectrumList = new BinarySpectrum[2];
        BasicIntegerNormalizer precursorNormalizer = new BasicIntegerNormalizer();
        FactoryNormalizer factory = new FactoryNormalizer(new SequestBinner(), new BasicIntegerNormalizer());
        int count = 0;
        while(specIt.hasNext()){
            Spectrum spec = specIt.next();
            spectrumList.add(spec);
            binarySpectrumList[count] = new BinarySpectrum(
                    (precursorNormalizer).binValue(spec.getPrecursorMZ()),
                    spec.getPrecursorCharge(),
                    factory.normalizePeaks(spec.getPeakList()),
                    GreedyClusteringEngine.COMPARISON_FILTER);
            count++;
        }
    }

    @Test
    public void getObjectSize() {
        SizeOf sizeOf = SizeOf.newInstance();
        long size = sizeOf.deepSizeOf(spectrumList);
        long binarySize = sizeOf.deepSizeOf(binarySpectrumList);

        Assert.assertTrue(binarySize * 2.9 < size);

    }

    @Test
    public void getExpectedObjectSizeMillion() {

        List<Spectrum> millionSpectra = new ArrayList<>(1000000);
        BinarySpectrum[] millionBinarySpectrum = new BinarySpectrum[1000000];
        for(int i= 0; i < 500000; i++){
            millionSpectra.add(spectrumList.get(0));
            millionSpectra.add(spectrumList.get(1));
            millionBinarySpectrum[i] = binarySpectrumList[0];
            millionBinarySpectrum[i+1] = binarySpectrumList[1];
        }

        SizeOf sizeOf = SizeOf.newInstance();
        long size = sizeOf.deepSizeOf(millionSpectra);
        long binarySize = sizeOf.deepSizeOf(millionBinarySpectrum);

        Assert.assertTrue(binarySize * 100 < size);

    }
}