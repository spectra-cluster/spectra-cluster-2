package org.spectra.cluster.model.cluster;

import org.junit.Assert;
import org.junit.Test;
import org.spectra.cluster.engine.GreedyClusteringEngine;
import org.spectra.cluster.io.spectra.MzSpectraReader;
import org.spectra.cluster.model.consensus.GreedyClusteringConsensusSpectrum;
import org.spectra.cluster.model.spectra.IBinarySpectrum;
import org.spectra.cluster.util.ClusteringParameters;

import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public class GreedySpectralClusterTest {
    @Test
    public void testSavingComparisonMatches() {
        GreedySpectralCluster cluster = new GreedySpectralCluster(new GreedyClusteringConsensusSpectrum("Test", GreedyClusteringEngine.COMPARISON_FILTER));

        Assert.assertEquals("Test", cluster.getId());
        Assert.assertEquals(0, cluster.getClusteredSpectraCount());
        Assert.assertTrue(cluster.getClusteredSpectraIds().isEmpty());

        cluster.saveComparisonResult("1", 1);
        cluster.saveComparisonResult("2", 2);

        Assert.assertEquals(2, cluster.getComparisonMatches().size());

        for (int i = 3; i <= 40; i++) {
            cluster.saveComparisonResult(String.valueOf(i), i);
        }

        Assert.assertEquals(GreedySpectralCluster.SAVED_COMPARISON_MATCHES, cluster.getComparisonMatches().size());
        Assert.assertTrue(cluster.isKnownComparisonMatch("30"));
        Assert.assertFalse(cluster.isKnownComparisonMatch("10"));
    }

    @Test
    public void testAddingSpectra() throws Exception {
        File testFile = new File(Objects.requireNonNull(GreedySpectralClusterTest.class.getClassLoader().getResource("same_sequence_cluster.mgf")).toURI());
        MzSpectraReader reader = new MzSpectraReader(new ClusteringParameters(), testFile);

        Iterator<IBinarySpectrum> spectrumIterator = reader.readBinarySpectraIterator();
        List<IBinarySpectrum> spectra = new ArrayList<>();

        while (spectrumIterator.hasNext()) {
            spectra.add(spectrumIterator.next());
        }

        // put all spectra to one cluster
        GreedySpectralCluster cluster = new GreedySpectralCluster(new GreedyClusteringConsensusSpectrum("test", GreedyClusteringEngine.COMPARISON_FILTER));
        cluster.addSpectra(spectra.toArray(new IBinarySpectrum[0]));

        Assert.assertEquals(spectra.size(), cluster.getClusteredSpectraCount());
        Assert.assertTrue(cluster.getClusteredSpectraIds().containsAll(spectra.stream().map(IBinarySpectrum::getUUI).collect(Collectors.toList())));
    }

    @Test
    public void testGetProperties() throws Exception {
        File testFile = new File(Objects.requireNonNull(GreedySpectralClusterTest.class.getClassLoader().getResource("same_sequence_cluster.mgf")).toURI());
        MzSpectraReader reader = new MzSpectraReader(new ClusteringParameters(), testFile);

        Iterator<IBinarySpectrum> spectrumIterator = reader.readBinarySpectraIterator();
        List<IBinarySpectrum> spectra = new ArrayList<>();

        while (spectrumIterator.hasNext()) {
            spectra.add(spectrumIterator.next());
        }

        // put all spectra to one cluster
        GreedySpectralCluster cluster = new GreedySpectralCluster(new GreedyClusteringConsensusSpectrum("test", GreedyClusteringEngine.COMPARISON_FILTER));
        cluster.addSpectra(spectra.toArray(new IBinarySpectrum[0]));

        // get the properties
        IClusterProperties properties = cluster.getProperties();

        int precursorMz = cluster.getPrecursorMz();

        Assert.assertEquals(cluster.getId(), properties.getId());
        Assert.assertEquals(precursorMz, properties.getPrecursorMz());
        Assert.assertEquals(cluster.getPrecursorCharge(), properties.getPrecursorCharge());
    }
}
