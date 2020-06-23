package org.spectra.cluster.binning;

import io.github.bigbio.pgatk.io.properties.IPropertyStorage;
import io.github.bigbio.pgatk.io.properties.InMemoryPropertyStorage;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.spectra.cluster.cdf.MinNumberComparisonsAssessor;
import org.spectra.cluster.engine.GreedyClusteringEngine;
import org.spectra.cluster.filter.binaryspectrum.HighestPeakPerBinFunction;
import org.spectra.cluster.filter.rawpeaks.*;
import org.spectra.cluster.io.spectra.MzSpectraReader;
import org.spectra.cluster.model.cluster.BasicClusterProperties;
import org.spectra.cluster.model.cluster.ICluster;
import org.spectra.cluster.model.cluster.IClusterProperties;
import org.spectra.cluster.model.consensus.GreedyConsensusSpectrum;
import org.spectra.cluster.normalizer.BasicIntegerNormalizer;
import org.spectra.cluster.normalizer.MaxPeakNormalizer;
import org.spectra.cluster.normalizer.TideBinner;
import org.spectra.cluster.predicates.ShareHighestPeaksClusterPredicate;
import org.spectra.cluster.similarity.CombinedFisherIntensityTest;

import java.io.File;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

public class TestSimilarSizedClusterBinner {
    private URI[] mgfFiles;
    private Path testDir;

    @Before
    public void setUp() throws Exception {
        mgfFiles = new URI[] {
                getClass().getClassLoader().getResource("same_sequence_cluster.mgf").toURI(),
                getClass().getClassLoader().getResource("synthetic_mixed_runs.mgf").toURI()
        };

        testDir = Files.createTempDirectory("clusters-");
    }

    @Test
    public void testBasicBinning() throws Exception {
        BasicIntegerNormalizer normalizer = new BasicIntegerNormalizer();

        SimilarSizedClusterBinner binner = new SimilarSizedClusterBinner(
                BasicIntegerNormalizer.MZ_CONSTANT, 1, false);

        IClusterProperties cluster1 = new BasicClusterProperties(
                normalizer.binValue(300.123), 1, "c1"
        );
        IClusterProperties cluster2 = new BasicClusterProperties(
                normalizer.binValue(300.9), 1, "c2"
        );
        IClusterProperties cluster3 = new BasicClusterProperties(
                normalizer.binValue(301.1), 1, "c3"
        );

        IClusterProperties[] testCluster = {cluster1, cluster2, cluster3};

        String[][] bins = binner.binClusters(testCluster, false);

        Assert.assertEquals(2, bins.length);
        Assert.assertEquals(2, bins[0].length);
        Assert.assertEquals(1, bins[1].length);

        Assert.assertEquals("c3", bins[1][0]);
    }

    @Test
    public void testShiftedBinning() throws Exception {
        BasicIntegerNormalizer normalizer = new BasicIntegerNormalizer();

        SimilarSizedClusterBinner binner = new SimilarSizedClusterBinner(
                BasicIntegerNormalizer.MZ_CONSTANT, 1, false);

        IClusterProperties cluster1 = new BasicClusterProperties(
                normalizer.binValue(300.1), 1, "c1"
        );
        IClusterProperties cluster2 = new BasicClusterProperties(
                normalizer.binValue(300.9), 1, "c2"
        );
        IClusterProperties cluster3 = new BasicClusterProperties(
                normalizer.binValue(301.1), 1, "c3"
        );

        IClusterProperties[] testCluster = {cluster1, cluster2, cluster3};

        String[][] bins = binner.binClusters(testCluster, true);

        Assert.assertEquals(2, bins.length);
        Assert.assertEquals(1, bins[0].length);
        Assert.assertEquals(2, bins[1].length);

        Assert.assertEquals("c1", bins[0][0]);
    }

    @Test
    public void testCharge() throws Exception {
        BasicIntegerNormalizer normalizer = new BasicIntegerNormalizer();

        SimilarSizedClusterBinner binner = new SimilarSizedClusterBinner(
                BasicIntegerNormalizer.MZ_CONSTANT, 1, true);

        IClusterProperties cluster1 = new BasicClusterProperties(
                normalizer.binValue(300.1), 1, "c1"
        );
        IClusterProperties cluster2 = new BasicClusterProperties(
                normalizer.binValue(300.9), 2, "c2"
        );
        IClusterProperties cluster3 = new BasicClusterProperties(
                normalizer.binValue(301.1), 1, "c3"
        );

        IClusterProperties[] testCluster = {cluster1, cluster2, cluster3};

        String[][] bins = binner.binClusters(testCluster, false);

        Assert.assertEquals(3, bins.length);
    }

