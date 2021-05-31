package org.spectra.cluster.predicates;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.spectra.cluster.engine.GreedyClusteringEngine;
import org.spectra.cluster.io.spectra.MzSpectraReader;
import org.spectra.cluster.model.spectra.BinaryPeak;
import org.spectra.cluster.model.spectra.BinarySpectrum;
import org.spectra.cluster.model.spectra.IBinarySpectrum;
import org.spectra.cluster.util.ClusteringParameters;

import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;

public class ShareHighestPeaksPredicateTest {
    private File testFile;
    private List<IBinarySpectrum> spectra;

    @Before
    public void setUp() throws Exception {
        testFile = new File(Objects.requireNonNull(ShareHighestPeaksPredicate.class.getClassLoader().getResource("same_sequence_cluster.mgf").toURI()));
        MzSpectraReader reader = new MzSpectraReader(new ClusteringParameters(), testFile);
        spectra = new ArrayList<>(50);
        Iterator<IBinarySpectrum> iterator = reader.readBinarySpectraIterator();

        while (iterator.hasNext()) {
            spectra.add(iterator.next());
        }
    }

    @Test
    public void testShareHighestPeaks() {
        IComparisonPredicate<IBinarySpectrum> comparisonPredicate = new ShareHighestPeaksPredicate(5);
        BinaryPeak[] wrongPeaks = {
                new BinaryPeak(1, 1),
                new BinaryPeak(2, 3),
                new BinaryPeak(3, 3)
        };
        BinarySpectrum wrongSpec = new BinarySpectrum(12, 2, wrongPeaks, GreedyClusteringEngine.COMPARISON_FILTER);

        Assert.assertTrue(comparisonPredicate.test(spectra.get(0), spectra.get(1)));

        Assert.assertFalse(comparisonPredicate.test(spectra.get(0), wrongSpec));

        IComparisonPredicate<IBinarySpectrum> falsePredicate = comparisonPredicate.and((s1, s2) -> false);
        Assert.assertFalse(falsePredicate.test(spectra.get(0), spectra.get(1)));

        IComparisonPredicate<IBinarySpectrum> truePredicate = comparisonPredicate.or((s1, s2) -> false);
        Assert.assertTrue(truePredicate.test(spectra.get(0), spectra.get(1)));

        IComparisonPredicate<IBinarySpectrum> anotherFalsePredicate = comparisonPredicate.negate();
        Assert.assertFalse(anotherFalsePredicate.test(spectra.get(0), spectra.get(1)));
    }
}
