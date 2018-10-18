package org.spectra.cluster.io.cluster;

import java.io.IOException;
import java.util.Optional;

/**
 * This code is licensed under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 * <p>
 *
 * This class provide a Factory to retrieve the different flavours of {@link BinaryClusterStorage}.
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 *
 *
 * @author ypriverol on 17/10/2018.
 */
public class ClusterStorageFactory {

    /**
     * Create a Dynamic Storage for the clusters. Depending on erros in the
     * file system. This can return a null value.
     *
     * @return BinaryClusterStorage
     */
    public static Optional<IClusterStorage> buildDynamicStorage(){
        try {
            return Optional.of(new BinaryClusterStorage(true, -1, null));
        } catch (IOException e) {
            e.printStackTrace();
        }
        return Optional.empty();
    }

    /**
     * Create a Static Storage for the clusters. The Static Storage is really fast but it demands
     * pre-allocation of the number of entries that will be storage.
     * @return BinaryClusterStorage
     */
    public static Optional<IClusterStorage> buildStaticStorage(){
        try {
            return Optional.of(new BinaryClusterStorage(false, -1, null));
        } catch (IOException e) {
            e.printStackTrace();
        }
        return Optional.empty();
    }


    /**
     * Return an Static Storage for the number of clusters that will be process.
     * @param numberClusters Number of Clusters
     * @return BinaryClusterStorage
     */
    public static Optional<IClusterStorage> buildStaticStorage(int numberClusters){
        try {
            return Optional.of(new BinaryClusterStorage(false, numberClusters, null));
        } catch (IOException e) {
            e.printStackTrace();
        }
        return Optional.empty();
    }


    /**
     * Create a Dynamic Storage for the clusters. Depending on erros in the
     * file system. This can return a null value.
     * @param  filePathName file Path for the file
     * @return BinaryClusterStorage
     */
    public static Optional<IClusterStorage> buildDynamicStorage(String filePathName){
        try {
            return Optional.of(new BinaryClusterStorage(true, -1, filePathName));
        } catch (IOException e) {
            e.printStackTrace();
        }
        return Optional.empty();
    }

    /**
     * Create a Static Storage for the clusters. The Static Storage is really fast but it demands
     * pre-allocation of the number of entries that will be storage.
     * @return BinaryClusterStorage
     */
    public static Optional<IClusterStorage> buildStaticStorage(String filePathName){
        try {
            return Optional.of(new BinaryClusterStorage(false, -1, filePathName));
        } catch (IOException e) {
            e.printStackTrace();
        }
        return Optional.empty();
    }


    /**
     * Return an Static Storage for the number of clusters that will be process.
     * @param numberClusters Number of Clusters
     * @param filePathName File Path Name
     * @return BinaryClusterStorage
     */
    public static Optional<IClusterStorage> buildStaticStorage(int numberClusters, String filePathName){
        try {
            return Optional.of(new BinaryClusterStorage(false, numberClusters, filePathName));
        } catch (IOException e) {
            e.printStackTrace();
        }
        return Optional.empty();
    }





}