    @Test
    public void testMerging() throws Exception {
        BasicIntegerNormalizer normalizer = new BasicIntegerNormalizer();

        SimilarSizedClusterBinner binner = new SimilarSizedClusterBinner(
                BasicIntegerNormalizer.MZ_CONSTANT, 2, false);

        IClusterProperties cluster1 = new BasicClusterProperties(
                normalizer.binValue(300.1), 1, "c1"
        );
        IClusterProperties cluster2 = new BasicClusterProperties(
                normalizer.binValue(300.9), 2, "c2"
        );
        IClusterProperties cluster3 = new BasicClusterProperties(
                normalizer.binValue(301.1), 1, "c3"
        );
        IClusterProperties cluster4 = new BasicClusterProperties(
                normalizer.binValue(303.1), 1, "c4"
        );

        IClusterProperties[] testCluster = {cluster1, cluster2, cluster3, cluster4};

        String[][] bins = binner.binClusters(testCluster, false);

        Assert.assertEquals(2, bins.length);
        Assert.assertEquals(2, bins[0].length);
        Assert.assertEquals(2, bins[1].length);
    }

    @Test
    public void testSortedClusters() throws Exception {
        // load all clusters
        // ignore the property storage for now
        IPropertyStorage propertyStorage = new InMemoryPropertyStorage();

        IRawSpectrumFunction loadingFilter = new RemoveImpossiblyHighPeaksFunction()
                .specAndThen(new RemovePrecursorPeaksFunction(0.5))
                .specAndThen(new RawPeaksWrapperFunction(new KeepNHighestRawPeaks(40)));

        // create a basic clustering engine for testing
        GreedyClusteringEngine engine = new GreedyClusteringEngine(BasicIntegerNormalizer.MZ_CONSTANT,
                1, 0.99f, 5, new CombinedFisherIntensityTest(),
                new MinNumberComparisonsAssessor(10000), new ShareHighestPeaksClusterPredicate(5),
                GreedyConsensusSpectrum.NOISE_FILTER_INCREMENT);

        File[] inFiles = Arrays.stream(mgfFiles).map(File::new).toArray(File[]::new);

        // read all files at once
        MzSpectraReader reader = new MzSpectraReader(new TideBinner(), new MaxPeakNormalizer(),
                new BasicIntegerNormalizer(), new HighestPeakPerBinFunction(), loadingFilter,
                GreedyClusteringEngine.COMPARISON_FILTER, engine, inFiles);

        // create the iterator
        Iterator<ICluster> iterator = reader.readClusterIterator(propertyStorage);

        // keep track of the cluster ids
        List<IClusterProperties> clusterProperties = new ArrayList<>(10_000);

        Map<String, Integer> clusterIdToPrecursor = new HashMap<>();

        while (iterator.hasNext()) {
            ICluster cluster = iterator.next();
            clusterProperties.add(cluster.getProperties());
            clusterIdToPrecursor.put(cluster.getId(), cluster.getPrecursorMz());
        }

        // bin the clusters
        SimilarSizedClusterBinner binner = new SimilarSizedClusterBinner(
                BasicIntegerNormalizer.MZ_CONSTANT, 5, false);

        String[][] binnedClusterIds = binner.binClusters(clusterProperties.toArray(new IClusterProperties[0]), false);

        // ensure that the clusters are sorted
        int nBin = 1;

        for (String[] clusterIds : binnedClusterIds) {
            int previousMz = 0;

            System.out.println("------ Bin " + String.valueOf(nBin++) + " --------");

            int nCluster = 0;

            for (String clusterId : clusterIds) {
                System.out.println("------ >> Cluster " + String.valueOf(nCluster++) + " --------");

                if (clusterIdToPrecursor.get(clusterId) < previousMz) {
                    Assert.fail("Clusters not sorted according to m/z");
                }

                previousMz = clusterIdToPrecursor.get(clusterId);
            }
        }
    }
}
