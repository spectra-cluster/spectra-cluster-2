package org.spectra.cluster.model.spectra;

import cern.colt.matrix.impl.SparseDoubleMatrix1D;
import lombok.Builder;
import lombok.Data;


@Data
public class BinarySpectrum implements IBinarySpectrum {

    long uui;
    int precursorIntMZ;
    int precursorCharge;
    private SparseDoubleMatrix1D mzPeaksVector;
    private SparseDoubleMatrix1D intensityPeaksVector;

    public BinarySpectrum() {

    }

    @Override
    public long getUUI() {
        return uui;
    }

    @Override
    public int getIntPrecursorMz() {
        return precursorIntMZ;
    }

    @Override
    public int getPrecursorCharge() {
        return precursorCharge;
    }

    @Override
    public SparseDoubleMatrix1D getIntMzVector() {
        return mzPeaksVector;
    }

    @Override
    public SparseDoubleMatrix1D getIntIntensityVector() {
        return intensityPeaksVector;
    }
}
