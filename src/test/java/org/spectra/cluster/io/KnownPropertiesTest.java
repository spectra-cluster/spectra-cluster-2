package org.spectra.cluster.io;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.spectra.cluster.io.properties.KnownProperties;

import java.util.Properties;

/**
 * This code is licensed under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 *
 * Some tests for the {@link KnownProperties} class.
 *
 * @author ypriverol on 25/08/2018.
 */
public class KnownPropertiesTest {

    private Properties properties;

    @Before
    public void setUp(){
        properties = new Properties();
        properties.put("identifiedPeptide", "true");
        properties.put("accession", "PQH1111");
    }

    @Test
    public void toMGFLine() {
        String resultString = KnownProperties.toMGFLine("INSTRUMENT", "LTQ");
        Assert.assertTrue(resultString.equalsIgnoreCase("USER12=INSTRUMENT=LTQ"));

    }

    @Test
    public void addMGFProperties() {
        KnownProperties.addMGFProperties(properties, "INSTRUMENT=LTQ");
        Assert.assertEquals(3, properties.size());
    }
}