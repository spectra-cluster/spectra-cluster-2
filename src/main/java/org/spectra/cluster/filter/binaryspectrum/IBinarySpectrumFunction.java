package org.spectra.cluster.filter.binaryspectrum;

import org.spectra.cluster.model.spectra.IBinarySpectrum;
import java.io.Serializable;
import java.util.Objects;
import java.util.function.Function;

/**
 * This code is licensed under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 *
 * This interface provides the functionality to filter {@link org.spectra.cluster.model.spectra.IBinarySpectrum}
 * with different functions. This Interface Filter information out of the spectra. As a result a new Spectrum is
 * created. The original spectrum object will not be changed.
 *
 *
 * @author ypriverol on 16/08/2018.
 */
public interface IBinarySpectrumFunction extends Serializable, Cloneable, Function<IBinarySpectrum, IBinarySpectrum> {

    /**
     * Filter an specific {@link IBinarySpectrum}
     * @param binarySpectrum BinarySpectrum
     * @return A filtered BinarySpectrum
     */
    @Override
    IBinarySpectrum apply(IBinarySpectrum binarySpectrum);

    /**
     * Compose function takes a function to be applied before applying the following rule.
     * @param before Before function.
     * @param <V> Returning Type
     * @return function result
     */
    @Override
    default <V> Function<V, IBinarySpectrum> compose(Function<? super V, ? extends IBinarySpectrum> before) {
        Objects.requireNonNull(before);
        return (V v) -> apply(before.apply(v));
    }


    @Override
    default <V> Function<IBinarySpectrum, V> andThen(Function<? super IBinarySpectrum, ? extends V> after) {
        Objects.requireNonNull(after);
        return (IBinarySpectrum t) -> after.apply(apply(t));
    }
}
