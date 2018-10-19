package org.spectra.cluster.util;

import lombok.Data;

import java.io.*;
import java.net.URISyntaxException;
import java.util.Properties;

/**
 * This code is licensed under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * ==Overview==
 *
 * @author ypriverol on 18/10/2018.
 */
@Data
public class DefaultParameters {

    private String binaryDirectory;
    private boolean reuseBinary;
    private boolean fastMode;
    private Integer clusterRounds;
    private boolean filterReportPeaks;
    private Integer numberHigherPeaks;
    private Double precursorIonTolerance;
    private Double fragmentIonTolerance;

    private Float thresholdStart;
    private Float thresholdEnd;


    public DefaultParameters(){

        try {
            Properties properties = readProperties();
            setProperties(properties);

        } catch (URISyntaxException e) {
            e.printStackTrace();
        }

    }

    private void setProperties(Properties properties) {
        if(properties.contains("precursor.tolerance"))
            this.precursorIonTolerance = Double.parseDouble(properties.getProperty("precursor.tolerance").trim());
        if(properties.contains("fragment.tolerance"))
            this.fragmentIonTolerance = Double.parseDouble(properties.getProperty("fragment.tolerance"));
        if(properties.contains("threshold.start"))
            this.thresholdStart =  Float.parseFloat(properties.getProperty("threshold.start"));
        if(properties.contains("threshold.end"))
            this.thresholdEnd   =  Float.parseFloat(properties.getProperty("threshold.end"));
        if(properties.contains("number.higher.peaks"))
            this.numberHigherPeaks = Integer.parseInt(properties.getProperty("number.higher.peaks"));
        if(properties.contains("cluster.rounds"))
            this.clusterRounds = Integer.parseInt(properties.getProperty("cluster.rounds"));
        if(properties.contains("binary.temp.directory"))
            this.binaryDirectory = properties.getProperty("binary.temp.directory");
        if(properties.contains("reuse.binary.files"))
            this.reuseBinary = Boolean.parseBoolean(properties.getProperty("reuse.binary.files"));
        if(properties.contains("cluster.fast.mode"))
            this.fastMode = Boolean.parseBoolean(properties.getProperty("cluster.fast.mode"));

        if(properties.contains("filters.remove.reporter.peaks"))
            this.filterReportPeaks = Boolean.parseBoolean(properties.getProperty("filters.remove.reporter.peaks"));
    }

    public Properties readProperties() throws URISyntaxException {
        Properties properties = new Properties();
        InputStream output;

        try {
            File propertiesFactoryBean = new File(DefaultParameters.class.getClassLoader().getResource("application.properties").toURI());
            output = new FileInputStream(propertiesFactoryBean);
            properties.load(output);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return properties;
    }

    public void mergeParameters(String configFile) throws IOException {
        File propertiesFactoryBean = new File(configFile);
        Properties newProperties = new Properties();
        InputStream output = new FileInputStream(propertiesFactoryBean);
        newProperties.load(output);
        setProperties(newProperties);
    }
}
