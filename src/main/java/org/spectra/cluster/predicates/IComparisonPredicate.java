package org.spectra.cluster.predicates;

public interface IComparisonPredicate<T> {
    /**
     * Compare two Objects.
     * @param o1 The first object
     * @param o2 The second object
     * @return boolean to indicate whether the predicate is fullfilled
     */
    boolean test(T o1, T o2);

    default IComparisonPredicate<T> negate() {
        return (o1, o2) -> !this.test(o1, o2);
    }

    default IComparisonPredicate<T> and(IComparisonPredicate<T> other) {
        return (o1, o2) -> {
            if (!this.test(o1, o2)) {
                return false;
            }
            return other.test(o1, o2);
        };
    }

    default IComparisonPredicate<T> or(IComparisonPredicate<T> other) {
        return (o1, o2) -> {
            if (this.test(o1, o2)) {
                return true;
            }
            return other.test(o1, o2);

        };
    }
}
