package org.spectra.cluster.model.spectra;

import lombok.Data;

import java.io.Serializable;

/**
 * This code is licensed under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 *
 *  Peaks can be define as a combination of an intensity value and mz value.
 *
 * @author ypriverol on 16/08/2018.
 */

@Data
public class BinaryPeak implements Serializable {
    protected final int mz;
    protected final int intensity;
}
