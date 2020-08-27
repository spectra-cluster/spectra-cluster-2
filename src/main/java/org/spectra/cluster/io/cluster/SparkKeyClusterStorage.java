package org.spectra.cluster.io.cluster;

import com.spotify.sparkey.CompressionType;
import com.spotify.sparkey.Sparkey;
import com.spotify.sparkey.SparkeyReader;
import com.spotify.sparkey.SparkeyWriter;
import io.github.bigbio.pgatk.io.common.PgatkIOException;
import io.github.bigbio.pgatk.io.mapcache.IMapStorage;
import lombok.extern.slf4j.Slf4j;
import org.spectra.cluster.exceptions.SpectraClusterException;
import org.spectra.cluster.model.cluster.GreedySpectralCluster;
import org.spectra.cluster.model.cluster.ICluster;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.HashSet;
import java.util.concurrent.atomic.AtomicLong;

@Slf4j
public class SparkKeyClusterStorage implements IMapStorage<ICluster> {

    private final boolean deleteOnClose;
    private final File dbFile;

    // Sparkey only supports a single writer per database
    private final SparkeyWriter writer;

    // Multiple simultaneous readers are not a problem
    HashSet<SparkeyReader> readerSet = new HashSet<>();
    AtomicLong entryCounter = new AtomicLong(0);
    Class clusterClass;

    private final Charset DEFAULT_CHARSET = StandardCharsets.UTF_8;

    /**
     * Creates a new SparkKeyClusterStorage. The storage implementation requires multiple files. Therefore,
     * they should ideally be stored in a dedicated directory. The directory as a whole can then be regarded
     * as a permanent record for the clustering run.
     *
     * By default, existing storage files will be loaded and not overwritten.
     *
     * @param dbDirectory Directory to create the required files in.
     * @param clusterClass The class of the clusters stored in the storage.
     * @param openExisting If set, the constructor throws an IOException in case the database file does not exist.
     * @param deleteOnClose If set, the database files are deleted when the "close" method is called.
     * @throws IOException
     */
    public SparkKeyClusterStorage(File dbDirectory, Class clusterClass, boolean openExisting, boolean deleteOnClose)
        throws IOException{
        this.dbFile = new File(dbDirectory, "spectra-cluster_object-storage.spi");

        if (openExisting && !this.dbFile.exists())
            throw new IOException("No storage found in object storage directory.");
        if (!openExisting && this.dbFile.exists())
            throw new IOException("Object storage directory already contains a storage.");

        // indicate that the entry count is wrong if the DB exists
        if (this.dbFile.exists())
            entryCounter.set(-1);

        this.deleteOnClose = deleteOnClose;
        this.clusterClass = clusterClass;

        // create the single writer
        writer = Sparkey.appendOrCreate(dbFile, CompressionType.SNAPPY, 512);
    }

    private final ThreadLocal<SparkeyReader> readers = new ThreadLocal<SparkeyReader>() {
        @Override
        protected SparkeyReader initialValue() {
            try {
                // open using a thread-safe reader
                SparkeyReader reader = Sparkey.openThreadLocalReader(dbFile);
                readerSet.add(reader);
                return reader;
            } catch (IOException ex) {
                throw new RuntimeException(ex);
            }
        }
    };

    private byte[] serialize(String value){
        return Base64.getDecoder().decode(Base64.getEncoder().encodeToString( value.getBytes( DEFAULT_CHARSET )));
    }

    private ICluster deserialize(byte[] bytes) throws PgatkIOException {
        if(clusterClass == GreedySpectralCluster.class) {
            try {
                return GreedySpectralCluster.fromBytes(bytes);
            } catch (SpectraClusterException e) {
                throw new PgatkIOException("Class for deserialize objects not found -- " + clusterClass.toString());
            }
        }
        throw new PgatkIOException("Class for deserialize objects not found -- " + clusterClass.toString());
    }

    @Override
    public synchronized void put(String key, ICluster cluster) {
        try {
            writer.put( serialize(key), cluster.toBytes());
        }catch (IOException | SpectraClusterException ex){
            throw new IllegalStateException("Error wiring the following property - " + key + " " + cluster.getId() + "error " + ex.getMessage());
        }
        entryCounter.incrementAndGet();
    }

    @Override
    public ICluster get(String key) {
        try {
            byte[] byteObject = readers.get().getAsByteArray(serialize(key));

            if (byteObject == null) {
                return null;
            }

            return deserialize(byteObject);
        } catch (PgatkIOException | IOException ex) {
            log.error("Error retrieving the value for key -- " + key );
            return null;
        }
    }

    @Override
    public void cleanStorage() throws PgatkIOException {
        // TODO: This function does not delete any elements.
        entryCounter.set(0);
    }

    @Override
    public synchronized long storageSize() {
        // load entry count if DB exists
        if (entryCounter.get() < 0) {
            entryCounter.set(readers.get().getIndexHeader().getNumEntries());
        }

        return entryCounter.get();
    }

    @Override
    public synchronized void close() throws PgatkIOException{
        try {
            if (!deleteOnClose) {
                flush();
            }

            writer.close();
        } catch (Exception e) {
            // ignore
        }

        // close all readers
        for (SparkeyReader r : readerSet) {
            r.close();
        }

        // delete the files
        if (deleteOnClose && dbFile != null) {
            if (dbFile.exists())
                dbFile.delete();

            String logFilePath = dbFile.getAbsolutePath().replace(".spi", ".spl");

            File logFile = new File(logFilePath);
            if (logFile.exists())
                logFile.delete();
        }
    }

    @Override
    public synchronized void flush() throws PgatkIOException{
        try {
            writer.flush();
            writer.writeHash();
        } catch (IOException ex) {
            throw new PgatkIOException("Error wiring the SparkKey DB -- " + ex.getMessage());
        }

    }
}
