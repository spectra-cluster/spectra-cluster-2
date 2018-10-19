package org.spectra.cluster.model.commons;

import org.spectra.cluster.model.spectra.IBinarySpectrum;
import uk.ac.ebi.pride.tools.jmzreader.model.Spectrum;

import java.io.File;
import java.util.Iterator;
import java.util.stream.Stream;

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
public class StreamIteratorConverter<K, T> implements Iterator<T> {

    private final Converter<? super ITuple, ? extends IBinarySpectrum> converter;
    private final Iterator<Tuple<File, Iterator<Spectrum>>> iterator;
    private ITuple currentTuple;

    public StreamIteratorConverter(Stream<Tuple<File, Iterator<Spectrum>>> iterator, Converter<? super ITuple, ? extends IBinarySpectrum> converter) {
        this.converter = converter;
        this.iterator = iterator.iterator();
        if(this.iterator.hasNext())
            currentTuple = this.iterator.next();
    }

    public boolean hasNext() {
        if(((Iterator)currentTuple.getValue()).hasNext())
            return true;
        if(this.iterator.hasNext()){
            currentTuple = this.iterator.next();
            return true;
        }
        return false;
    }

    public T next() {
        try {
            ITuple value = new Tuple(currentTuple.getKey(), ((Iterator)currentTuple.getValue()).next());
            return (T) converter.convert(value);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

}
