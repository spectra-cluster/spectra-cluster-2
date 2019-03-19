package org.spectra.cluster.similarity;

import cern.jet.random.HyperGeometric;
import cern.jet.random.Normal;
import cern.jet.random.engine.RandomEngine;
import org.apache.commons.math3.distribution.ChiSquaredDistribution;
import org.spectra.cluster.model.spectra.BinaryPeak;
import org.spectra.cluster.model.spectra.IBinarySpectrum;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Implementation of the combined FisherIntensity test as it
 * is used by the spectra-cluster v1 algorithm.
 */
public class CombinedFisherIntensityTest implements IBinarySpectrumSimilarity {

    private final KendallsCorrelation kendallsCorrelation = new KendallsCorrelation();
    /** Cached ChiSquareDistribution for the HGT score calculation */
    protected final ChiSquaredDistribution chiSquaredDistribution = new ChiSquaredDistribution(4); // always 4 degrees of freedom
    /** Static RandomEngine since it is not used */
    protected final static RandomEngine RANDOM_ENGINE = RandomEngine.makeDefault();
    /** Define a maximum score which is returned if infinity is reached */
    public static final double MAX_SCORE = 200;
    /** Value for an extremely low score */
    public static final double BAD_SCORE = 0;

    /** Minimum number of shared peaks required to calculate the score. Otherwise BAD_SCORE is returned */
    private final int minSharedPeaks;
    /** Maximum hgt probability above which no Kendall correlation is calculated */
    private final double maxHgt;

    /**
     * Create a new CombinedFisherIntensityTest similarity function.
     * @param minSharedPeaks The minimum number of shared peaks to calculate the score.
     * @param maxHgt The maximum allowed p-value for the HGT test. If the HGT score is above this
     *               p-value, the Kendall correlation is not calculated and only the HGT score returned
     */
    public CombinedFisherIntensityTest(int minSharedPeaks, double maxHgt) {
        // minSharedPeaks must not be below 1
        this.minSharedPeaks = (minSharedPeaks >= 1 ? minSharedPeaks : 1);
        this.maxHgt = maxHgt;
    }

    /**
     * Creates a CombinedFisherIntensityTest with disabled additional filtering
     */
    public CombinedFisherIntensityTest() {
        this.minSharedPeaks = 1;
        this.maxHgt = 2; // impossibly high
    }

    @Override
    public double correlation(IBinarySpectrum spectrum1, IBinarySpectrum spectrum2) {
        // use copies since these will be changed
        Set<BinaryPeak> peakSet1 = new HashSet<>(spectrum1.getComparisonFilteredPeaks().keySet());
        Set<BinaryPeak> peakSet2 = new HashSet<>(spectrum2.getComparisonFilteredPeaks().keySet());

        // retain shared peaks
        peakSet1.retainAll(peakSet2);
        peakSet2.retainAll(peakSet1);

        // return 0 if no intensities are shared
        if (peakSet1.size() < minSharedPeaks) {
            return BAD_SCORE;
        }

        // calculate the hypergeometric score
        int minBin = Math.min(spectrum1.getMinComparisonMz(), spectrum2.getMinComparisonMz());
        int maxBin = Math.max(spectrum1.getMaxComparisonMz(), spectrum2.getMaxComparisonMz());

        int morePeaks = spectrum1.getComparisonFilteredPeaks().size();
        int lessPeaks = spectrum2.getComparisonFilteredPeaks().size();

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

        // only return the hgtScore if it is above the allowed maximum
        if (hgtScore > maxHgt) {
            return -Math.log(hgtScore);
        }

        // create the list of intensities
        Map<BinaryPeak, BinaryPeak> comparisonPeaks2 = spectrum2.getComparisonFilteredPeaks();
        int[] intensities1 = new int[peakSet1.size()];
        int[] intensities2 = new int[peakSet2.size()];
        int counter = 0;

        for (BinaryPeak p : peakSet1) {
            intensities1[counter] = p.getIntensity();
            intensities2[counter] = comparisonPeaks2.get(p).getIntensity();
            counter++;
        }

        // calculate the fisher p
        double kendallP = assessKendallCorrelation(
                intensities1,
                intensities2);

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
