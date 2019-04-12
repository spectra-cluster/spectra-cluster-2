package org.spectra.cluster.cdf;

import java.util.ArrayList;
import java.util.List;

/**
 * Represents the empirical cumulative distribution
 * function for a certain similarity metric.
 *
 * Created by jg on 05.05.15.
 */
public class CumulativeDistributionFunction {
    public static final String HEADER_LINE = "max_score\tlower_diff_matches\tcum_lower_diff_matches\trel_cum_lower_matches\ttotal_matches";
    protected final long totalComparisons;
    protected final double scoreIncrements;
    /**
     * Stores the relative number of peptides that were observed
     * below the score threshold. The score threshold for a given
     * array position <i>i</i> is calculated using
     * <i>(i + 1) * scoreIncrements</i>
     */
    protected final double[] proportionPeptidesBelowScore;

    public CumulativeDistributionFunction(long totalComparisons, double scoreIncrements, double[] proportionPeptidesBelowScore) {
        this.totalComparisons = totalComparisons;
        this.scoreIncrements = scoreIncrements;
        this.proportionPeptidesBelowScore = proportionPeptidesBelowScore;
    }

    public static CumulativeDistributionFunction fromString(String string) throws Exception {
        if (string == null || string.length() < 1)
            return null;

        // split into lines
        String[] lines = string.split("\n");

        if (lines.length < 2)
            return null;

        // sanity check whether the right format is used
        if (!HEADER_LINE.equals(lines[0]))
            throw new Exception("Unsupported format used. Header line does not match.");

        String[] firstFields = lines[1].split("\t");
        if (firstFields.length != 5)
            throw new Exception("First line does not contain the expected 5 fields.");

        // save the total score increment - this is stable accross the whole file
        double scoreIncrement = Double.parseDouble(firstFields[0]);
        long calculatedTotalComparisons = 0;

        // get the actual values
        List<Double> proportionPeptidesBelowScore = new ArrayList<>();
        for (int i = 1; i < lines.length; i++) {
            String line = lines[i];
            String[] fields = line.split("\t");

            if (fields.length != 5)
               throw new Exception("Line does not contain the expected 5 fields.");

            long numberCumulativePeptides = Long.parseLong(fields[2]);
            long totalComparisons = Long.parseLong(fields[4]); // total number can change at each step since this is an aggregated file from many single analysis

            double relCumulativePeptides = (double) numberCumulativePeptides / totalComparisons;

            calculatedTotalComparisons = Math.max(calculatedTotalComparisons, totalComparisons);
            proportionPeptidesBelowScore.add(relCumulativePeptides);
        }

        return new CumulativeDistributionFunction(calculatedTotalComparisons, scoreIncrement,
                proportionPeptidesBelowScore.stream().mapToDouble(Double::doubleValue).toArray());
    }

    protected int getBinForScore(double score) {
        double doubleBin = score / scoreIncrements;

        // use ceil to get the next higher scoring bin
        int index = (int) Math.ceil(doubleBin);

        if (index >= proportionPeptidesBelowScore.length)
            index = proportionPeptidesBelowScore.length - 1;

        return index;
    }

    public double getCdfForThreshold(double threshold) {
        int index = getBinForScore(threshold);

        return proportionPeptidesBelowScore[index];
    }

    /**
     * Determines the probability that the passed threshold does belong
     * to a correct match correcting for multiple testing.
     * @param threshold The observed score threshold.
     * @param nComparisons The number of comparison performed.
     * @return The probability as a p-value.
     */
    public double probability(double threshold, int nComparisons) {

        return 1.0 - Math.pow(getCdfForThreshold(threshold), nComparisons);
    }

    /**
     * Determines whether the match at the given similarity would be sufficiently good
     * to satisfy the defined maximal mixture probability.
     * @param similarity Similarity of the match
     * @param nComparisons Number of comparisons already performed for the spectrum.
     * @param maximumMixtureProbability Allowed maximum mixture probability.
     * @return
     */
    public boolean isSaveMatch(double similarity, int nComparisons, double maximumMixtureProbability) {
        // get the estimated proportion of correct matches at this threshold / similarity
        double proportionCorrectMatches = Math.pow(getCdfForThreshold(similarity), nComparisons);
        double minimumCorrectMatches = 1.0 - maximumMixtureProbability;

        return proportionCorrectMatches > minimumCorrectMatches;
    }
}
