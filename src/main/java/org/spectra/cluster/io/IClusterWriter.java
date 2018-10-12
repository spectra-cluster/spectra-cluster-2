package org.spectra.cluster.io;

import org.spectra.cluster.model.cluster.ICluster;

import java.io.IOException;

/**
 * Defines a class to write clusters to an output file. The output
 * file's path, clustering setting specific properties that are written
 * to the header, etc. should be set in the constructor.
 *
 * @author jg
 */
public interface IClusterWriter {
    /**
     * Clusters to append to the output file.
     * @param clusters The clusters to write.
     */
    void appendClusters(ICluster... clusters) throws IOException;

    /**
     * Close the file explicitly.
     */
    void close() throws IOException;
}
