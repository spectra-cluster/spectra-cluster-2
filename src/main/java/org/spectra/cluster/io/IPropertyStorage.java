package org.spectra.cluster.io;

/**
 * This interface describes a property storage used to store
 * the properties of the loaded spectra.
 *
 * @author jg
 */
public interface IPropertyStorage {
    /**
     * Store a property.
     * @param itemId The item's unique id to store the property for.
     * @param propertyName The property's name
     * @param propertyValue The property's value
     */
    void storeProperty(String itemId, String propertyName, String propertyValue);

    /**
     * Retrieve a stored property for a defined item. Retruns NULL in case
     * the property has not been set.
     * @param itemId The item's unique id to fetch the property for
     * @param propertyName The property's name.
     * @return The property's value as a String.
     * @throws IndexOutOfBoundsException In case no item with this id exists.
     */
    String getProperty(String itemId, String propertyName) throws IndexOutOfBoundsException;
}
