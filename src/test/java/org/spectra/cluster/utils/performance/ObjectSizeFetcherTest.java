package org.spectra.cluster.utils.performance;

import org.ehcache.sizeof.SizeOf;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.spectra.cluster.model.spectra.BinaryPeak;
import org.spectra.cluster.model.spectra.BinarySpectrum;
import org.spectra.cluster.model.spectra.IBinarySpectrum;
import org.spectra.cluster.normalizer.BasicIntegerNormalizer;
import org.spectra.cluster.normalizer.FactoryNormalizer;
import org.spectra.cluster.normalizer.SequestBinner;
import uk.ac.ebi.pride.tools.jmzreader.JMzReaderException;
import uk.ac.ebi.pride.tools.jmzreader.model.Spectrum;
import uk.ac.ebi.pride.tools.mgf_parser.MgfFile;

import java.io.File;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;

import static org.junit.Assert.*;

/**
 * This Test check the size of an {@link IBinarySpectrum} List and compare with a List of {@link Spectrum}. The test
 * expect that the size is half of the original size of an spectrum.
 *
 * @author ypriverol
 */
public class ObjectSizeFetcherTest {

    List<Spectrum> spectrumList;
    List<BinarySpectrum> binarySpectrumList;


    @Before
    public void setUp() throws Exception {

        URI uri = Objects.requireNonNull(BinarySpectrum.class.getClassLoader().getResource("single-spectra.mgf")).toURI();
        MgfFile mgfFile = new MgfFile(new File(uri));
        Iterator<Spectrum> specIt = mgfFile.getSpectrumIterator();
        spectrumList = new ArrayList<>();
        binarySpectrumList = new ArrayList<>();
        BasicIntegerNormalizer precursorNormalizer = new BasicIntegerNormalizer();
        FactoryNormalizer factory = new FactoryNormalizer(new SequestBinner(), new BasicIntegerNormalizer());
        while(specIt.hasNext()){
            Spectrum spec = specIt.next();
            spectrumList.add(spec);
            binarySpectrumList.add(BinarySpectrum.builder()
                    .precursorCharge(spec.getPrecursorCharge())
                    .precursorMZ((precursorNormalizer)
                            .binValue(spec.getPrecursorMZ()))
                    .peaks(factory.normalizePeaks(spec.getPeakList()))
                    .build());
        }
    }

    @Test
    public void getObjectSize() {
        SizeOf sizeOf = SizeOf.newInstance();
        long size = sizeOf.deepSizeOf(spectrumList);
        long binarySize = sizeOf.deepSizeOf(binarySpectrumList);

        Assert.assertTrue(binarySize * 2 < size);

    }
}