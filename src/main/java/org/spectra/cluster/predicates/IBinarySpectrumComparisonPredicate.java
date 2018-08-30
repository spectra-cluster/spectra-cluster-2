package org.spectra.cluster.predicates;

import org.spectra.cluster.model.spectra.IBinarySpectrum;

import java.io.Serializable;

public interface IBinarySpectrumComparisonPredicate extends Serializable, Cloneable {
    /**
     * Compare two spectra.
     * @param s1 The first spectrum
     * @param s2 The second spectrum
     * @return boolean to indicate whether the predicate is fullfilled
     */
    boolean test(IBinarySpectrum s1, IBinarySpectrum s2);

    default IBinarySpectrumComparisonPredicate negate() {
        return (s1, s2) -> !this.test(s1, s2);
    }

    default IBinarySpectrumComparisonPredicate and(IBinarySpectrumComparisonPredicate other) {
        return (s1, s2) -> {
            if (!this.test(s1, s2)) {
                return false;
            }
            if (!other.test(s1, s2)) {
                return false;
            }

            return true;
        };
    }

    default IBinarySpectrumComparisonPredicate or(IBinarySpectrumComparisonPredicate other) {
        return (s1, s2) -> {
            if (this.test(s1, s2)) {
                return true;
            }
            if (other.test(s1, s2)) {
                return true;
            }

            return false;
        };
    }
}
