package org.spectra.cluster.io.properties;

import org.spectra.cluster.exceptions.SpectraClusterException;

import javax.swing.text.html.Option;
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
 * @author ypriverol on 18/10/2018.
 */
public class PropertyStorageFactory {

    /**
     * Get {@link InMemoryPropertyStorage} for the Storage
     * @return IPropertyStorage
     */
    public static Optional<IPropertyStorage> buildInMemoryPropertyStorage(){
        return Optional.of(new InMemoryPropertyStorage());
    }

    /**
     * Get a Static Property Storage with default version MAX_NUMBER_FEATURES
     * @return IPropertyStorage
     */
    public static IPropertyStorage buildStaticPropertyStorage() throws SpectraClusterException {
        try {
            return new MapDBPropertyStorage(false, -1);
        } catch (IOException e) {
            throw new SpectraClusterException("Error building the Dynamic Property Storage --", e.getCause());
        }
    }

    /**
     * Get a Static Property Storage with s predefined number of entries.
     * @param numberProperties Number of properties
     * @return IPropertyStorage
     */
    public static IPropertyStorage buildStaticPropertyStorage( int numberProperties) throws SpectraClusterException {
        try {
            return new MapDBPropertyStorage(false, numberProperties);
        } catch (IOException e) {
            throw new SpectraClusterException("Error building the Dynamic Property Storage --", e.getCause());
        }

    }

    /**
     * Get a Static Property Storage with s predefined number of entries.
     * @return IPropertyStorage
     */
    public static IPropertyStorage buildDynamicPropertyStorage( ) throws SpectraClusterException {
        try {
            return new MapDBPropertyStorage(true, -1);
        } catch (IOException e) {
            throw new SpectraClusterException("Error building the Dynamic Property Storage --", e.getCause());
        }
    }
}
