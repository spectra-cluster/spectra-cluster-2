package org.spectra.cluster.io;

import java.util.HashMap;
import java.util.Map;

/**
 * This implementation stores all properties in memory. It is therefore
 * not suggested to use this implementation in production systems but
 * primarily in testing environments or cases where only small amounts
 * of data are being processed.
 *
 * @author jg
 */
public class InMemoryPropertyStorage implements  IPropertyStorage {
    private final Map<String, String> propertyStorage;

    public InMemoryPropertyStorage() {
        propertyStorage = new HashMap<>(20_000);
    }

    @Override
    public void storeProperty(String itemId, String propertyName, String propertyValue) {
        propertyStorage.put(getStorageKey(itemId, propertyName), propertyValue);
    }

    @Override
    public String getProperty(String itemId, String propertyName) throws IndexOutOfBoundsException {
        return propertyStorage.get(getStorageKey(itemId, propertyName));
    }

    private String getStorageKey(String itemId, String propertyName) {
        return itemId + propertyName;
    }
}
