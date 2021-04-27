package org.spectra.cluster.engine;

import io.github.bigbio.pgatk.io.common.spectra.Spectrum;
import lombok.extern.slf4j.Slf4j;
import org.spectra.cluster.cdf.CumulativeDistributionFunction;
import org.spectra.cluster.cdf.CumulativeDistributionFunctionFactory;
import org.spectra.cluster.cdf.INumberOfComparisonAssessor;
import org.spectra.cluster.filter.binaryspectrum.FractionTicFilterFunction;
import org.spectra.cluster.filter.binaryspectrum.IBinarySpectrumFunction;
import org.spectra.cluster.model.cluster.GreedySpectralCluster;
import org.spectra.cluster.model.cluster.ICluster;
import org.spectra.cluster.model.consensus.GreedyClusteringConsensusSpectrum;
import org.spectra.cluster.model.consensus.IClusteringConsensusSpectrumBuilder;
import org.spectra.cluster.model.spectra.IBinarySpectrum;
import org.spectra.cluster.predicates.ClusterIsKnownComparisonPredicate;
import org.spectra.cluster.predicates.IComparisonPredicate;
import org.spectra.cluster.similarity.IBinarySpectrumSimilarity;

import java.security.InvalidParameterException;
import java.util.Arrays;
import java.util.Comparator;
import java.util.stream.Collectors;

/**
 * Implementation of the original clustering engine used by default
 * in the spectra-cluster 1.x API
 *
 * @author jg
 * @author ypriverol
 */
@Slf4j
public class GreedyClusteringEngine implements IClusteringEngine {
    public final static IBinarySpectrumFunction COMPARISON_FILTER = new FractionTicFilterFunction();

    private final int precursorTolerance;
    private final float thresholdStart;
    private final float thresholdEnd;
    private final int clusteringRounds;
    private final IBinarySpectrumSimilarity similarityMeasure;
    private final CumulativeDistributionFunction cdf;
    private final INumberOfComparisonAssessor numberOfComparisonAssessor;
    private final IComparisonPredicate<ICluster> firstRoundPredicate;
    private final int consensusSpectrumNoiseFilterIncrement;
    // TODO: Add a factory for consensus spectrum builder so we can put them as a parameter
    // private final IConsensusSpectrumBuilder consensusSpectrumBuilder;


    /**
     * Initializes a new GreedyClusteringEngine. This resembles the original implementation in the
     * spectra-cluster 1.x API.
     * @param precursorTolerance The precursor tolerance to use.
     * @param thresholdStart The starting threshold of the clustering process.
     * @param thresholdEnd The final threshold of the clustering process.
     * @param clusteringRounds The number of clustering rounds to perform.
     * @param similarityMeasure The similarity measure to use
     * @param numberOfComparisonAssessor The numberOfComparisonAssessor to use during the clustering process.
     * @param firstRoundPredicate Predicate to use in the first clustering round to decide whether spectra should be compare.
     *                            In subsequent rounds only spectra that were compared previously are taken into consideration.
     * @throws Exception Thrown in case no CDF can be loaded for the passed similarity measure.
     */
    public GreedyClusteringEngine(int precursorTolerance, float thresholdStart, float thresholdEnd,
                                  int clusteringRounds, IBinarySpectrumSimilarity similarityMeasure,
                                  INumberOfComparisonAssessor numberOfComparisonAssessor,
                                  IComparisonPredicate<ICluster> firstRoundPredicate,
                                  int consensusSpectrumNoiseFilterIncrement)
            throws Exception {
        this.precursorTolerance = precursorTolerance;
        this.thresholdStart = 1 - thresholdStart;
        this.thresholdEnd = 1 - thresholdEnd;
        this.clusteringRounds = clusteringRounds;
        this.similarityMeasure = similarityMeasure;
        this.numberOfComparisonAssessor = numberOfComparisonAssessor;
        this.firstRoundPredicate = firstRoundPredicate;
        this.consensusSpectrumNoiseFilterIncrement = consensusSpectrumNoiseFilterIncrement;

        // some sanity checks
        if (thresholdEnd > thresholdStart) {
            throw new InvalidParameterException("The starting threshold must be larger than the ending threshold");
        }

        this.cdf = CumulativeDistributionFunctionFactory.getCumulativeDistributionFunctionForSimilarityMetric(similarityMeasure.getClass());
    }

