package org.spectra.cluster.model.commons;

import org.bigbio.pgatk.io.common.MzIterableReader;
import org.bigbio.pgatk.io.common.spectra.Spectrum;

import java.io.File;
import java.util.Iterator;

public class AbstractIteratorConverter <K, T> implements Iterator<T> {

    Iterator<Tuple<File, MzIterableReader>> iterator;
    Converter converter;

    ITuple currentTuple;
    // cache the spectrum to enable filtering
    ITuple nextSpectrum;

    ITuple fetchNextValidSpectrum() {
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

    private ITuple fetchNextSpectrum() {
        if(((Iterator) currentTuple.getValue()).hasNext())
            return new Tuple(currentTuple.getKey(), ((Iterator)currentTuple.getValue()).next());
        if(this.iterator.hasNext()){
            currentTuple = this.iterator.next();
            if(((Iterator)currentTuple.getValue()).hasNext())
                return new Tuple(currentTuple.getKey(), ((Iterator)currentTuple.getValue()).next());
        }
        return null;
    }

    public boolean isSpecValid(Spectrum s) {
        return s.getPrecursorCharge() != null && s.getPrecursorMZ() != null;
    }

    @Override
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

    public boolean hasNext() {
        return nextSpectrum != null;
    }

}
