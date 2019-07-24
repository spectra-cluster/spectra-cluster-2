package org.spectra.cluster.model.commons;

import org.bigbio.pgatk.io.common.MzIterableReader;
import org.spectra.cluster.model.spectra.IBinarySpectrum;

import java.io.File;
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
public class SpectrumIteratorConverter<K, T> extends AbstractIteratorConverter {

    public SpectrumIteratorConverter(Stream<Tuple<File, MzIterableReader>> iterator, Converter<? super ITuple, ? extends IBinarySpectrum> converter) {
        this.converter = converter;
        this.iterator = iterator.iterator();
        if (this.iterator.hasNext())
            currentTuple = (ITuple) this.iterator.next();
        if (currentTuple != null)
            this.nextSpectrum = fetchNextValidSpectrum();
        else
            // no spectra to fetch?
            this.nextSpectrum = null;
    }

}
