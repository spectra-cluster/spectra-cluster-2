package org.spectra.cluster.model.cluster;

import java.io.Serializable;

/**
 * This class only stores a certain similarity and the
 * spectrum id it was associated with. It is intended to
 * store similarity matches within a cluster.
 *
 * Created by jg on 06.05.15.
 */

public class ComparisonMatch implements Comparable<ComparisonMatch>, Serializable {

    private String spectrumId;

    /** Single precision is sufficient for this*/
    private float similarity;

    public ComparisonMatch() { }

    public ComparisonMatch(String spectrumId, float similarity) {
        this.spectrumId = spectrumId;
        this.similarity = similarity;
    }

    public String getSpectrumId() {
        return spectrumId;
    }

    public void setSpectrumId(String spectrumId) {
        this.spectrumId = spectrumId;
    }

    public float getSimilarity() {
        return similarity;
    }

    public void setSimilarity(float similarity) {
        this.similarity = similarity;
    }

    @Override
    public int compareTo(ComparisonMatch o) {
        return Float.compare(this.similarity, o.similarity);
    }

}
