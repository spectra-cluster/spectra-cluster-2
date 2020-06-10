package org.spectra.cluster.binning;

import org.junit.Assert;
import org.junit.Test;
import org.spectra.cluster.model.cluster.BasicClusterProperties;
import org.spectra.cluster.model.cluster.IClusterProperties;
import org.spectra.cluster.normalizer.BasicIntegerNormalizer;

public class TestSimilarSizedClusterBinner {
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
}
