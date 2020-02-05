package org.spectra.cluster.tools;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.PosixParser;
import org.bigbio.pgatk.io.properties.IPropertyStorage;
import org.bigbio.pgatk.io.properties.PropertyStorageFactory;
import org.spectra.cluster.cdf.SpectraPerBinNumberComparisonAssessor;
import org.spectra.cluster.engine.GreedyClusteringEngine;
import org.spectra.cluster.engine.IClusteringEngine;
import org.spectra.cluster.exceptions.MissingParameterException;
import org.spectra.cluster.filter.binaryspectrum.HighestPeakPerBinFunction;
import org.spectra.cluster.filter.rawpeaks.*;
import org.spectra.cluster.io.cluster.old_writer.DotClusteringWriter;
import org.spectra.cluster.io.cluster.old_writer.IClusterWriter;
import org.spectra.cluster.io.spectra.MzSpectraReader;
import org.spectra.cluster.model.cluster.ICluster;
import org.spectra.cluster.normalizer.*;
import org.spectra.cluster.predicates.IComparisonPredicate;
import org.spectra.cluster.predicates.SameChargePredicate;
import org.spectra.cluster.predicates.ShareNComparisonPeaksPredicate;
import org.spectra.cluster.similarity.CombinedFisherIntensityTest;
import org.spectra.cluster.tools.utils.IProgressListener;
import org.spectra.cluster.tools.utils.ProgressUpdate;
import org.spectra.cluster.util.DefaultParameters;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.*;

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
@Slf4j
public class SpectraClusterTool implements IProgressListener {

    public static final boolean DELETE_TEMPORARY_CLUSTERING_RESULTS = true;

    public DefaultParameters defaultParameters = new DefaultParameters();

    private boolean verbose;

    public static void main(String[] args) {
        SpectraClusterTool instance = new SpectraClusterTool();
        instance.run(args);
    }

