package org.spectra.cluster.io.result;

import io.github.bigbio.pgatk.io.properties.IPropertyStorage;
import org.spectra.cluster.io.cluster.ObjectDBGreedyClusterStorage;

import java.nio.file.Path;

/**
 * Classes that write the clustering result into
 * a specific format.
 *
 * @author jg
 */
public interface IClusteringResultWriter {
    /**
     * Convert a clustering result into a specific format.
     *
     * @param resultFile The final result file that will be created. This file must not exist.
     * @param clusterStorage The final clustering result as an ObjectDBGreedyClusterStorage.
     * @param spectraPropertyStorage The spectrum property storage.
     * @throws Exception
     */
    public void writeResult(Path resultFile, ObjectDBGreedyClusterStorage clusterStorage, IPropertyStorage spectraPropertyStorage) throws Exception;
}
