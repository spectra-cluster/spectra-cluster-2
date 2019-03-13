package org.spectra.cluster.tools;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.PosixParser;
import org.spectra.cluster.cdf.MinNumberComparisonsAssessor;
import org.spectra.cluster.cdf.SpectraPerBinNumberComparisonAssessor;
import org.spectra.cluster.engine.GreedyClusteringEngine;
import org.spectra.cluster.engine.IClusteringEngine;
import org.spectra.cluster.exceptions.MissingParameterException;
import org.spectra.cluster.filter.binaryspectrum.HighestPeakPerBinFunction;
import org.spectra.cluster.filter.rawpeaks.*;
import org.spectra.cluster.io.cluster.DotClusteringWriter;
import org.spectra.cluster.io.cluster.IClusterWriter;
import org.spectra.cluster.io.properties.IPropertyStorage;
import org.spectra.cluster.io.properties.PropertyStorageFactory;
import org.spectra.cluster.io.spectra.MzSpectraReader;
import org.spectra.cluster.model.cluster.ICluster;
import org.spectra.cluster.model.spectra.IBinarySpectrum;
import org.spectra.cluster.normalizer.BasicIntegerNormalizer;
import org.spectra.cluster.normalizer.MaxPeakNormalizer;
import org.spectra.cluster.normalizer.TideBinner;
import org.spectra.cluster.similarity.CombinedFisherIntensityTest;
import org.spectra.cluster.tools.utils.IProgressListener;
import org.spectra.cluster.tools.utils.ProgressUpdate;
import org.spectra.cluster.util.DefaultParameters;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
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
            double fragmentTolerance = defaultParameters.getFragmentIonTolerance();
            if (commandLine.hasOption(CliOptions.OPTIONS.FRAGMENT_TOLERANCE.getValue())) {
                fragmentTolerance = Float.parseFloat(commandLine.getOptionValue(CliOptions.OPTIONS.FRAGMENT_TOLERANCE.getValue()));
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
            IRawSpectrumFunction loadingFilter = new RemoveImpossiblyHighPeaksFunction()
                    .specAndThen(new RemovePrecursorPeaksFunction(fragmentTolerance))
                    .specAndThen(new RawPeaksWrapperFunction(new KeepNHighestRawPeaks(defaultParameters.getNumberHigherPeaks())));

            IPropertyStorage localStorage = PropertyStorageFactory.buildDynamicPropertyStorage();
            File[] inputFiles = Arrays.stream(peakFiles)
                    .map(File::new)
                    .toArray(File[]::new);

            MzSpectraReader reader = new MzSpectraReader( new TideBinner(), new MaxPeakNormalizer(),
                    new BasicIntegerNormalizer(), new HighestPeakPerBinFunction(), loadingFilter, inputFiles);

            // set the comparison assessor as listener to count the spectra per bin
            reader.addSpectrumListener(numberOfComparisonAssessor);

            Iterator<IBinarySpectrum> iterator = reader.readBinarySpectraIterator(localStorage);

            List<IBinarySpectrum> spectra = new ArrayList<>(1_000);

            log.debug(String.format("Loading spectra from %d file(s)...", inputFiles.length));
            while (iterator.hasNext()) {
                spectra.add(iterator.next());
            }

            // sort according to m/z
            spectra.sort(Comparator.comparingInt(IBinarySpectrum::getPrecursorMz));

            Path thisResult = Paths.get(finalResultFile.getAbsolutePath());

            IClusteringEngine engine = new GreedyClusteringEngine(
                    binnedPrecursorTolerance,
                    startThreshold, endThreshold, rounds, new CombinedFisherIntensityTest(),
                    new MinNumberComparisonsAssessor(10_000), nInitiallySharedPeaks);

            log.debug("Clustering files...");
            ICluster[] clusters = engine.clusterSpectra(spectra.toArray(new IBinarySpectrum[0]));

            IClusterWriter writer = new DotClusteringWriter(thisResult, false, localStorage);
            writer.appendClusters(clusters);
            writer.close();

            System.out.println("Results written to " + thisResult.toString());

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
                               List<String> addedFilters) {
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
        System.out.println("Fragment ion tolerance: " + defaultParameters.getFragmentIonTolerance());

        // used filters
        System.out.print("Added filters: ");
        for (int i = 0; i < addedFilters.size(); i++) {
            if (i > 0) {
                System.out.print(", ");
            }
            System.out.print(addedFilters.get(i));
        }
        System.out.println("");

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

    @Override
    public void onProgressUpdate(ProgressUpdate progressUpdate) {
        System.out.println(progressUpdate.getMessage());
    }
}
