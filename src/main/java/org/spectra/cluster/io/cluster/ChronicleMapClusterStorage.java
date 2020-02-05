package org.spectra.cluster.io.cluster;

import lombok.extern.slf4j.Slf4j;
import net.openhft.chronicle.map.ChronicleMap;
import net.openhft.chronicle.map.ChronicleMapBuilder;
import org.bigbio.pgatk.io.common.PgatkIOException;
import org.bigbio.pgatk.io.mapcache.IMapStorage;
import org.spectra.cluster.model.cluster.ICluster;
import org.spectra.cluster.util.ClusterUtils;

import java.io.File;
import java.io.IOException;

@Slf4j
public class ChronicleMapClusterStorage<V> implements IMapStorage {

    private static final double CLUSTER_SIZE = 6000 + (200 * 10);
    private static final double CLUSTER_KEY_SIZE = 36 + (100 * 2);
    public  File dbFile = null;

    // This is the number of features + items
    public static final long MAX_NUMBER_FEATURES = 100_000_000;
    private File dbDirectory;
    private long numberProperties;
    private ChronicleMap<String, ICluster> clusterStorage;

    /**
     * Create a {@link net.openhft.chronicle.map.ChronicleMap} for storing properties
     * @param dbDirectory Path to the directory that contains the properties
     * @param numberProperties estimated number of properties
     * @throws IOException
     */
    public ChronicleMapClusterStorage(File dbDirectory, long numberProperties) throws IOException {

        if(numberProperties == -1)
            this.numberProperties = MAX_NUMBER_FEATURES;
        else
            this.numberProperties = numberProperties;

        log.info("----- CHRONICLE MAP ------------------------");
        this.dbDirectory = dbDirectory;
        dbFile = new File(dbDirectory.getAbsolutePath() + File.separator + "properties-" + System.nanoTime() + ".db");
        dbFile.deleteOnExit();
        this.clusterStorage = ChronicleMapBuilder.of(String.class, ICluster.class)
                .entries(numberProperties) //the maximum number of entries for the map
                .averageKeySize(CLUSTER_KEY_SIZE)
                .averageValueSize(CLUSTER_SIZE)
                .createPersistedTo(dbFile);

    }

    /**
     * Create a {@link net.openhft.chronicle.map.ChronicleMap} for storing properties
     * @param dbFile Path to the directory that contains the properties
     * @throws IOException
     */
    public ChronicleMapClusterStorage(File dbFile, boolean useFileConfig) throws IOException {

        log.info("----- CHRONICLE MAP ------------------------");
        this.dbFile = dbFile;
        this.clusterStorage = ChronicleMapBuilder.of(String.class, ICluster.class)
                .entries(numberProperties) //the maximum number of entries for the map
                .averageKeySize(CLUSTER_KEY_SIZE)
                .averageValueSize(CLUSTER_SIZE)
                .recoverPersistedTo(dbFile, useFileConfig);
    }

    public static ChronicleMapClusterStorage getInstance(File dbFile, boolean useFileConfig) throws IOException{
        ChronicleMapClusterStorage chronicleMapClusterStorage = new ChronicleMapClusterStorage(dbFile, useFileConfig);
        return chronicleMapClusterStorage;
    }

    /**
     * Create a {@link net.openhft.chronicle.map.ChronicleMap} for storing properties using the
     * MAX_NUMBER_FEATURES = 100M.
     * @param directoryPath Directory Path
     * @throws IOException
     */
    public ChronicleMapClusterStorage(File directoryPath) throws IOException {
        this(directoryPath, MAX_NUMBER_FEATURES);
    }

    @Override
    public long storageSize() {
        return clusterStorage.size();
    }

    /**
     * Close the DB on Disk and delete it.
     */
    @Override
    public void close() throws PgatkIOException {
        ClusterUtils.cleanFilePersistence(dbDirectory);
        if(dbFile != null && dbFile.exists()){
            dbFile.deleteOnExit();
        }
    }

    @Override
    public void put(String key, Object value) {
        ICluster cluster = (ICluster) value;
        this.clusterStorage.put(key, cluster);
    }

    @Override
    public ICluster get(String key) throws PgatkIOException {
        return this.clusterStorage.get(key);
    }

    @Override
    public void cleanStorage() throws PgatkIOException{
        try {
            log.info("----- CHRONICLE MAP ------------------------");
            dbFile.deleteOnExit();
            this.clusterStorage = ChronicleMapBuilder.of(String.class, ICluster.class)
                    //the maximum number of entries for the map
                    .entries(numberProperties)
                    .averageKeySize(64)
                    .averageValueSize(54)
                    .createPersistedTo(dbFile);
        } catch (IOException e) {
            throw new PgatkIOException("Error cleaning the ChronicleMap Store -- " + e.getMessage());
        }
    }
}
