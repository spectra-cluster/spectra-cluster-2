package org.spectra.cluster.normalizer;


import org.bigbio.pgatk.io.common.PgatkIOException;
import org.bigbio.pgatk.io.common.Spectrum;
import org.bigbio.pgatk.io.mgf.MgfIterableReader;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.spectra.cluster.model.spectra.BinarySpectrum;

import java.io.File;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Iterator;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

public class BasicMzBinnerTest {


    private MgfIterableReader mgfFile;

    @Before
    public void setUp() throws URISyntaxException, PgatkIOException {

        URI uri = Objects.requireNonNull(BinarySpectrum.class.getClassLoader().getResource("single-spectra.mgf")).toURI();
       mgfFile = new MgfIterableReader(new File(uri), true, false, true);


    }

    @Test
    public void binnedHighResMzPeaks() {

        if(mgfFile.hasNext()){
            Spectrum spectrum = mgfFile.next();
            BasicMzBinner binner = new BasicMzBinner();

            int[] values = binner.binDoubles(spectrum.getPeakList().entrySet().stream().map(Map.Entry::getKey).collect(Collectors.toList()));
            Assert.assertEquals(88, values.length);
        }



    }
}