    private void run(String[] args) {
        CommandLineParser parser = new PosixParser();

        try {
            CommandLine commandLine = parser.parse(CliOptions.getOptions(), args);

            // HELP
            if (commandLine.hasOption(CliOptions.OPTIONS.HELP.getValue())) {
                printUsage();
                return;
            }

            // File input
            String[] peakFiles = commandLine.getArgs();

            if (peakFiles.length < 1) {
                printUsage();
                throw new MissingParameterException("Missing input files");
            }

            // RESULT FILE PATH
            if (!commandLine.hasOption(CliOptions.OPTIONS.OUTPUT_PATH.getValue()))
                throw new MissingParameterException("Missing required option " + CliOptions.OPTIONS.OUTPUT_PATH.getValue());
            File finalResultFile = new File(commandLine.getOptionValue(CliOptions.OPTIONS.OUTPUT_PATH.getValue()));

            if(commandLine.hasOption(CliOptions.OPTIONS.CONFIG_FILE.getValue()))
                defaultParameters.mergeParameters(commandLine.getOptionValue(CliOptions.OPTIONS.CONFIG_FILE.getValue()));

            if (finalResultFile.exists())
                throw new Exception("Result file " + finalResultFile + " already exists");

            // NUMBER OF ROUNDS
            int rounds = defaultParameters.getClusterRounds();
            if (commandLine.hasOption(CliOptions.OPTIONS.ROUNDS.getValue()))
                rounds = Integer.parseInt(commandLine.getOptionValue(CliOptions.OPTIONS.ROUNDS.getValue()));

            // START THRESHOLD
            float startThreshold = defaultParameters.getThresholdStart();
            if (commandLine.hasOption(CliOptions.OPTIONS.START_THRESHOLD.getValue()))
                startThreshold = Float.parseFloat(commandLine.getOptionValue(CliOptions.OPTIONS.START_THRESHOLD.getValue()));

            // END THRESHOLD
            float endThreshold = defaultParameters.getThresholdEnd();
            if (commandLine.hasOption(CliOptions.OPTIONS.END_THRESHOLD.getValue()))
                endThreshold = Float.parseFloat(commandLine.getOptionValue(CliOptions.OPTIONS.END_THRESHOLD.getValue()));

            // PRECURSOR TOLERANCE
            // TODO: Add support for precursor tolerance
            double precursorTolerance = defaultParameters.getPrecursorIonTolerance();
            if (commandLine.hasOption(CliOptions.OPTIONS.PRECURSOR_TOLERANCE.getValue())) {
                precursorTolerance = Float.parseFloat(commandLine.getOptionValue(CliOptions.OPTIONS.PRECURSOR_TOLERANCE.getValue()));
            }
            // convert the precursor tolerance to int space
            int binnedPrecursorTolerance = (int) Math.round(precursorTolerance * (double) BasicIntegerNormalizer.MZ_CONSTANT);

            // FRAGMENT TOLERANCE
            // TODO: Add support for fragment tolerance
            String fragmentPrecision = defaultParameters.getFragmentIonPrecision();
            if (commandLine.hasOption(CliOptions.OPTIONS.FRAGMENT_PRECISION.getValue())) {
                fragmentPrecision = commandLine.getOptionValue(CliOptions.OPTIONS.FRAGMENT_PRECISION.getValue());
            }
            if (!"high".equalsIgnoreCase(fragmentPrecision) && !"low".equalsIgnoreCase(fragmentPrecision)) {
                throw new Exception("Invalid fragment precision set. Allowed values are 'low' and 'high'");
            }

            // IGNORE CHARGE STATE
            boolean ignoreCharge = defaultParameters.isIgnoreCharge();
            if (commandLine.hasOption(CliOptions.OPTIONS.IGNORE_CHARGE.getValue())) {
                ignoreCharge = true;
            }

            String tempFolder = defaultParameters.getBinaryDirectory();
            if(commandLine.hasOption(CliOptions.OPTIONS.TEMP_DIRECTORY.getValue())){
                tempFolder = commandLine.getOptionValue(CliOptions.OPTIONS.TEMP_DIRECTORY.getValue());
            }else{
                tempFolder = createTempFolderPath(finalResultFile, tempFolder);
            }

            /**
             * Advanced options
             */
            // MIN NUMBER OF COMPARISONS
            int minNumberComparisons = defaultParameters.getMinNumberOfComparisons();
            if (commandLine.hasOption(CliOptions.OPTIONS.ADVANCED_MIN_NUMBER_COMPARISONS.getValue())) {
                minNumberComparisons = Integer.parseInt(commandLine.getOptionValue(CliOptions.OPTIONS.ADVANCED_MIN_NUMBER_COMPARISONS.getValue()));
            }

            SpectraPerBinNumberComparisonAssessor numberOfComparisonAssessor = new SpectraPerBinNumberComparisonAssessor(
                    binnedPrecursorTolerance * 2, minNumberComparisons, BasicIntegerNormalizer.MZ_CONSTANT * 2500
            );

            int nInitiallySharedPeaks = defaultParameters.getNInitiallySharedPeaks();

            /** Perform clustering **/
            LocalDateTime startTime = LocalDateTime.now();

            // set an approximate fragment tolerance for the filters
            double fragmentTolerance = (fragmentPrecision.equalsIgnoreCase("high")) ? 0.01 : 0.5;

            // set the window size for the noise filter
            int windowSizeNoiseFilter = (fragmentPrecision.equalsIgnoreCase("high")) ? 3000 : 100;

            // set the appropriate m/z binner
            IMzBinner mzBinner = (fragmentPrecision.equalsIgnoreCase("high")) ? new HighResolutionMzBinner() : new TideBinner();

            IRawSpectrumFunction loadingFilter = new RemoveImpossiblyHighPeaksFunction()
                    .specAndThen(new RemovePrecursorPeaksFunction(fragmentTolerance))
                    .specAndThen(new RawPeaksWrapperFunction(new KeepNHighestRawPeaks(defaultParameters.getNumberHigherPeaks())));

            IPropertyStorage localStorage = PropertyStorageFactory.buildDynamicLevelDBPropertyStorage(new File(tempFolder));

            File[] inputFiles = Arrays.stream(peakFiles)
                    .map(File::new)
                    .toArray(File[]::new);

            // create the comparison predicate for the first round
            IComparisonPredicate<ICluster> firstRoundPredicate = new ShareNComparisonPeaksPredicate(nInitiallySharedPeaks);

            IClusteringEngine engine = new GreedyClusteringEngine(
                    binnedPrecursorTolerance,
                    startThreshold, endThreshold, rounds, new CombinedFisherIntensityTest(),
                    numberOfComparisonAssessor, firstRoundPredicate,
                    windowSizeNoiseFilter);

            MzSpectraReader reader = new MzSpectraReader( mzBinner, new MaxPeakNormalizer(),
                    new BasicIntegerNormalizer(), new HighestPeakPerBinFunction(), loadingFilter,
                    GreedyClusteringEngine.COMPARISON_FILTER, engine, inputFiles);

            // set the comparison assessor as listener to count the spectra per bin
            reader.addSpectrumListener(numberOfComparisonAssessor);

            Iterator<ICluster> iterator = reader.readClusterIterator(localStorage);

            List<ICluster> spectra = new ArrayList<>(1_000);

            log.debug(String.format("Loading spectra from %d file(s)...", inputFiles.length));

            int count = 0;
            while (iterator.hasNext()) {
                ICluster cluster = iterator.next();
                spectra.add(cluster);
                count++;
            }

            LocalDateTime loadingCompleteTime = LocalDateTime.now();
            log.debug(String.format("Loaded %d spectra in %d seconds", count, Duration.between(startTime, loadingCompleteTime).getSeconds()));

            // sort according to m/z
            spectra.sort(Comparator.comparingInt(ICluster::getPrecursorMz));

            if (!ignoreCharge) {
                firstRoundPredicate = new SameChargePredicate().and(firstRoundPredicate);
            }

            Path thisResult = Paths.get(finalResultFile.getAbsolutePath());

            log.debug("Clustering files...");
            ICluster[] clusters = engine.clusterSpectra(spectra.toArray(new ICluster[spectra.size()]));

            LocalDateTime clusteringCompleteTime = LocalDateTime.now();
            log.debug(String.format("Clustering completed in %d seconds", Duration.between(loadingCompleteTime, clusteringCompleteTime).getSeconds()));

            IClusterWriter writer = new DotClusteringWriter(thisResult, false, localStorage);
            writer.appendClusters(clusters);
            writer.close();

            log.debug(String.format("Results written in %d seconds", Duration.between(clusteringCompleteTime, LocalDateTime.now()).getSeconds()));
            System.out.println("Results written to " + thisResult.toString());

            log.debug(String.format("Process completed in %d seconds", Duration.between(startTime, LocalDateTime.now()).getSeconds()));

            // Close the property storage and delete folders and property files
            localStorage.close();

            System.exit(0);
        } catch (MissingParameterException e) {
            System.out.println("Error: " + e.getMessage() + "\n\n");
            printUsage();

            System.exit(1);
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("Error: " + e.getMessage());

            System.exit(1);
        }
    }

