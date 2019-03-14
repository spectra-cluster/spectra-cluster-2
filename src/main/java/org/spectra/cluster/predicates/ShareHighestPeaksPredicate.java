package org.spectra.cluster.predicates;

import lombok.Data;
import org.spectra.cluster.model.spectra.BinaryPeak;
import org.spectra.cluster.model.spectra.IBinarySpectrum;

import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * This predicate tests whether the two spectra share any of the N highest peaks.
 */
@Data
public class ShareHighestPeaksPredicate implements IComparisonPredicate<IBinarySpectrum> {
    private final int nHighestPeaks;

    @Override
    public boolean test(IBinarySpectrum s1, IBinarySpectrum s2) {
        Set<Integer> mz1 = Arrays.stream(s1.getPeaks())
                .filter((BinaryPeak p) -> p.getRank() <= nHighestPeaks)
                .mapToInt(BinaryPeak::getMz)
                .boxed()
                .collect(Collectors.toSet());

        return Arrays.stream(s2.getPeaks())
                .filter((BinaryPeak p) -> p.getRank() <= nHighestPeaks)
                .mapToInt(BinaryPeak::getMz)
                .anyMatch(mz1::contains);
    }
}
