package org.spectra.cluster.exceptions;

/**
 *
 * Missing Parameter Exception.
 *
 * @author ypriverol
 */
public class MissingParameterException extends Exception {
    public MissingParameterException() {
        super();
    }

    public MissingParameterException(String message) {
        super(message);
    }
}
