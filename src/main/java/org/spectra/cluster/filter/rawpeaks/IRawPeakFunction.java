package org.spectra.cluster.filter.rawpeaks;

import org.spectra.cluster.model.spectra.IBinarySpectrum;

import java.io.Serializable;
import java.util.Map;
import java.util.function.Function;

/**
 * This code is licensed under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 *
 * This interface takes the data of a Peak in a apply some filters to it.
 *
 * @author ypriverol on 21/08/2018.
 */
public interface IRawPeakFunction extends Serializable, Cloneable, Function<Map<Double, Double>, Map<Double, Double>> {

    @Override
    Map<Double, Double> apply(Map<Double, Double> peaks);
}
