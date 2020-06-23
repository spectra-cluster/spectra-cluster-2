package org.spectra.cluster.normalizer;

import io.github.bigbio.pgatk.io.common.PgatkIOException;
import io.github.bigbio.pgatk.io.common.spectra.Spectrum;
import io.github.bigbio.pgatk.io.mgf.MgfIterableReader;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.spectra.cluster.model.spectra.BinarySpectrum;

import java.io.File;
import java.net.URI;
import java.net.URISyntaxException;
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
 * @author ypriverol on 14/08/2018.
 */
public class LSHBinnerTest {
    private MgfIterableReader mgfFile;

    @Before
    public void setUp() throws URISyntaxException, PgatkIOException {
        URI uri = Objects.requireNonNull(BinarySpectrum.class.getClassLoader().getResource("single-spectra.mgf")).toURI();
        mgfFile = new MgfIterableReader(new File(uri), true, false, true);
    }

    @Test
    public void LSHBinner() {
        if (mgfFile.hasNext()) {
            Spectrum spectrum = mgfFile.next();
            LSHBinner kernelsHLS = LSHBinner.getInstance();

            TideBinner binner = new TideBinner();

            int[] values = kernelsHLS.getKernels(binner.binDoubles(spectrum.getPeakList().entrySet().stream().map(Map.Entry::getKey).collect(Collectors.toList())));
            Assert.assertEquals(10, values.length);
        }
    }



}