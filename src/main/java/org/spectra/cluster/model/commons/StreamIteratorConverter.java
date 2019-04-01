package org.spectra.cluster.model.commons;

import org.bigbio.pgatk.io.common.MzIterableReader;
import org.bigbio.pgatk.io.common.Spectrum;
import org.spectra.cluster.model.spectra.IBinarySpectrum;


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
    private final Iterator<Tuple<File, MzIterableReader>> iterator;
    private ITuple currentTuple;
    // cache the spectrum to enable filtering
    private ITuple nextSpectrum;

    public StreamIteratorConverter(Stream<Tuple<File, MzIterableReader>> iterator, Converter<? super ITuple, ? extends IBinarySpectrum> converter) {
        this.converter = converter;
        this.iterator = iterator.iterator();
        if (this.iterator.hasNext())
            currentTuple = this.iterator.next();
        if (currentTuple != null)
            this.nextSpectrum = fetchNextValidSpectrum();
        else
            // no spectra to fetch?
            this.nextSpectrum = null;
    }

    private ITuple fetchNextValidSpectrum() {
        ITuple s = fetchNextSpectrum();

        if (s == null) {
            return null;
        }

        // check whether it is valid
        Spectrum spec = (Spectrum) s.getValue();

        if (!isSpecValid(spec)) {
            return fetchNextValidSpectrum();
        }

        return s;
    }

    private boolean isSpecValid(Spectrum s) {
        return s.getPrecursorCharge() != null && s.getPrecursorMZ() != null;
    }

    private ITuple fetchNextSpectrum() {
        if(((Iterator)currentTuple.getValue()).hasNext())
            return new Tuple(currentTuple.getKey(), ((Iterator)currentTuple.getValue()).next());
        if(this.iterator.hasNext()){
            currentTuple = this.iterator.next();
            if(((Iterator)currentTuple.getValue()).hasNext())
                return new Tuple(currentTuple.getKey(), ((Iterator)currentTuple.getValue()).next());
        }
        return null;
    }

    public boolean hasNext() {
        return nextSpectrum != null;
    }

    public T next() {
        try {
            // convert the current spectrum
            T result = (T) converter.convert(nextSpectrum);
            // move to the next spectrum
            nextSpectrum = fetchNextValidSpectrum();
            return result;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

}
