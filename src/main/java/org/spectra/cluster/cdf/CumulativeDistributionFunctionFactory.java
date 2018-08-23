package org.spectra.cluster.cdf;

import org.spectra.cluster.similarity.CombinedFisherIntensityTest;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Created by jg on 05.05.15.
 */
public class CumulativeDistributionFunctionFactory {
    private CumulativeDistributionFunctionFactory() {

    }

    /**
     * Returns the the matching CDF from the resource file.
     * @param similatiryClass
     * @return
     * @throws Exception
     */
    public static CumulativeDistributionFunction getDefaultCumlativeDistributionFunctionForSimilarityMetric(Class similatiryClass) throws Exception {
        return getCumulativeDistributionFunctionForSimilarityMetric(similatiryClass);
    }

    /**
     * Returns the matching cumulative distribution function from the matching
     * resource file. If no resource file exists for the passed similarity
     * checker class an Exception is thrown.
     * @param similarityCheckerClass
     * @return
     * @throws Exception Thrown if no CDF resource exists for the passed similarity checker class.
     */
    public static CumulativeDistributionFunction getCumulativeDistributionFunctionForSimilarityMetric(Class similarityCheckerClass) throws Exception {
        if (similarityCheckerClass == CombinedFisherIntensityTest.class) {
            return getCumulativeDistributionFunctionForResource("cumulative.cdf.tsv");
        }

        throw new Exception("No cumulative distribution function defined for " + similarityCheckerClass.toString());
    }

    /**
     * Reads the cumulative distribution function from a resource and builds the
     * matching CumulativeDistributionFunction class from it.
     * @param resource
     * @return
     * @throws Exception
     */
    private static CumulativeDistributionFunction getCumulativeDistributionFunctionForResource(String resource) throws Exception {
        Path resourcePath = Paths.get(CumulativeDistributionFunctionFactory.class.getClassLoader().getResource(resource).toURI());

        StringBuilder definitionString = new StringBuilder();
        Files.lines(resourcePath).forEach(s -> definitionString.append(s).append("\n"));

        return CumulativeDistributionFunction.fromString(definitionString.toString());
    }
}
