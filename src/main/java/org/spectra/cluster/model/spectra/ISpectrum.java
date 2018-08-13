package org.spectra.cluster.model.spectra;


public interface ISpectrum  {

    /** Get precursor charge **/
    int getPrecursorCharge();

    /** A unique auto generated id **/
    long getUUI();

    /** Get number of Peaks**/
    int getNumberOfPeaks();

}
