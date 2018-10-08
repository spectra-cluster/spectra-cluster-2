package org.spectra.cluster.similarity;

import cern.jet.random.HyperGeometric;
import cern.jet.random.Normal;
import cern.jet.random.engine.RandomEngine;
import org.apache.commons.math3.distribution.ChiSquaredDistribution;
import org.spectra.cluster.model.spectra.BinaryPeak;
import org.spectra.cluster.model.spectra.IBinarySpectrum;

import java.util.ArrayList;
import java.util.List;

/**
 * Implementation of the combined FisherIntensity test as it
 * is used by the spectra-cluster v1 algorithm.
 */
public class CombinedFisherIntensityTest implements IBinarySpectrumSimilarity {

    private final KendallsCorrelation kendallsCorrelation = new KendallsCorrelation();
    protected final ChiSquaredDistribution chiSquaredDistribution = new ChiSquaredDistribution(4); // always 4 degrees of freedom

    protected final static RandomEngine RANDOM_ENGINE = RandomEngine.makeDefault();

    public final double MAX_SCORE = 200;

    @Override
    public double correlation(IBinarySpectrum spectrum1, IBinarySpectrum spectrum2) {
        int index1 = 0;
        int index2 = 0;
        List<Integer> sharedIntensitySpec1 = new ArrayList<>(20);
        List<Integer> sharedIntensitySpec2 = new ArrayList<>(20);

        BinaryPeak[] peaks1 = spectrum1.getPeaks();
        BinaryPeak[] peaks2 = spectrum2.getPeaks();

        // get the shared peak indexes
        for (; index1 < peaks1.length; index1++) {
            int mzSpec1 = peaks1[index1].getMz();

            for (; index2 < peaks2.length; index2++) {
                int mzSpec2 = peaks2[index2].getMz();

                if (mzSpec1 < mzSpec2) {
                    break;
                }
                if (mzSpec2 < mzSpec1) {
                    continue;
                }

                // now both are equal
                sharedIntensitySpec1.add(peaks1[index1].getIntensity());
                sharedIntensitySpec2.add(peaks2[index2].getIntensity());
                index2++;
                break;
            }
        }

        // return 0 if no intensities are shared
        if (sharedIntensitySpec1.size() < 1) {
            return 0;
        }

        // calculate the hypergeometric score
        int minBin = Math.min(peaks1[0].getMz(), peaks2[0].getMz());
        int maxBin = Math.max(peaks1[peaks1.length - 1].getMz(), peaks2[peaks2.length - 1].getMz());

        int morePeaks = peaks1.length;
        int lessPeaks = peaks2.length;

        if (morePeaks < lessPeaks) {
            morePeaks = peaks2.length;
            lessPeaks = peaks1.length;
        }

        // the (maxBin - minBin) * 2 formula is used to keep the scores consistent with version
        // 1.x where the bins were evaluated based on the set fragment tolerance. Estimating based
        // on fragment tolerance leads to roughly twise as many bins.
        // -- JG 08.10.2018
        double hgtScore = new HyperGeometric((maxBin - minBin) * 2, morePeaks, lessPeaks, RANDOM_ENGINE).pdf(sharedIntensitySpec1.size());

        if (hgtScore == 0) {
            hgtScore = 1;
        }

        // calculate the fisher p
        double kendallP = assessKendallCorrelation(
                sharedIntensitySpec1.stream().mapToInt(Integer::intValue).toArray(),
                sharedIntensitySpec2.stream().mapToInt(Integer::intValue).toArray());

        // combine the two

        return combineProbabilities(hgtScore, kendallP);
    }

    /**
     * Combine two p-values using Fisher's method
     * @param p1 First p-value
     * @param p2 Second p-value
     * @return The combined p-value.
     */
    private double combineProbabilities(double p1, double p2) {
        // combine the p-values using Fisher's method
        double combined = -2 * (Math.log(p1) + Math.log(p2));
        double pValue;

        if (combined == 0)
            return 0;

        if (Double.isInfinite(combined))
            pValue = 0;
        else
            pValue = chiSquaredDistribution.density(combined);

        // return a very high score if the p-value is 0
        if (pValue == 0) {
            return MAX_SCORE;
        }

        return -Math.log(pValue);
    }

    /**
     * Assess the Kendall Tau's correlation converted to a probability score.
     * @param sharedIntensitySpec1 A list of intensities for spectrum 1
     * @param sharedIntensitySpec2 A list of intensities for spectrum 2
     * @return Probability as a double
     */
    private double assessKendallCorrelation(int[] sharedIntensitySpec1, int[] sharedIntensitySpec2) {
        // get the Tau score
        double correlation = kendallsCorrelation.correlation(sharedIntensitySpec1, sharedIntensitySpec2);

        // map to p-value
        // if the correlation cannot be calculated, assume that there is none
        if (Double.isNaN(correlation) || correlation == 0) {
            return 1;
        }

        // convert correlation into probability using the distribution used in Peptidome
        // Normal Distribution with mean = 0 and SD^2 = 2(2k + 5)/9k(k − 1)
        double k = (double) sharedIntensitySpec1.length;

        // this cannot be calculated for only 1 shared peak
        if (k == 1)
            return 1;

        double sdSquare = (2 * (2 * k + 5)) / (9 * k * (k - 1) );
        double sd = Math.sqrt(sdSquare);

        Normal normal = new Normal(0, sd, RANDOM_ENGINE);
        double probability = normal.cdf(correlation);

        return 1 - probability;
    }
}
