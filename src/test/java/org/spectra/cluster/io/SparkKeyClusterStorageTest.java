package org.spectra.cluster.io;

import io.github.bigbio.pgatk.io.mapcache.IMapStorage;
import io.github.bigbio.pgatk.io.properties.IPropertyStorage;
import io.github.bigbio.pgatk.io.properties.InMemoryPropertyStorage;
import org.junit.*;
import org.junit.rules.ExpectedException;
import org.spectra.cluster.exceptions.SpectraClusterException;
import org.spectra.cluster.io.cluster.ClusterStorageFactory;
import org.spectra.cluster.io.spectra.MzSpectraReader;
import org.spectra.cluster.model.cluster.GreedySpectralCluster;
import org.spectra.cluster.model.cluster.ICluster;
import org.spectra.cluster.util.ClusterUtils;
import org.spectra.cluster.util.ClusteringParameters;

import java.io.File;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

public class SparkKeyClusterStorageTest {
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

    @After
    public void cleanUp() throws Exception {
        // remove the test directory
        ClusterUtils.cleanFilePersistence(testDir.toFile());

        if (Files.exists(testDir))
            Files.delete(testDir);
    }

    @Rule
    public ExpectedException expectedException = ExpectedException.none();

    @Test
    public void testReadWriteClusters() throws Exception {
        /**
         * ----------------- Store the clusters of the first MGF file -----------------------
         */
        IMapStorage<ICluster> clusterStorage = ClusterStorageFactory.buildDynamicStorage(testDir.toFile(), GreedySpectralCluster.class);
        // ignore the property storage for now
        IPropertyStorage propertyStorage = new InMemoryPropertyStorage();

        MzSpectraReader reader = new MzSpectraReader(new ClusteringParameters(), new File(this.mgfFiles[0]));

        // create the iterator
        Iterator<ICluster> iterator = reader.readClusterIterator(propertyStorage);

        // keep track of the cluster ids
        Set<String> clusterIds = new HashSet<>(200);

        while (iterator.hasNext()) {
            ICluster cluster = iterator.next();
            String storageId = cluster.getId();

            // store the cluster
            clusterStorage.put(storageId, cluster);
            clusterIds.add(storageId);
        }

        System.out.println("Storage size = " + Long.toString(clusterStorage.storageSize()));
        clusterStorage.close();

        /**
         * ------------------ Load the clusters again ----------------------
         */
        IMapStorage<ICluster> localStorage = ClusterStorageFactory.openDynamicStorage(testDir.toFile(), GreedySpectralCluster.class);

        // check entry count
        Assert.assertEquals(158, localStorage.storageSize());

        ICluster cluster = localStorage.get("does-not-exist");
        Assert.assertNull(cluster);

        // load the existing clusters
        for (String clusterId : clusterIds) {
            ICluster loadedCluster = localStorage.get(clusterId);
            Assert.assertNotNull(loadedCluster);
            Assert.assertEquals(clusterId, loadedCluster.getId());
        }

        localStorage.close();

        /**
         * ------------- Creating a new storage should fail ------------------
         */
        expectedException.expect(SpectraClusterException.class);
        expectedException.expectMessage("Error creating the SparkKey Cluster storage -- Object storage directory already contains a storage.");
        IMapStorage<ICluster> failedStorage = ClusterStorageFactory.buildDynamicStorage(testDir.toFile(), GreedySpectralCluster.class);
    }

    @Test
    public void testCreateTemporaryStorage() throws Exception {
        /**
         * ----------------- Store the clusters of the first MGF file -----------------------
         */
        Path temporaryTestDir = Files.createTempDirectory("clusters-temp-");
        IMapStorage<ICluster> clusterStorage = ClusterStorageFactory.buildTemporaryDynamicStorage(temporaryTestDir.toFile(), GreedySpectralCluster.class);

        // ignore the property storage for now
        IPropertyStorage propertyStorage = new InMemoryPropertyStorage();

        MzSpectraReader reader = new MzSpectraReader(new ClusteringParameters(), new File(this.mgfFiles[0]));

        // create the iterator
        Iterator<ICluster> iterator = reader.readClusterIterator(propertyStorage);

        // keep track of the cluster ids
        Set<String> clusterIds = new HashSet<>(200);

        while (iterator.hasNext()) {
            ICluster cluster = iterator.next();
            String storageId = cluster.getId();

            // store the cluster
            clusterStorage.put(storageId, cluster);
            clusterIds.add(storageId);
        }

        // close the storage
        clusterStorage.close();

        // make sure the directory is empty
        long fileCount = Files.list(temporaryTestDir).count();
        Assert.assertEquals(0, fileCount);

        Files.delete(temporaryTestDir);
    }
}
