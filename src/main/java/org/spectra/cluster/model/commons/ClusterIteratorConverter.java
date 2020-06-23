package org.spectra.cluster.model.commons;

import io.github.bigbio.pgatk.io.common.MzIterableReader;
import io.github.bigbio.pgatk.io.common.spectra.Spectrum;
import org.spectra.cluster.model.cluster.ICluster;

import java.io.File;
import java.util.stream.Stream;

public class ClusterIteratorConverter<K, T> extends AbstractIteratorConverter {

    public ClusterIteratorConverter(Stream<Tuple<File, MzIterableReader>> iterator, Converter<? super ITuple, ? extends ICluster> converter) {
        this.converter = converter;
        this.iterator = iterator.iterator();
        if (this.iterator.hasNext())
            currentTuple = (ITuple) this.iterator.next();
        if (currentTuple != null)
            this.nextSpectrum = fetchNextValidSpectrum();
        else
            this.nextSpectrum = null;
    }

    @Override
    public boolean isSpecValid(Spectrum s) {
        return s.getPrecursorMZ() != null;
    }
}