    @Override
    public ICluster[] clusterSpectra(ICluster... clusters) {
        // convert all spectra to clusters
        float scoreIncrement = (thresholdEnd - thresholdStart) / (float) (clusteringRounds - 1);
        IComparisonPredicate<ICluster> currentComparisonPredicate;

        // cluster the spectra
        for (float currentThreshold = thresholdStart; currentThreshold <= thresholdEnd; currentThreshold += scoreIncrement) {
            log.debug(String.format("Merging clusters with threshold %.3f", currentThreshold));

            // set the current predicate to use
            if (currentThreshold == thresholdStart) {
                currentComparisonPredicate = firstRoundPredicate;
            } else {
                currentComparisonPredicate = new ClusterIsKnownComparisonPredicate();
            }

            // do the clustering - ie. the merging
            clusters = mergeSimilarClusters(clusters, currentThreshold, currentComparisonPredicate);

            // TODO: find a better solution than sorting between clustering rounds
            Arrays.parallelSort(clusters, Comparator.comparingInt(ICluster::getPrecursorMz));
        }

        return clusters;
    }

    //Todo: Jgriss should review which method can be remove from here.
    @Override
    public ICluster createSingleSpectrumCluster(IBinarySpectrum spectrum) {
        return convertSingleSpectrum(spectrum);
    }


    /**
     * Merges similar clusters based on the set clustering threshold.
     * @param clustersToMerge The clusters to merge.
     * @param similarityThreshold The similarity threshold to use.
     * @param predicate The predicate to use to decide which clusters to compare
     * @return An array of clusters representing the merged result. Warning: The original objects are changed!
     */
    private ICluster[] mergeSimilarClusters(ICluster[] clustersToMerge, double similarityThreshold, IComparisonPredicate<ICluster> predicate) {
        // clusters can never be split
        ICluster[] mergedClusters = new GreedySpectralCluster[clustersToMerge.length];
        int mergedClusterSize = 0;
        // the current offset to use based on the set precursor tolerance
        int mergedClusterPrecursorOffset = 0;
        int lastMz = 0;
        int maxSortTolerance = Math.round((float) precursorTolerance / 5);

        // merge similar clusters
        for (ICluster clusterToMerge : clustersToMerge) {
            if (mergedClusterSize < 1) {
                lastMz = clusterToMerge.getPrecursorMz();
                mergedClusters[mergedClusterSize++] = clusterToMerge;
                continue;
            }

            // TODO: ideally, we should not need this check
            // make sure everything is sorted according to precursor m/z
            if (lastMz - clusterToMerge.getPrecursorMz() > maxSortTolerance) {
                throw new IllegalStateException("Clusters are not sorted according to precursor m/z");
            }
            lastMz = clusterToMerge.getPrecursorMz();

            boolean isClusterMerged = false;

            // compare against all existing cluster
            for (int i = mergedClusterPrecursorOffset; i < mergedClusterSize; i++) {
                ICluster existingCluster = mergedClusters[i];

                // check about the precursor tolerance
                if (Math.abs(existingCluster.getPrecursorMz() - clusterToMerge.getPrecursorMz()) > precursorTolerance) {
                    mergedClusterPrecursorOffset = i + 1;
                    continue;
                }

                // apply the predicate
                if (!predicate.test(existingCluster, clusterToMerge)) {
                    continue;
                }

                // calculate the score
                // TODO: in the previous version we stored all filtered consensus spectra of existing clusters
                double similarity = similarityMeasure.correlation(clusterToMerge.getConsensusSpectrum(), existingCluster.getConsensusSpectrum());

                // if it is a save match, merge the cluster
                if (cdf.isSaveMatch(similarity, numberOfComparisonAssessor.getNumberOfComparisons(clusterToMerge.getPrecursorMz(), mergedClusterSize - mergedClusterPrecursorOffset + 1), similarityThreshold)) {
                    // merge the cluster
                    mergedClusters[i].mergeCluster(clusterToMerge);
                    isClusterMerged = true;
                    break;
                }

                // save the comparison
                existingCluster.saveComparisonResult(clusterToMerge.getId(), (float) similarity);
                clusterToMerge.saveComparisonResult(existingCluster.getId(), (float) similarity);
            }

            // if the cluster hasn't been merged, store it
            if (!isClusterMerged) {
                mergedClusters[mergedClusterSize++] = clusterToMerge;
            }
        }

        // now that the clustering round is done, shrink the array
        return(Arrays.copyOf(mergedClusters, mergedClusterSize));
    }

