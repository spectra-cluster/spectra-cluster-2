package org.spectra.cluster.io.cluster;

import org.bigbio.pgatk.io.common.PgatkIOException;
import org.bigbio.pgatk.io.mapcache.IMapStorage;

public class EcacheClusterStorage implements IMapStorage {


    @Override
    public long storageSize() {
        return 0;
    }

    @Override
    public void close() throws PgatkIOException {

    }

    @Override
    public void put(String key, Object value) throws PgatkIOException {

    }

    @Override
    public Object get(String key) throws PgatkIOException {
        return null;
    }

    @Override
    public void cleanStorage() throws PgatkIOException {

    }
}
