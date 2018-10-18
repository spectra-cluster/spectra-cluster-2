package org.spectra.cluster.tools;

import org.apache.commons.cli.Option;
import org.apache.commons.cli.OptionBuilder;
import org.apache.commons.cli.Options;

/**
 * Created with IntelliJ IDEA.
 * User: jg
 * Date: 9/15/13
 * Time: 11:35 AM

 */
public class CliOptions {
    public enum OPTIONS {
        OUTPUT_PATH("output.path", "o"),

        PRECURSOR_TOLERANCE("precursor.tolerance", "p"),
        FRAGMENT_TOLERANCE("fragment.tolerance", "f"),

        MAJOR_PEAK_JOBS("major.peak.jobs", "m"),

        START_THRESHOLD("threshold.start", "s"),
        END_THRESHOLD("threshold.end", "e"),

        ROUNDS("rounds", "r"),

        BINARY_TMP_DIR("binary.directory", "b"),
        KEEP_BINARY_FILE("keep.binary.files", "k"),

        REUSE_BINARY_FILES("reuse.binary.files", "u"),
        REMOVE_REPORTER_PEAKS("remove.reporters", "rr"),

        FAST_MODE("fast_mode", "fm"),
        FILTER("filter", "ft"),
        HELP("help", "h"),
        VERBOSE("verbose", "v"),

        // Advanced options
        ADVANCED_MIN_NUMBER_COMPARISONS("x.min.comparisons", "mc"),
        ADVANCED_NUMBER_PREFILTERED_PEAKS("xn.prefiltered.peaks", "pp"),
        ADVANCED_LEARN_CDF("x.learn.cdf", "lcdf"),
        ADVANCED_LOAD_CDF_FILE("x.load.cdf", "lcdf"),
        ADVANCED_DISABLE_MGF_COMMENTS("disable.mgf.comments", "dmc");

        private String value;
        private String longValue;

        OPTIONS(String logValue, String value) {
            this.value = value;
            this.longValue = logValue;
        }

        public String getValue() {
            return value;
        }

        @Override
        public String toString() {
            return value;
        }

        public String getLongValue() {
            return longValue;
        }
    }

    private static final Options options = new Options();

