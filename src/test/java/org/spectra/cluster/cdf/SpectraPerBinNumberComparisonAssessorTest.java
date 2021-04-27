package org.spectra.cluster.cdf;

import org.junit.Assert;
import org.junit.Test;
import org.spectra.cluster.engine.GreedyClusteringEngine;
import org.spectra.cluster.engine.IClusteringEngine;
import org.spectra.cluster.filter.rawpeaks.*;
import org.spectra.cluster.io.MzSpectraReaderTest;
import org.spectra.cluster.io.spectra.MzSpectraReader;
import org.spectra.cluster.model.consensus.GreedyClusteringConsensusSpectrum;
import org.spectra.cluster.model.spectra.IBinarySpectrum;
import org.spectra.cluster.normalizer.BasicIntegerNormalizer;
import org.spectra.cluster.predicates.ShareHighestPeaksClusterPredicate;
import org.spectra.cluster.similarity.CombinedFisherIntensityTest;
import org.spectra.cluster.util.ClusteringParameters;

import java.io.File;
import java.util.Iterator;

public class SpectraPerBinNumberComparisonAssessorTest {
    @Test
    public void testNumberAssessor() {
        SpectraPerBinNumberComparisonAssessor assessor = new SpectraPerBinNumberComparisonAssessor(10, 50, 2000);

        int[] precursorsObserved = {10, 11, 21};

        for (int precursor : precursorsObserved) {
            for (int i = 0; i < 100; i++) {
                assessor.countSpectrum(precursor);
            }
        }

        Assert.assertEquals(50, assessor.getNumberOfComparisons(9, 1));
        Assert.assertEquals(200, assessor.getNumberOfComparisons(10, 1));
        Assert.assertEquals(200, assessor.getNumberOfComparisons(11, 1));
        Assert.assertEquals(100, assessor.getNumberOfComparisons(20, 1));
        Assert.assertEquals(50, assessor.getNumberOfComparisons(30, 1));
        Assert.assertEquals(50, assessor.getNumberOfComparisons(40, 1));
    }

    @Test
    public void testLoadingFiles() throws Exception {
        File testFile = new File(MzSpectraReaderTest.class.getClassLoader().getResource("same_sequence_cluster.mgf").toURI());

        BasicIntegerNormalizer normalizer = new BasicIntegerNormalizer();

        IRawSpectrumFunction loadingFilter = new RemoveImpossiblyHighPeaksFunction()
                .specAndThen(new RemovePrecursorPeaksFunction(0.5))
                .specAndThen(new RawPeaksWrapperFunction(new KeepNHighestRawPeaks(40)));

        IClusteringEngine engine = new GreedyClusteringEngine(BasicIntegerNormalizer.MZ_CONSTANT,
                1, 0.99f, 5, new CombinedFisherIntensityTest(),
                new MinNumberComparisonsAssessor(10000), new ShareHighestPeaksClusterPredicate(5),
                GreedyClusteringConsensusSpectrum.NOISE_FILTER_INCREMENT);

        MzSpectraReader reader = new MzSpectraReader(new ClusteringParameters(), testFile);

        SpectraPerBinNumberComparisonAssessor assessor = new SpectraPerBinNumberComparisonAssessor(
                (int) Math.round(BasicIntegerNormalizer.MZ_CONSTANT * 0.5), 0, 2500 * BasicIntegerNormalizer.MZ_CONSTANT);

        reader.addSpectrumListener(assessor);

        Iterator<IBinarySpectrum> it = reader.readBinarySpectraIterator();

        while (it.hasNext()) {
            Assert.assertNotNull(it.next());
        }

        // make sure all spectra were counted
        Assert.assertEquals(54, assessor.getNumberOfComparisons(normalizer.binValue(977.0), 0));
        Assert.assertEquals(38, assessor.getNumberOfComparisons(normalizer.binValue(652.0), 0));
        Assert.assertEquals(1, assessor.getNumberOfComparisons(normalizer.binValue(651.0), 0));
        Assert.assertEquals(66, assessor.getNumberOfComparisons(normalizer.binValue(651.69), 0));
    }
}
