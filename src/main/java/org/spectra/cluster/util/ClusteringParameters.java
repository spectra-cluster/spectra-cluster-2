package org.spectra.cluster.util;

import lombok.Data;
import org.apache.commons.cli.CommandLine;
import org.spectra.cluster.cdf.SpectraPerBinNumberComparisonAssessor;
import org.spectra.cluster.engine.GreedyClusteringEngine;
import org.spectra.cluster.filter.rawpeaks.*;
import org.spectra.cluster.model.cluster.ICluster;
import org.spectra.cluster.normalizer.BasicIntegerNormalizer;
import org.spectra.cluster.normalizer.HighResolutionMzBinner;
import org.spectra.cluster.normalizer.IMzBinner;
import org.spectra.cluster.normalizer.TideBinner;
import org.spectra.cluster.predicates.IComparisonPredicate;
import org.spectra.cluster.predicates.SameChargePredicate;
import org.spectra.cluster.predicates.ShareNComparisonPeaksPredicate;
import org.spectra.cluster.similarity.CombinedFisherIntensityTest;
import org.spectra.cluster.tools.CliOptions;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
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
public class ClusteringParameters {

    private String binaryDirectory;
    private boolean reuseBinary;
    private boolean fastMode;
    private Integer clusterRounds;
    private boolean filterReportPeaks;
    private Integer numberHigherPeaks;
    private Double precursorIonTolerance;
    private String fragmentIonPrecision;
    private boolean ignoreCharge;

    private Float thresholdStart;
    private Float thresholdEnd;
    private int nInitiallySharedPeaks;
    private int minNumberOfComparisons;

    private File outputFile;
    private boolean outputMsp;

    private int nThreads;


    public ClusteringParameters(){

        try {
            Properties properties = readProperties();
            setProperties(properties);

        } catch (URISyntaxException e) {
            e.printStackTrace();
        }

    }

    private void setProperties(Properties properties) {
        if(properties.containsKey("precursor.tolerance"))
            this.precursorIonTolerance = Double.parseDouble(properties.getProperty("precursor.tolerance").trim());
        if(properties.containsKey("fragment.precision"))
            this.fragmentIonPrecision = properties.getProperty("fragment.precision");
        if(properties.containsKey("n.threads"))
            this.nThreads = Integer.parseInt(properties.getProperty("n.threads"));
        if(properties.containsKey("threshold.start"))
            this.thresholdStart =  Float.parseFloat(properties.getProperty("threshold.start"));
        if(properties.containsKey("threshold.end"))
            this.thresholdEnd   =  Float.parseFloat(properties.getProperty("threshold.end"));
        if(properties.containsKey("number.higher.peaks"))
            this.numberHigherPeaks = Integer.parseInt(properties.getProperty("number.higher.peaks"));
        if(properties.containsKey("cluster.rounds"))
            this.clusterRounds = Integer.parseInt(properties.getProperty("cluster.rounds"));
        if(properties.containsKey("binary.temp.directory"))
            this.binaryDirectory = properties.getProperty("binary.temp.directory");
        if(properties.containsKey("reuse.binary.files"))
            this.reuseBinary = Boolean.parseBoolean(properties.getProperty("reuse.binary.files"));
        if(properties.containsKey("ignore.charge"))
            this.ignoreCharge = Boolean.parseBoolean(properties.getProperty("ignore.charge"));
        if(properties.containsKey("cluster.fast.mode"))
            this.fastMode = Boolean.parseBoolean(properties.getProperty("cluster.fast.mode"));
        if(properties.containsKey("filters.remove.reporter.peaks"))
            this.filterReportPeaks = Boolean.parseBoolean(properties.getProperty("filters.remove.reporter.peaks"));
        if(properties.containsKey("initially.shared.peaks"))
            this.nInitiallySharedPeaks = Integer.parseInt(properties.getProperty("initially.shared.peaks"));
        if(properties.containsKey("x.min.comparisons"))
            this.minNumberOfComparisons = Integer.parseInt(properties.getProperty("x.min.comparisons"));
        if(properties.contains("output.msp"))
            this.outputMsp = Boolean.parseBoolean(properties.getProperty("output.msp"));
    }

