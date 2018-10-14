package org.spectra.cluster.io.properties;

import edu.emory.mathcs.backport.java.util.Collections;
import org.spectra.cluster.io.properties.IPropertyStorage;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * This implementation stores all properties in memory. It is therefore
 * not suggested to use this implementation in production systems but
 * primarily in testing environments or cases where only small amounts
 * of data are being processed.
 *
 * @author jg
 */
public class InMemoryPropertyStorage implements IPropertyStorage {
    protected Map<String, String> propertyStorage;
    protected final Set<String> propertyNames = new HashSet<>(20);

    public InMemoryPropertyStorage() {
        propertyStorage = new HashMap<>(20_000);
    }

    @Override
    public void storeProperty(String itemId, String propertyName, String propertyValue) {
        propertyNames.add(propertyName);
        propertyStorage.put(getStorageKey(itemId, propertyName), propertyValue);
    }

    @Override
    public String getProperty(String itemId, String propertyName) throws IndexOutOfBoundsException {
        return propertyStorage.get(getStorageKey(itemId, propertyName));
    }

    protected String getStorageKey(String itemId, String propertyName) {
        return itemId + propertyName;
    }

    @Override
    public Set<String> getAvailableProperties() {
        return Collections.unmodifiableSet(propertyNames);
    }

    public int storageSize(){
        return propertyStorage.size();
    }
}
