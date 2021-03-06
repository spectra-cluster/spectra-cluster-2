package org.spectra.cluster.exceptions;

/**
 * This code is licensed under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * ==Overview==
 *
 * @author ypriverol on 18/10/2018.
 */
public class SpectraClusterException extends Exception {

    public SpectraClusterException() {
        super();
    }

    public SpectraClusterException(String message, Throwable cause) {
        super(message, cause);
    }

    public SpectraClusterException(String message) {
        super(message);
    }
}
