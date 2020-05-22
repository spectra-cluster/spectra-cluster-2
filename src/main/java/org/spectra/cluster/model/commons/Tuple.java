package org.spectra.cluster.model.commons;

/**
 * This code is licensed under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * ==Overview==
 *
 * @author ypriverol on 19/10/2018.
 */

import java.util.Objects;

/**
 * Tuple stores two elements.
 *
 * @quthor Yasset Perez-Riverol
 * @version $Id$
 */

public class Tuple <K, V> implements ITuple<K, V> {
    private K key;
    private V value;

    private int hashCode;

    public Tuple(K key, V value) {
        this.key = key;
        this.value = value;
        this.hashCode = computeHashCode();
    }

    public K getKey() {
        return key;
    }

    public void setKey(K key) {
        this.key = key;
        this.hashCode = computeHashCode();
    }

    public V getValue() {
        return value;
    }

    public void setValue(V value) {
        this.value = value;
        this.hashCode = computeHashCode();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Tuple)) return false;

        Tuple tuple = (Tuple) o;

        return !(!Objects.equals(key, tuple.key)) && !(!Objects.equals(value, tuple.value));

    }

    @Override
    public int hashCode() {
        return this.hashCode;
    }

    private int computeHashCode(){
        int result = key != null ? key.hashCode() : 0;
        result = 31 * result + (value != null ? value.hashCode() : 0);
        return result;
    }
}
