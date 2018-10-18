package org.spectra.cluster.io.cluster;

import java.io.IOException;
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
public class ClusterStorageFactory {

    Optional<IClusterStorage> buildDynamicStorage(){
        try {
            return Optional.of(new BinaryClusterStorage(true, -1, null));
        } catch (IOException e) {
            e.printStackTrace();
        }
        return Optional.empty();
    }

    Optional<IClusterStorage> buildStaticStorage(){
        try {
            return Optional.of(new BinaryClusterStorage(false, -1, null));
        } catch (IOException e) {
            e.printStackTrace();
        }
        return Optional.empty();
    }


    Optional<IClusterStorage> buildStaticStorage(int numberClusters){
        try {
            return Optional.of(new BinaryClusterStorage(false, -1, null));
        } catch (IOException e) {
            e.printStackTrace();
        }
        return Optional.empty();
    }



}
