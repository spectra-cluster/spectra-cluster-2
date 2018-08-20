package org.spectra.cluster.filter;

import org.spectra.cluster.model.spectra.IBinarySpectrum;
import java.io.Serializable;

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
public interface IFilter extends Serializable, Cloneable {

    /**
     * Filter an specific {@link IBinarySpectrum}
     * @param binarySpectrum BinarySpectrum
     * @return A filtered BinarySpectrum
     */
    public IBinarySpectrum filter(IBinarySpectrum binarySpectrum);
}
