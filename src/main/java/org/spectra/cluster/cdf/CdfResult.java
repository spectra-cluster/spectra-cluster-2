package org.spectra.cluster.cdf;

import java.util.ArrayList;
import java.util.List;

/**
 * This class is used to create empirical CDFs by storing the number
 * of incorrect peptides below a given score.
 *
 * Created by jg on 04.05.15.
 */
public class CdfResult {
    private long totalComparisons = 0;

    private final double scoreIncrements;
    private List<Long> lowerPeptidesPerScoreIncrement = new ArrayList<>();

    /**
     * Creates a new CdfResult class based on the defined scoreIncrements. These score
     * increments define the score windows used to save the different thresholds and thereby
     * define the granularity of the given score. For example, for the dot product a scoreIncrement
     * of 0.5 means that values stored for "0.5 > 1 > 1.5 > ..." are being stored.
     *
     * Values used as scoreIncrements are:
     *   - 0.5 for the CombinedFisherIntensityTest
     *   - 0.01 for the DotProduct
     *
     * @param scoreIncrements The score increment (ie. score window size) to use to store the created CDF.
     */
    public CdfResult(double scoreIncrements) {
        this.scoreIncrements = scoreIncrements;
    }

    /**
     * Saves the similarity score observed for a random (= incorrect)
     * match.
     * @param similarity The observed similarity
     */
    public void saveRandomMatchResult(double similarity) {
        totalComparisons++;

        int bin = getBinForScore(similarity);

        // ignore scores that cannot be stored correctly
        if (bin < 0) {
            return;
        }

        if (lowerPeptidesPerScoreIncrement.size() < bin + 1) {
            int oldSize = lowerPeptidesPerScoreIncrement.size();
            for (int i = oldSize; i < bin + 1; i++) {
                lowerPeptidesPerScoreIncrement.add(0L);
            }
        }

        lowerPeptidesPerScoreIncrement.set(bin, lowerPeptidesPerScoreIncrement.get(bin) + 1L);
    }

    /**
     * Function used to bin the observed scores.
     * @param score The observed score.
     * @return The bin index as integer.
     */
    private int getBinForScore(double score) {
        if (score < 0)
            score = 0;

        double doubleBin = score / scoreIncrements;

        return (int) Math.floor(doubleBin);
    }

    /**
     * This function is used to save the result as a TSV file.
     * @return The TSV representation of the learned function.
     */
    @Override
    public String toString() {
        StringBuilder stringBuilder = new StringBuilder("max_score\tlower_diff_matches\tcum_lower_diff_matches\trel_cum_lower_matches\ttotal_matches\n");

        long totalMatches = 0;

        for (int i = 0; i < lowerPeptidesPerScoreIncrement.size(); i++) {
            totalMatches += lowerPeptidesPerScoreIncrement.get(i);

            stringBuilder
                    .append(String.format("%.2f", scoreIncrements * (i + 1)))
                    .append("\t")
                    .append(lowerPeptidesPerScoreIncrement.get(i))
                    .append("\t")
                    .append(totalMatches)
                    .append("\t")
                    .append((double) totalMatches / totalComparisons)
                    .append("\t")
                    .append(totalComparisons)
                    .append("\n");
        }

        return stringBuilder.toString();
    }

    /**
     * Function used to merge two results.
     * @param other The CdfResult to add to this result
     * @throws Exception Thrown if the same object is merged or if different score increments were used.
     */
    public void addCdfResult(CdfResult other) throws Exception {
        if (this == other)
            throw new Exception("Cannot join same object");

        if (this.scoreIncrements != other.scoreIncrements)
            throw new Exception("Cannot add cdf result with different score increment (this = " + this.scoreIncrements + ", other = " + other.scoreIncrements + ")");

        int maxSize = Math.max(other.lowerPeptidesPerScoreIncrement.size(), this.lowerPeptidesPerScoreIncrement.size());

        for (int i = 0; i < maxSize; i++) {
            if (i >= this.lowerPeptidesPerScoreIncrement.size()) {
                this.lowerPeptidesPerScoreIncrement.add(other.lowerPeptidesPerScoreIncrement.get(i));
                continue;
            }

            if (i >= other.lowerPeptidesPerScoreIncrement.size()) {
                continue;
            }

            long thisCount = this.lowerPeptidesPerScoreIncrement.get(i);
            long otherCount = other.lowerPeptidesPerScoreIncrement.get(i);

            // override this number
            this.lowerPeptidesPerScoreIncrement.set(i, thisCount + otherCount);
        }

        this.totalComparisons += other.totalComparisons;
    }

    /**
     * Return the total number of observerd comparisons.
     * @return The number of comparisons as long.
     */
    public long getTotalComparisons() {
        return totalComparisons;
    }
}
