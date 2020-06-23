package org.spectra.cluster.io.cluster;

import lombok.extern.slf4j.Slf4j;
import net.openhft.chronicle.map.ChronicleMap;
import net.openhft.chronicle.map.ChronicleMapBuilder;
import io.github.bigbio.pgatk.io.common.PgatkIOException;
import io.github.bigbio.pgatk.io.mapcache.IMapStorage;
import org.spectra.cluster.model.cluster.ICluster;

import java.io.File;
import java.io.IOException;
import java.util.Iterator;
import java.util.Map;

@Slf4j
public class ChronicleMapClusterStorage implements IMapStorage<ICluster> {

    private static final double CLUSTER_SIZE = 6000 + (200 * 10);
    private static final double CLUSTER_KEY_SIZE = 36 + (100 * 2);
    private final boolean deleteOnClose;
    public  File dbFile = null;

    // This is the number of features + items
    public static final long MAX_NUMBER_FEATURES = 100_000_000;
    private File dbDirectory;
    private long numberProperties;
    private ChronicleMap<String, ICluster> clusterStorage;

    /**
     * Create a {@link net.openhft.chronicle.map.ChronicleMap} for storing properties.
     *
     * This function fails if the directory already contains a storage.
     *
     * @param dbDirectory Path to the directory that contains the properties
     * @param numberProperties estimated number of properties
     * @param deleteOnClose If set, the database file is deleted when the storage is closed.
     * @throws IOException
     */
    public ChronicleMapClusterStorage(File dbDirectory, long numberProperties, boolean deleteOnClose) throws IOException {
        this.deleteOnClose = deleteOnClose;

        if(numberProperties == -1)
            this.numberProperties = MAX_NUMBER_FEATURES;
        else
            this.numberProperties = numberProperties;

        log.info("----- CHRONICLE MAP ------------------------");
        this.dbDirectory = dbDirectory;
        dbFile = new File(dbDirectory.getAbsolutePath() + File.separator + "cluster-cm-storage.db");

        // the storage must not yet exist
        if (dbFile.exists())
            throw new IOException("Directory already contains a storage database.");

        if (deleteOnClose)
            dbFile.deleteOnExit();

        this.clusterStorage = ChronicleMapBuilder.of(String.class, ICluster.class)
                .entries(numberProperties) //the maximum number of entries for the map
                .averageKeySize(CLUSTER_KEY_SIZE)
                .averageValueSize(CLUSTER_SIZE)
                .createPersistedTo(dbFile);
    }

    /**
     * Create a {@link net.openhft.chronicle.map.ChronicleMap} for storing properties
     * @param dbDirectory Path to the directory that contains the properties
     * @throws IOException
     */
    public ChronicleMapClusterStorage(File dbDirectory, boolean useFileConfig) throws IOException {
        this.deleteOnClose = false;

        log.info("----- CHRONICLE MAP ------------------------");
        this.dbDirectory = dbDirectory;
        this.dbFile = new File(dbDirectory.getAbsolutePath() + File.separator + "cluster-cm-storage.db");

        // make sure the file exists
        if (!this.dbFile.exists())
            throw new IOException("Directory does not contain a cluster storage.");

        this.clusterStorage = ChronicleMapBuilder.of(String.class, ICluster.class)
                .averageKeySize(CLUSTER_KEY_SIZE)
                .averageValueSize(CLUSTER_SIZE)
                .recoverPersistedTo(dbFile, useFileConfig);
    }

    public static ChronicleMapClusterStorage getInstance(File dbFile, boolean useFileConfig) throws IOException{
        ChronicleMapClusterStorage chronicleMapClusterStorage = new ChronicleMapClusterStorage(dbFile, useFileConfig);
        return chronicleMapClusterStorage;
    }

    /**
     * Create a temporary {@link net.openhft.chronicle.map.ChronicleMap} for storing properties using the
     * MAX_NUMBER_FEATURES = 100M.
     * @param directoryPath Directory Path
     * @throws IOException
     */
    public ChronicleMapClusterStorage(File directoryPath) throws IOException {
        this(directoryPath, MAX_NUMBER_FEATURES, false);
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
        if(deleteOnClose && dbFile != null && dbFile.exists()){
            dbFile.delete();
        }
    }

    @Override
    public void put(String key, ICluster cluster) {
        this.clusterStorage.put(key, cluster);
    }

    @Override
    public ICluster get(String key) throws PgatkIOException {
        return this.clusterStorage.get(key);
    }

    @Override
    public void flush() throws PgatkIOException {
        // this function has no effect
    }

    public Iterator<Map.Entry<String, ICluster>> getIterator(){
        return this.clusterStorage.entrySet().iterator();
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