    public Properties readProperties() throws URISyntaxException {
        Properties properties = new Properties();
        InputStream output;

        try {
            output = getClass().getClassLoader().getResourceAsStream("application.properties");
            properties.load(output);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return properties;
    }

    /**
     * Adapt the default parameters based on the set command line arguments.
     *
     * @param commandLine A command line object.
     */
    public void mergeCommandLineArgs(CommandLine commandLine) {
        if (commandLine.hasOption(CliOptions.OPTIONS.OUTPUT_PATH.getValue()))
            outputFile = new File(commandLine.getOptionValue(CliOptions.OPTIONS.OUTPUT_PATH.getValue()));

        // NUMBER OF ROUNDS
        if (commandLine.hasOption(CliOptions.OPTIONS.ROUNDS.getValue()))
            clusterRounds = Integer.parseInt(commandLine.getOptionValue(CliOptions.OPTIONS.ROUNDS.getValue()));

        if (commandLine.hasOption(CliOptions.OPTIONS.START_THRESHOLD.getValue()))
            thresholdStart = Float.parseFloat(commandLine.getOptionValue(CliOptions.OPTIONS.START_THRESHOLD.getValue()));

        if (commandLine.hasOption(CliOptions.OPTIONS.END_THRESHOLD.getValue()))
            thresholdEnd = Float.parseFloat(commandLine.getOptionValue(CliOptions.OPTIONS.END_THRESHOLD.getValue()));

        if (commandLine.hasOption(CliOptions.OPTIONS.PRECURSOR_TOLERANCE.getValue()))
            precursorIonTolerance = Double.parseDouble(commandLine.getOptionValue(CliOptions.OPTIONS.PRECURSOR_TOLERANCE.getValue()));

        if (commandLine.hasOption(CliOptions.OPTIONS.FRAGMENT_PRECISION.getValue()))
            fragmentIonPrecision = commandLine.getOptionValue(CliOptions.OPTIONS.FRAGMENT_PRECISION.getValue());

        ignoreCharge = commandLine.hasOption(CliOptions.OPTIONS.IGNORE_CHARGE.getValue());

        if (commandLine.hasOption(CliOptions.OPTIONS.TEMP_DIRECTORY.getValue()))
            binaryDirectory = commandLine.getOptionValue(CliOptions.OPTIONS.TEMP_DIRECTORY.getValue());

        if (commandLine.hasOption(CliOptions.OPTIONS.ADVANCED_MIN_NUMBER_COMPARISONS.getValue()))
            minNumberOfComparisons = Integer.parseInt(commandLine.getOptionValue(CliOptions.OPTIONS.ADVANCED_MIN_NUMBER_COMPARISONS.getValue()));

        if (commandLine.hasOption(CliOptions.OPTIONS.ADVANCED_MIN_INITIAL_PEAKS.getValue()))
            nInitiallySharedPeaks = Integer.parseInt(commandLine.getOptionValue(CliOptions.OPTIONS.ADVANCED_MIN_INITIAL_PEAKS.getValue()));

        if (commandLine.hasOption(CliOptions.OPTIONS.N_THREADS.getValue()))
            nThreads = Integer.parseInt(commandLine.getOptionValue(CliOptions.OPTIONS.N_THREADS.getValue()));

        outputMsp = commandLine.hasOption(CliOptions.OPTIONS.OUTPUT_MSP.getValue());
    }

    public void mergeParameters(String configFile) throws IOException {
        File propertiesFactoryBean = new File(configFile);
        Properties newProperties = new Properties();
        InputStream output = new FileInputStream(propertiesFactoryBean);
        newProperties.load(output);
        setProperties(newProperties);
    }

    // ----------
    // A collection of functions to return clustering objects matching the
    // parameters

    /**
     * Get the precursor tolerance as an integer.
     *
     * @return The precursor tolerance
     */
    public int getIntPrecursorTolerance() {
        return (int) Math.round(precursorIonTolerance * (double) BasicIntegerNormalizer.MZ_CONSTANT);
    }

    /**
     * Creates a new GreedyClusteringEngine based on the currently set parameters.
     *
     * Note: The validity of these parameters is not checked in this function but
     * must be checked before calling it.
     *
     * @return A new instance of a GreedyClusteringEngine
     * @throws Exception
     */
    public GreedyClusteringEngine createGreedyClusteringEngine() throws Exception {
        int precursorTolerance = getIntPrecursorTolerance();

        SpectraPerBinNumberComparisonAssessor numberOfComparisonAssessor = new SpectraPerBinNumberComparisonAssessor(
                precursorTolerance * 2, minNumberOfComparisons, BasicIntegerNormalizer.MZ_CONSTANT * 2500
        );

        IComparisonPredicate<ICluster> firstRoundPredicate = new ShareNComparisonPeaksPredicate(
                nInitiallySharedPeaks);

        if (!ignoreCharge) {
            firstRoundPredicate = new SameChargePredicate().and(firstRoundPredicate);
        }

        int windowSizeNoiseFilter = (fragmentIonPrecision.equalsIgnoreCase("high")) ? 3000 : 100;

        GreedyClusteringEngine engine = new GreedyClusteringEngine(
                precursorTolerance,
                thresholdStart, thresholdEnd, clusterRounds, new CombinedFisherIntensityTest(),
                numberOfComparisonAssessor, firstRoundPredicate,
                windowSizeNoiseFilter);

        return engine;
    }

    /**
     * Create a new loading filter based on the currently set parameters.
     *
     * Note: The validity of these parameters is not checked in this function but
     * must be checked before calling it.
     *
     * @return A new instance of an IRawSpectrumFunction
     */
    public IRawSpectrumFunction createLoadingFilter() {
        // set an approximate fragment tolerance for the filters
        double fragmentTolerance = (fragmentIonPrecision.equalsIgnoreCase("high")) ? 0.01 : 0.5;

        return new RemoveImpossiblyHighPeaksFunction()
                .specAndThen(new RemovePrecursorPeaksFunction(fragmentTolerance))
                .specAndThen(new RawPeaksWrapperFunction(new KeepNHighestRawPeaks(numberHigherPeaks)));

    }

    /**
     * Creates a new instance of the matching m/z binner.
     *
     * Note: The validity of these parameters is not checked in this function but
     * must be checked before calling it.
     *
     * @return A new IMzBinner object.
     */
    public IMzBinner createMzBinner() {
        return (fragmentIonPrecision.equalsIgnoreCase("high")) ?
                new HighResolutionMzBinner() : new TideBinner();
    }
}
