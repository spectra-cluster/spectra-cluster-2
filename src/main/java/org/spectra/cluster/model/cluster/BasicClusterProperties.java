package org.spectra.cluster.model.cluster;

import lombok.Data;

@Data
public class BasicClusterProperties implements IClusterProperties {
    private final int precursorMz;
    private final Integer precursorCharge;
    private final String id;
}
