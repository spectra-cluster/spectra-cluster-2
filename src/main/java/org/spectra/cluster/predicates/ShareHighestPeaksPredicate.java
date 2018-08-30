package org.spectra.cluster.predicates;

import lombok.Data;
import org.spectra.cluster.model.spectra.BinaryPeak;
import org.spectra.cluster.model.spectra.IBinarySpectrum;

import java.util.Arrays;
import java.util.Comparator;
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
        // get the nHighestPeaks from both spectra
        // we need a copy since we will be changing the sort order
        BinaryPeak[] peaks1 = Arrays.copyOf(s1.getPeaks(), s1.getPeaks().length);
        BinaryPeak[] peaks2 = Arrays.copyOf(s2.getPeaks(), s2.getPeaks().length);

        // sort according to intensity
        Arrays.sort(peaks1, Comparator.comparingInt(BinaryPeak::getIntensity).reversed());
        Arrays.sort(peaks2, Comparator.comparingInt(BinaryPeak::getIntensity).reversed());

        // extract the highest N peaks from spectrum 1
        Set<Integer> mz1 = Arrays.stream(peaks1).limit(nHighestPeaks).mapToInt(BinaryPeak::getMz).boxed().collect(Collectors.toSet());

        // check if any are shared
        return Arrays.stream(peaks2).limit(nHighestPeaks).mapToInt(BinaryPeak::getMz).anyMatch(mz1::contains);
    }
}
