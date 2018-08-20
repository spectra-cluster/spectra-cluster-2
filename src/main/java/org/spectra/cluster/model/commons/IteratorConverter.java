package org.spectra.cluster.model.commons;

import java.util.Iterator;

/**
 * This code is licensed under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 *
 * This class enable to convert an {@link Iterator} of type F to an {@link Iterator} of type
 * T . It use the {@link Converter} interface. This class is used to transform an Iterator of Spectrum
 * to a Iterator of BinarySpectrum in {@link org.spectra.cluster.io.MzSpectraReader}
 *
 *
 * @author ypriverol on 14/08/2018.
 */
public class IteratorConverter<F, T> implements Iterator<T> {

    private final Converter<? super F, ? extends T> converter;
    private final Iterator<F> iterator;

    public IteratorConverter(Iterator<F> iterator, Converter<? super F, ? extends T> converter) {
        this.converter = converter;
        this.iterator = iterator;
    }

    public boolean hasNext() {
        return iterator.hasNext();
    }

    public T next() {
        try {
            return converter.convert(iterator.next());
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public void remove() {
        iterator.remove();
    }
}

