package org.spectra.cluster.similarity;

import cern.jet.random.HyperGeometric;
import cern.jet.random.Normal;
import cern.jet.random.engine.RandomEngine;
import org.apache.commons.math3.distribution.ChiSquaredDistribution;
import org.spectra.cluster.model.spectra.BinaryPeak;
import org.spectra.cluster.model.spectra.IBinarySpectrum;

import java.util.*;

/**
 * Implementation of the combined FisherIntensity test as it
 * is used by the spectra-cluster v1 algorithm.
 */
public class CombinedFisherIntensityTest implements IBinarySpectrumSimilarity {

    private final KendallsCorrelation kendallsCorrelation = new KendallsCorrelation();
    protected final ChiSquaredDistribution chiSquaredDistribution = new ChiSquaredDistribution(4); // always 4 degrees of freedom

    protected final static RandomEngine RANDOM_ENGINE = RandomEngine.makeDefault();

    public static final double MAX_SCORE = 200;

    @Override
    public double correlation(IBinarySpectrum spectrum1, IBinarySpectrum spectrum2) {
        // use copies since these will be changed
        Set<BinaryPeak> peakSet1 = new HashSet<>(spectrum1.getComparisonFilteredPeaks());
        Set<BinaryPeak> peakSet2 = new HashSet<>(spectrum2.getComparisonFilteredPeaks());

        // retain shared peaks
        peakSet1.retainAll(peakSet2);
        peakSet2.retainAll(peakSet1);

        // return 0 if no intensities are shared
        if (peakSet1.size() < 1) {
            return 0;
        }

        // sort the peaks according to m/z
        BinaryPeak[] peaks1 = peakSet1.stream().sorted(Comparator.comparingInt(BinaryPeak::getMz)).toArray(BinaryPeak[]::new);
        BinaryPeak[] peaks2 = peakSet2.stream().sorted(Comparator.comparingInt(BinaryPeak::getMz)).toArray(BinaryPeak[]::new);

        // calculate the hypergeometric score
        int minBin = Math.min(peaks1[0].getMz(), peaks2[0].getMz());
        int maxBin = Math.max(peaks1[peaks1.length - 1].getMz(), peaks2[peaks2.length - 1].getMz());

        int morePeaks = peakSet1.size();
        int lessPeaks = peakSet2.size();

        if (morePeaks < lessPeaks) {
            morePeaks = peakSet2.size();
            lessPeaks = peakSet1.size();
        }

        // the (maxBin - minBin) * 2 formula is used to keep the scores consistent with version
        // 1.x where the bins were evaluated based on the set fragment tolerance. Estimating based
        // on fragment tolerance leads to roughly twice as many bins.
        // -- JG 08.10.2018
        double hgtScore = new HyperGeometric((maxBin - minBin) * 2, morePeaks, lessPeaks, RANDOM_ENGINE).pdf(peakSet1.size());

        if (hgtScore == 0) {
            hgtScore = 1;
        }

        // calculate the fisher p
        double kendallP = assessKendallCorrelation(
                Arrays.stream(peaks1).mapToInt(BinaryPeak::getIntensity).toArray(),
                Arrays.stream(peaks2).mapToInt(BinaryPeak::getIntensity).toArray());

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
