package org.spectra.cluster.io.cluster;

import com.spotify.sparkey.CompressionType;
import com.spotify.sparkey.Sparkey;
import com.spotify.sparkey.SparkeyReader;
import com.spotify.sparkey.SparkeyWriter;
import org.bigbio.pgatk.io.common.PgatkIOException;
import org.bigbio.pgatk.io.mapcache.IMapStorage;
import org.spectra.cluster.exceptions.SpectraClusterException;
import org.spectra.cluster.model.cluster.GreedySpectralCluster;
import org.spectra.cluster.model.cluster.ICluster;
import org.spectra.cluster.util.ClusterUtils;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.HashSet;

public class SparkKeyClusterStorage implements IMapStorage {

    private final File dbDirectory;
    File dbFile;
    HashSet<SparkeyWriter> writerSet = new HashSet<>();
    HashSet<SparkeyReader> readerSet = new HashSet<>();
    long entryCounter = 0;
    Class clusterClass;

    private final Charset DEFAULT_CHARSET = StandardCharsets.UTF_8;

    public SparkKeyClusterStorage(File dbDirectory, Class clusterClass) throws IOException{
        dbFile = new File(dbDirectory, "properties-" + System.nanoTime() + ".spi");
        this.dbDirectory = dbDirectory;
        this.clusterClass = clusterClass;
    }

    private final ThreadLocal<SparkeyWriter> writers = new ThreadLocal<SparkeyWriter>() {
        @Override
        protected SparkeyWriter initialValue() {
            try {
                SparkeyWriter writer = Sparkey.appendOrCreate(dbFile, CompressionType.SNAPPY, 512);
                writerSet.add(writer);
                return writer;
            } catch (IOException ex) {
                throw new RuntimeException(ex);
            }
        }
    };

    private final ThreadLocal<SparkeyReader> readers = new ThreadLocal<SparkeyReader>() {
        @Override
        protected SparkeyReader initialValue() {
            try {
                SparkeyReader reader = Sparkey.open(dbFile);
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
    public void put(String key, Object value) throws PgatkIOException{
        ICluster cluster = (ICluster) value;
        try {
            writers.get().put( serialize(key), cluster.toBytes());
        }catch (IOException | SpectraClusterException ex){
            throw new PgatkIOException("Error wiring the following property - " + key + " " + cluster.getId() + "error " + ex.getMessage());
        }
        entryCounter++;
    }

    @Override
    public ICluster get(String key) throws PgatkIOException {
        try{
            return deserialize(readers.get().getAsByteArray(serialize(key)));
        }catch (IOException ex){
            throw new PgatkIOException("Error retrieving the value for key -- " + key );
        }
    }

    @Override
    public void cleanStorage() throws PgatkIOException {
       entryCounter = 0;
    }

    @Override
    public long storageSize() {
        return entryCounter;
    }

    @Override
    public void close() throws PgatkIOException{
        for (SparkeyWriter w : writerSet) {
            try {
                w.writeHash();
                w.close();
            } catch (IOException ex) {
                // do nothings.
            }
        }
        for (SparkeyReader r : readerSet) {
            r.close();
        }

        ClusterUtils.cleanFilePersistence(dbDirectory);
        if(dbFile != null && dbFile.exists()){
            dbFile.deleteOnExit();
        }
    }

    public void flush() throws PgatkIOException{
        try {
            writers.get().flush();
            writers.get().writeHash();
        }catch (IOException ex){
            throw new PgatkIOException("Error wiring the SparkKey DB -- " + ex.getMessage());
        }

    }
}