    private void printSettings(File finalResultFile, int nMajorPeakJobs, float startThreshold,
                               float endThreshold, int rounds, boolean keepBinaryFiles, File binaryTmpDirectory,
                               String[] peaklistFilenames, boolean reUseBinaryFiles, boolean fastMode,
                               List<String> addedFilters, String fragmentPrecision) {
        System.out.println("Spectra Cluster API Version 2.0");
        System.out.println("Created by Yasset Perez-Riverol & Johannes Griss\n");

        System.out.println("-- Settings --");
        System.out.println("Number of threads: " + String.valueOf(nMajorPeakJobs));
        System.out.println("Thresholds: " + String.valueOf(startThreshold) + " - " + String.valueOf(endThreshold) + " in " + rounds + " rounds");
        System.out.println("Keeping binary files: " + (keepBinaryFiles ? "true" : "false"));
        System.out.println("Binary file directory: " + binaryTmpDirectory);
        System.out.println("Result file: " + finalResultFile);
        System.out.println("Reuse binary files: " + (reUseBinaryFiles ? "true" : "false"));
        System.out.println("Input files: " + peaklistFilenames.length);
        System.out.println("Using fast mode: " + (fastMode ? "yes" : "no"));

        System.out.println("\nOther settings:");
        System.out.println("Precursor tolerance: " + defaultParameters.getPrecursorIonTolerance());
        System.out.println("Fragment ion precision: " + fragmentPrecision);

        // used filters
        System.out.print("Added filters: ");
        for (int i = 0; i < addedFilters.size(); i++) {
            if (i > 0) {
                System.out.print(", ");
            }
            System.out.print(addedFilters.get(i));
        }
        System.out.println();

//        // only show certain settings if they were changed
//        if (Defaults.getMinNumberComparisons() != Defaults.DEFAULT_MIN_NUMBER_COMPARISONS)
//            System.out.println("Minimum number of comparisons: " + Defaults.getMinNumberComparisons());

        System.out.println();
    }

    private void printUsage() {
        HelpFormatter formatter = new HelpFormatter();
        formatter.printHelp("Spectra Cluster ",
                "Clusters the spectra found in Mass Spectrometry file formats and writes the results in a text-based file.\n",
                CliOptions.getOptions(), "\n\n", true);
    }

    private String createTempFolderPath(File outputFile, String tempFolder) {
        //check directory
        String finalPath;
        File directory = new File(outputFile.getParentFile(), tempFolder);
        if(directory.exists())
            finalPath = directory.getAbsolutePath();
        else{
            directory.mkdirs();
            finalPath = directory.getAbsolutePath();
        }
        return finalPath;
    }

    @Override
    public void onProgressUpdate(ProgressUpdate progressUpdate) {
        System.out.println(progressUpdate.getMessage());
    }
}
