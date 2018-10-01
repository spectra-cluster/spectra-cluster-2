package org.spectra.cluster.io;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.spectra.cluster.cdf.MinNumberComparisonsAssessor;
import org.spectra.cluster.engine.GreedyClusteringEngine;
import org.spectra.cluster.engine.IClusteringEngine;
import org.spectra.cluster.model.cluster.ICluster;
import org.spectra.cluster.model.spectra.IBinarySpectrum;
import org.spectra.cluster.similarity.CombinedFisherIntensityTest;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class DotClusteringWriterTest {
    private List<IBinarySpectrum> spectra = new ArrayList<>(30);
    private IPropertyStorage storage = new InMemoryPropertyStorage();

    @Before
    public void setUp() throws Exception {
        File mgfFile = new File(DotClusteringWriterTest.class.getClassLoader().getResource("same_sequence_cluster.mgf").toURI());
        MzSpectraReader reader = new MzSpectraReader(mgfFile);
        Iterator<IBinarySpectrum> iterator = reader.readBinarySpectraIterator(storage);

        while (iterator.hasNext()) {
            IBinarySpectrum s = iterator.next();
            spectra.add(s);
        }
    }

    @Test
    public void testWritingClustering() throws Exception {
        IClusteringEngine engine = new GreedyClusteringEngine(10_000, 1, 0.99f, 5, new CombinedFisherIntensityTest(), new MinNumberComparisonsAssessor(10000), 5);

        ICluster[] clusters = engine.clusterSpectra(spectra.toArray(new IBinarySpectrum[0]));

        Assert.assertEquals(11, clusters.length);

        // write everything to a test file
        Path outputFile = Files.createTempFile("spectra_cluster_test_", ".clustering");

        IClusterWriter writer = new DotClusteringWriter(outputFile, false, storage);

        writer.appendClusters(clusters);

        writer.close();

        // read the file back in
        List<String> lines = Files.readAllLines(outputFile);

        Assert.assertEquals(247, lines.size());

        Files.delete(outputFile);
    }
}
