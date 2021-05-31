package org.spectra.cluster.io;

import io.github.bigbio.pgatk.io.properties.IPropertyStorage;
import io.github.bigbio.pgatk.io.properties.InMemoryPropertyStorage;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.spectra.cluster.cdf.MinNumberComparisonsAssessor;
import org.spectra.cluster.engine.GreedyClusteringEngine;
import org.spectra.cluster.io.cluster.old_writer.DotClusteringWriter;
import org.spectra.cluster.io.cluster.old_writer.IClusterWriter;
import org.spectra.cluster.io.spectra.MzSpectraReader;
import org.spectra.cluster.model.cluster.ICluster;
import org.spectra.cluster.model.consensus.GreedyClusteringConsensusSpectrum;
import org.spectra.cluster.predicates.ShareHighestPeaksClusterPredicate;
import org.spectra.cluster.similarity.CombinedFisherIntensityTest;
import org.spectra.cluster.util.ClusteringParameters;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;

public class DotClusteringWriterTest {

    private List<ICluster> spectra = new ArrayList<>(30);
    private IPropertyStorage storage = new InMemoryPropertyStorage();
    private GreedyClusteringEngine engine;

    @Before
    public void setUp() throws Exception {
        File mgfFile = new File(DotClusteringWriterTest.class.getClassLoader().getResource("same_sequence_cluster.mgf").toURI());
        engine = new GreedyClusteringEngine(10_000, 1, 0.99f,
                5, new CombinedFisherIntensityTest(), new MinNumberComparisonsAssessor(10000),
                new ShareHighestPeaksClusterPredicate(5), GreedyClusteringConsensusSpectrum.NOISE_FILTER_INCREMENT);

        MzSpectraReader reader = new MzSpectraReader(new ClusteringParameters(), mgfFile);
        Iterator<ICluster> iterator = reader.readClusterIterator(storage);

        while (iterator.hasNext()) {
            ICluster s = iterator.next();
            spectra.add(s);
        }

        spectra.sort(Comparator.comparingInt(ICluster::getPrecursorMz));
    }

    @Test
    public void testWritingClustering() throws Exception {

        ICluster[] clusters = engine.clusterSpectra(spectra.toArray(new ICluster[spectra.size()]));

        Assert.assertEquals(8, clusters.length);

        // write everything to a test file
        Path outputFile = Files.createTempFile("spectra_cluster_test_", ".clustering");

        IClusterWriter writer = new DotClusteringWriter(outputFile, false, storage);

        writer.appendClusters(clusters);

        writer.close();

        // read the file back in
        List<String> lines = Files.readAllLines(outputFile);

        Assert.assertEquals(223, lines.size());

        Files.delete(outputFile);
    }
}
