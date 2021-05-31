package org.spectra.cluster.io.result;

import io.github.bigbio.pgatk.io.objectdb.LongObject;
import io.github.bigbio.pgatk.io.objectdb.ObjectsDB;
import io.github.bigbio.pgatk.io.properties.IPropertyStorage;
import io.github.bigbio.pgatk.io.properties.InMemoryPropertyStorage;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.spectra.cluster.consensus.AverageConsensusSpectrumBuilder;
import org.spectra.cluster.io.cluster.ObjectDBGreedyClusterStorage;
import org.spectra.cluster.io.spectra.MzSpectraReader;
import org.spectra.cluster.model.cluster.GreedySpectralCluster;
import org.spectra.cluster.model.cluster.ICluster;
import org.spectra.cluster.util.ClusteringParameters;

import java.io.File;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

public class DotClusteringWriterTest {
    Path testDir;

    @Before
    public void setUp() throws Exception {
        testDir = Files.createTempDirectory("clusters-");
    }

    @Test
    public void testWriting() throws Exception {
        // ignore the property storage for now
        IPropertyStorage propertyStorage = new InMemoryPropertyStorage();

        URI[] mgfFiles = new URI[] {
                getClass().getClassLoader().getResource("same_sequence_cluster.mgf").toURI(),
                getClass().getClassLoader().getResource("synthetic_mixed_runs.mgf").toURI()};
        File[] inFiles = Arrays.stream(mgfFiles).map(File::new).toArray(File[]::new);

        // read all files at once
        MzSpectraReader reader = new MzSpectraReader(new ClusteringParameters(), inFiles);

        // create the iterator
        Iterator<ICluster> iterator = reader.readClusterIterator(propertyStorage);

        // create the output file
        Path clusteringResult = Paths.get(testDir.toString(), "clustering_result.cls");
        ObjectDBGreedyClusterStorage clusterStorage = new ObjectDBGreedyClusterStorage(new ObjectsDB(clusteringResult.toString(), true));

        while (iterator.hasNext()) {
            GreedySpectralCluster c = (GreedySpectralCluster) iterator.next();
            clusterStorage.addGreedySpectralCluster(LongObject.asLongHash(c.getId()), c);
        }

        clusterStorage.writeDBMode();
        clusterStorage.flush();

        // convert
        Path clusteringFile = Paths.get(testDir.toString(), "clusters.clustering");

        DotClusteringWriter resultWriter = new DotClusteringWriter(new AverageConsensusSpectrumBuilder(new ClusteringParameters()));
        resultWriter.writeResult(clusteringFile, clusterStorage, propertyStorage);

        // check that everything worked
        Assert.assertTrue(Files.exists(clusteringFile));

        List<String> lines = Files.readAllLines(clusteringFile);
        Assert.assertEquals(8568, lines.size());

        boolean found = false;
        int index = 0;

        for (;index < lines.size(); index++) {
            if (lines.get(index).equals("sequence=[EVQLVETGGGLIQPGGSLR:1]")) {
                found = true;
                break;
            }
        }

        Assert.assertTrue(found);
        String psmLine = lines.get(8);
        String[] fields = psmLine.split("\t");
        Assert.assertEquals(8, fields.length);
        Assert.assertEquals("SPEC", fields[0]);
        Assert.assertEquals("EVQLVETGGGLIQPGGSLR", fields[3]);
        Assert.assertEquals("0-UNIMOD:1", fields[7]);

    }

    @Test
    public void testCleanSequence() {
        String sequence = "+42.011EVQLVETGGGLIQPGGSLR";
        String cleanSequence = DotClusteringWriter.getCleanSequence(sequence);

        Assert.assertEquals("EVQLVETGGGLIQPGGSLR", cleanSequence);
    }
}