    static {
        Option fragmentTolerance = OptionBuilder
                .hasArg()
                .withDescription("fragment ion tolerance in m/z to use for fragment peak matching")
                .withLongOpt(OPTIONS.FRAGMENT_TOLERANCE.getLongValue())
                .create(OPTIONS.FRAGMENT_TOLERANCE.getValue());
        options.addOption(fragmentTolerance);

        Option precursorTolerance = OptionBuilder
                .hasArg()
                .withDescription("precursor tolerance (clustering window size) in m/z used during matching.")
                .withLongOpt(OPTIONS.PRECURSOR_TOLERANCE.getLongValue())
                .create(OPTIONS.PRECURSOR_TOLERANCE.getValue());
        options.addOption(precursorTolerance);

        Option outputPath = OptionBuilder
                .hasArg()
                .withDescription("path to the outputfile. Outputfile must not exist.")
                .withLongOpt(OPTIONS.OUTPUT_PATH.getLongValue())
                .create(OPTIONS.OUTPUT_PATH.getValue());
        options.addOption(outputPath);

        Option startThreshold = OptionBuilder
                .hasArg()
                .withDescription("(highest) starting threshold")
                .withLongOpt(OPTIONS.START_THRESHOLD.getLongValue())
                .create(OPTIONS.START_THRESHOLD.getValue());
        options.addOption(startThreshold);

        Option endThreshold = OptionBuilder
                .hasArg()
                .withDescription("(lowest) final clustering threshold")
                .withLongOpt(OPTIONS.END_THRESHOLD.getLongValue())
                .create(OPTIONS.END_THRESHOLD.getValue());
        options.addOption(endThreshold);

        Option rounds = OptionBuilder
                .hasArg()
                .withDescription("number of clustering rounds to use.")
                .withLongOpt(OPTIONS.ROUNDS.getLongValue())
                .create(OPTIONS.ROUNDS.getValue());
        options.addOption(rounds);

        Option majorPeakJobs = OptionBuilder
                .hasArg()
                .withDescription("number of threads to use for major peak clustering.")
                .withLongOpt(OPTIONS.MAJOR_PEAK_JOBS.getLongValue())
                .create(OPTIONS.MAJOR_PEAK_JOBS.getValue());
        options.addOption(majorPeakJobs);

        Option binaryDirectory = OptionBuilder
                .hasArg()
                .withDescription("path to the directory to (temporarily) store the binary files. By default a temporary directory is being created")
                .withLongOpt(OPTIONS.BINARY_TMP_DIR.getLongValue())
                .create(OPTIONS.BINARY_TMP_DIR.getValue());
        options.addOption(binaryDirectory);

        Option keepBinary = OptionBuilder
                .withDescription("if this options is set, the binary files are not deleted after clustering.")
                .withLongOpt(OPTIONS.KEEP_BINARY_FILE.getLongValue())
                .create(OPTIONS.KEEP_BINARY_FILE.getValue());
        options.addOption(keepBinary);

        Option reuseBinaryFiles = OptionBuilder
                .withDescription("if this option is set, the binary files found in the binary file directory will be used for clustering.")
                .withLongOpt(OPTIONS.REUSE_BINARY_FILES.getLongValue())
                .create(OPTIONS.REUSE_BINARY_FILES.getValue());
        options.addOption(reuseBinaryFiles);

        Option fastMode = OptionBuilder
                .withDescription("if this option is set the 'fast mode' is enabled. In this mode, the radical peak filtering used for the comparison function is already applied during spectrum conversion. Thereby, the clustering and consensus spectrum quality is slightly decreased but speed increases 2-3 fold.")
                .withLongOpt(OPTIONS.FAST_MODE.getLongValue())
                .create(OPTIONS.FAST_MODE.getValue());
        options.addOption(fastMode);

        Option filter = OptionBuilder
                .hasArg()
                .withDescription("Adds a filter to be applied to the input spectrum. Available values are ['immonium_ions', 'mz_150', 'mz_200']")
                .withLongOpt(OPTIONS.FILTER.getLongValue())
                .create(OPTIONS.FILTER.getValue());
        options.addOption(filter);

        Option verbose = OptionBuilder
                .withDescription("if set additional status information is printed.")
                .withLongOpt(OPTIONS.VERBOSE.getLongValue())
                .create(OPTIONS.VERBOSE.getValue());
        options.addOption(verbose);

        Option removeReporters = OptionBuilder
                .hasArg()
                .withArgName("QUANTITATION TYPE")
                .withDescription("Filter Remove Reporter ion peaks in quantitation experiments. Possible QUANTIATION TYPES are 'ITRAQ', 'TMT' and 'ALL' ('TMT' and 'ITRAQ' peaks are removed.")
                .withLongOpt(OPTIONS.REMOVE_REPORTER_PEAKS.getLongValue())
                .create(OPTIONS.REMOVE_REPORTER_PEAKS.getValue());
        options.addOption(removeReporters);

        Option help = new Option(OPTIONS.HELP.toString(),OPTIONS.HELP.longValue,false, "Print this message.");
        options.addOption(help);

        /**
         * ADVANCED OPTIONS
         */
        Option xMinComparisons = OptionBuilder
                .withDescription("(Experimental option) Sets the minimum number of comparisons used to calculate the probability that incorrect spectra are clustered.")
                .hasArg()
                .withLongOpt(OPTIONS.ADVANCED_MIN_NUMBER_COMPARISONS.getLongValue())
                .create(OPTIONS.ADVANCED_MIN_NUMBER_COMPARISONS.getValue());
        options.addOption(xMinComparisons);

        Option xLearnCdf = OptionBuilder
                .hasArg()
                .withArgName("output filename")
                .withDescription("(Experimental option) Learn the used cumulative distribution function directly from the processed data. This is only recommended for high-resolution data. The result will be written to the defined file.")
                .withLongOpt(OPTIONS.ADVANCED_LEARN_CDF.getLongValue())
                .create(OPTIONS.ADVANCED_LEARN_CDF.getValue());
        options.addOption(xLearnCdf);

        Option xLoadCdf = OptionBuilder
                .hasArg()
                .withArgName("CDF filename")
                .withDescription("(Experimental option) Loads the cumulative distribution function to use from the specified file. These files can be created using the " + OPTIONS.ADVANCED_LEARN_CDF.getValue() + " parameter")
                .withLongOpt(OPTIONS.ADVANCED_LOAD_CDF_FILE.getLongValue())
                .create(OPTIONS.ADVANCED_LOAD_CDF_FILE.getValue());
        options.addOption(xLoadCdf);

        Option xNumberPrefilteredPeaks = OptionBuilder
                .hasArg()
                .withArgName("number peaks")
                .withDescription("(Experimental option) Set the number of highest peaks that are kept per spectrum during loading.")
                .create(OPTIONS.ADVANCED_NUMBER_PREFILTERED_PEAKS.getValue());
        options.addOption(xNumberPrefilteredPeaks);

        Option xDisableMgfComments = OptionBuilder
                .withDescription("(Advanced option) If set, MGF comment strings are NOT supported. This will increase performance but only works for MGF files that do not contain any comments")
                .withLongOpt(OPTIONS.ADVANCED_DISABLE_MGF_COMMENTS.getLongValue())
                .create(OPTIONS.ADVANCED_DISABLE_MGF_COMMENTS.getValue());
        options.addOption(xDisableMgfComments);
    }

    public static Options getOptions() {
        return options;
    }
}
