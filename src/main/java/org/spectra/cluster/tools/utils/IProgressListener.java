package org.spectra.cluster.tools.utils;

/**
 * Defines an interface for classes listening to progress
 * updates.
 *
 * Created by jg on 10.06.16.
 */
public interface IProgressListener {
    void onProgressUpdate(ProgressUpdate progressUpdate);
}
