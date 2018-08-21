package org.spectra.cluster.filter.rawpeaks;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

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
 * Test to move ION reporters from Mass Spectra.
 *
 * @author ypriverol on 21/08/2018.
 */
public class RemoveReporterIonPeaksFunctionTest {

    Map<Double, Double> peaks;

    @Before
    public void setUp(){
        peaks = new HashMap<>();
        // 130.1348 130.1410 131.1380
        peaks.put(130.1348, 1.0);
        peaks.put(130.1410, 3.0);
        peaks.put(131.1380, 6.7);
    }

    @Test
    public void apply() {

        RemoveReporterIonPeaksFunction reporterRemoval = new RemoveReporterIonPeaksFunction(0.5, RemoveReporterIonPeaksFunction.REPORTER_TYPE.TMT);
        Map<Double, Double> resultPeaks = reporterRemoval.apply(peaks);
        Assert.assertTrue(resultPeaks.isEmpty());
    }
}