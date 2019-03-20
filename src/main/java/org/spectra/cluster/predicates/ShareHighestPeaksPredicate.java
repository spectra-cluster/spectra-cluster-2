package org.spectra.cluster.predicates;

import lombok.Data;
import org.spectra.cluster.model.spectra.BinaryPeak;
import org.spectra.cluster.model.spectra.IBinarySpectrum;

import java.util.HashSet;
import java.util.Set;

/**
 * This predicate tests whether the two spectra share any of the N highest peaks.
 */
@Data
public class ShareHighestPeaksPredicate implements IComparisonPredicate<IBinarySpectrum> {
    private final int nHighestPeaks;

    @Override
    public boolean test(IBinarySpectrum s1, IBinarySpectrum s2) {
        // store the highest peaks in a set
        Set<Integer> mz1 = new HashSet<>(nHighestPeaks + 4, 0.95f);
        for (BinaryPeak p : s1.getPeaks()) {
            if (p.getRank() <= nHighestPeaks) {
                mz1.add(p.getMz());
            }
            if (mz1.size() >= nHighestPeaks) {
                break;
            }
        }

        // test if there is any match
        for (BinaryPeak p : s2.getPeaks()) {
            if (p.getRank() > nHighestPeaks) {
                continue;
            }
            if (mz1.contains(p.getMz())) {
                return true;
            }
        }

        return false;
    }
}
