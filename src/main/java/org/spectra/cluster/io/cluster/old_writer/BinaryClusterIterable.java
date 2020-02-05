package org.spectra.cluster.io.cluster.old_writer;

import org.spectra.cluster.model.cluster.ICluster;

import java.io.ObjectInputStream;
import java.util.Iterator;

/**
 * Created by jg on 15.05.15.
 */
public class BinaryClusterIterable implements Iterable<ICluster> {

    private final ObjectInputStream objectInputStream;
    private Iterator<ICluster> thisIterator;
    private String nextClassName;


    public BinaryClusterIterable(ObjectInputStream objectInputStream) {
        this.objectInputStream = objectInputStream;
        this.thisIterator = new BinaryClusterIterator();
    }

    @Override
    public Iterator<ICluster> iterator() {
        return thisIterator;
    }

    protected class BinaryClusterIterator implements Iterator<ICluster> {
        @Override
        public boolean hasNext() {
            if (nextClassName == null) {
                try {
                    nextClassName = (String) objectInputStream.readObject();
                } catch (Exception e) {
                    return false;
                }
            }

            return (nextClassName != null && !"END".equals(nextClassName));
        }

        @Override
        public ICluster next() {
            try {
                ICluster returnCluster = BinaryClusterParser.INSTANCE.parseNextCluster(objectInputStream, nextClassName);
                nextClassName = (String) objectInputStream.readObject();
                return returnCluster;
            } catch (Exception e) {
                throw new RuntimeException("Tried to parse corrupt file (not terminated correctly)", e);
            }
        }

        @Override
        public void remove() {
            //todo: provide implementation
        }
    }

}