    /**
     * Converts the spectra objects into an array of cluster objects each only
     * containing a single spectrum.
     * @param spectra The spectra to convert.
     * @return An array of ICluster
     */
    private GreedySpectralCluster[] convertSpectraToCluster(IBinarySpectrum[] spectra) {
        return Arrays.stream(spectra).map(s -> {
            ICluster cluster = new GreedySpectralCluster(new GreedyClusteringConsensusSpectrum(s.getUUI(),
                    GreedyClusteringConsensusSpectrum.MIN_PEAKS_TO_KEEP ,
                    GreedyClusteringConsensusSpectrum.MIN_PEAKS_TO_KEEP,
                    consensusSpectrumNoiseFilterIncrement,
                    COMPARISON_FILTER));
            cluster.addSpectra(s);
            return cluster;
        }).toArray(GreedySpectralCluster[]::new);
    }

    /**
     * Converts the spectra objects into an array of cluster objects each only
     * containing a single spectrum.
     * @param spectrum The spectrum to be converted
     * @return An array of ICluster
     */
    private GreedySpectralCluster convertSingleSpectrum(IBinarySpectrum spectrum) {
        GreedySpectralCluster greedyCluster = new GreedySpectralCluster(new GreedyClusteringConsensusSpectrum(spectrum.getUUI(),
                GreedyClusteringConsensusSpectrum.MIN_PEAKS_TO_KEEP,
                GreedyClusteringConsensusSpectrum.MIN_PEAKS_TO_KEEP,
                consensusSpectrumNoiseFilterIncrement,
                COMPARISON_FILTER));
        greedyCluster.addSpectra(spectrum);
        return greedyCluster;
    }

    @Override
    public ICluster newCluster(io.github.bigbio.pgatk.io.common.cluster.ICluster cluster){
        GreedySpectralCluster greedyCluster = new GreedySpectralCluster(cluster.getId(), cluster.getSpectrumReferences().stream().map(Spectrum::getId).collect(Collectors.toSet()), initGreedyConsensusBuilder(cluster),
                null, (float) 0.1);

        return greedyCluster;
    }

    private IClusteringConsensusSpectrumBuilder initGreedyConsensusBuilder(io.github.bigbio.pgatk.io.common.cluster.ICluster cluster) {
        IClusteringConsensusSpectrumBuilder greedySpectrumBuilder = new GreedyClusteringConsensusSpectrum(cluster.getId(), null, null, COMPARISON_FILTER,
                -1, -1, null, false, cluster.getSpecCount(), 0, 0, 0,
                GreedyClusteringConsensusSpectrum.MIN_PEAKS_TO_KEEP, GreedyClusteringConsensusSpectrum.MIN_PEAKS_TO_KEEP, -1);
        return greedySpectrumBuilder;
    }

    @Override
    public int getPrecursorTolerance() {
        return 0;
    }
}
