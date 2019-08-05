package org.spectra.cluster.io.cluster;

import org.spectra.cluster.model.cluster.ICluster;

import java.io.Serializable;
import java.util.Optional;

/**
 * This code is licensed under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * ==Overview==
 *
 * @author ypriverol on 17/10/2018.
 */
public interface IClusterStorage extends Serializable {

    /**
     * Add the corresponding ICluster to the HashMap
     * @param key identifier of the Cluster
     * @return ICluster
     */
    void storeCluster(String key, ICluster cluster);

    /**
     * Return the corresponding ICluster from the HashMap
     * @param key identifier of the Cluster
     * @return ICluster
     */
    Optional<ICluster> getCluster(String key);

    /**
     * Delete specific key from the Map
     * @param key key to be delete
     * @return return true is the clusters has been deleted .
     */
    boolean deleteCluster(String key);

    /**
     * Delete specific keys from the Map
     * @param keys key to be delete
     * @return return true if the cluster has been deleted.
     */
    boolean deleteCluster(String... keys);

    /**
     * Get the number of Spectra in the Map
     * @return number of Spectra
     */
    int size();

    /**
     * Save clusters to a BinaryFile
     * @param filePath save clusters to a File
     */
    void saveToFile(String filePath);

    /**
     * Read clusters from Binary File
     * @param filePath file
     */
    void readFromFile(String filePath);



}
