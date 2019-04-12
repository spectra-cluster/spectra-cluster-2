package org.spectra.cluster.model.cluster;

import lombok.Builder;
import lombok.Data;

import java.io.Serializable;

/**
 * This class only stores a certain similarity and the
 * spectrum id it was associated with. It is intended to
 * store similarity matches within a cluster.
 *
 * Created by jg on 06.05.15.
 */
@Builder
@Data
public class ComparisonMatch implements Comparable<ComparisonMatch>, Serializable {
    private final String spectrumId;
    /**
     * Single precision is sufficient for this
     */
    private final float similarity;

    @Override
    public int compareTo(ComparisonMatch o) {
        return Float.compare(this.similarity, o.similarity);
    }
}
