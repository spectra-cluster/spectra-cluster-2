package org.spectra.cluster.model.consensus;

import lombok.extern.slf4j.Slf4j;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.spectra.cluster.engine.GreedyClusteringEngine;
import org.spectra.cluster.filter.rawpeaks.*;
import org.spectra.cluster.io.spectra.MzSpectraReader;
import org.spectra.cluster.model.cluster.GreedySpectralClusterTest;
import org.spectra.cluster.model.spectra.BinaryPeak;
import org.spectra.cluster.model.spectra.BinarySpectrum;
import org.spectra.cluster.model.spectra.IBinarySpectrum;
import org.spectra.cluster.normalizer.BasicIntegerNormalizer;
import org.spectra.cluster.normalizer.CumulativeIntensityNormalizer;
import org.spectra.cluster.similarity.CombinedFisherIntensityTest;
import org.spectra.cluster.similarity.IBinarySpectrumSimilarity;
import org.spectra.cluster.util.ClusteringParameters;

import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;

@Slf4j
public class GreedyConsensusSpectrumTest {
    IRawSpectrumFunction loadingFilter;

    @Before
    public void setUp() {
        loadingFilter = new RemoveImpossiblyHighPeaksFunction()
                .specAndThen(new RemovePrecursorPeaksFunction(0.5))
                .specAndThen(new RawPeaksWrapperFunction(new KeepNHighestRawPeaks(70)));
    }

    @Test
    public void testAddPeaksToConsensus() {
        BinaryConsensusPeak[] existingPeaks = {
                new BinaryConsensusPeak(10, 100, 10),
                new BinaryConsensusPeak(20, 200, 5),
                new BinaryConsensusPeak(100, 1000, 30)
        };

        BinaryPeak[] peaksToAdd = {
                new BinaryPeak(5, 10),
                new BinaryPeak(20, 10),
                new BinaryPeak(110, 20)
        };

        BinaryConsensusPeak[] mergedPeaks = GreedyClusteringConsensusSpectrum.addPeaksToConsensus(existingPeaks, peaksToAdd);

        Assert.assertEquals(5, mergedPeaks.length);
        Assert.assertEquals(5, mergedPeaks[0].getMz());
        Assert.assertEquals(1, mergedPeaks[0].getCount());

        Assert.assertEquals(20, mergedPeaks[2].getMz());
        Assert.assertEquals(168, mergedPeaks[2].getIntensity());
        Assert.assertEquals(6, mergedPeaks[2].getCount());
        Assert.assertEquals(110, mergedPeaks[4].getMz());
    }

    @Test
    public void testAddConsensusPeaksToConsensus() {
        BinaryConsensusPeak[] existingPeaks = {
                new BinaryConsensusPeak(10, 100, 10),
                new BinaryConsensusPeak(20, 200, 5),
                new BinaryConsensusPeak(100, 1000, 30)
        };

        BinaryConsensusPeak[] peaksToAdd = {
                new BinaryConsensusPeak(5, 10, 5),
                new BinaryConsensusPeak(20, 10, 10),
                new BinaryConsensusPeak(110, 20, 10)
        };

        BinaryConsensusPeak[] mergedPeaks = GreedyClusteringConsensusSpectrum.addPeaksToConsensus(existingPeaks, peaksToAdd);

        Assert.assertEquals(5, mergedPeaks.length);
        Assert.assertEquals(5, mergedPeaks[0].getMz());
        Assert.assertEquals(5, mergedPeaks[0].getCount());

        Assert.assertEquals(20, mergedPeaks[2].getMz());
        Assert.assertEquals(73, mergedPeaks[2].getIntensity());
        Assert.assertEquals(15, mergedPeaks[2].getCount());
        Assert.assertEquals(110, mergedPeaks[4].getMz());
        Assert.assertEquals(10, mergedPeaks[4].getCount());
    }

    /**
     * Tests whether the consensus spectrum of the first 10 test spectra (all from the same peptide) are similar to the consensus.
     *
     * @throws Exception
     */
    @Test
    public void testGeneratesSimilarSpectrum() throws Exception {
        File testFile = new File(Objects.requireNonNull(GreedySpectralClusterTest.class.getClassLoader().getResource("same_sequence_cluster.mgf")).toURI());

        ClusteringParameters clusteringParameters = new ClusteringParameters();

        MzSpectraReader reader = new MzSpectraReader(new ClusteringParameters(), (IBinarySpectrum s) -> s, testFile);

        Iterator<IBinarySpectrum> spectrumIterator = reader.readBinarySpectraIterator();
        List<IBinarySpectrum> spectra = new ArrayList<>();

        while (spectrumIterator.hasNext()) {
            if (spectra.size() > 10) {
                break;
            }

            spectra.add(spectrumIterator.next());
        }

        long currentTime = System.currentTimeMillis();

        GreedyClusteringConsensusSpectrum consensusSpectrum = new GreedyClusteringConsensusSpectrum("Test", (IBinarySpectrum s) -> s);
        consensusSpectrum.addSpectra(spectra.toArray(new IBinarySpectrum[0]));

        currentTime = System.currentTimeMillis();
        IBinarySpectrumSimilarity similarity = new CombinedFisherIntensityTest();

        IBinarySpectrum consensus = consensusSpectrum.getConsensusSpectrum();
        long timeDifference = (System.currentTimeMillis() - currentTime);
        System.out.println("Time Consensus Spectra Generation: " + timeDifference + " Spectrum Peaks Size: " + consensus.getPeaks().length);
        int counter = 0;

        for (IBinarySpectrum s : spectra) {
            double score = similarity.correlation(consensus, s);
            counter++;

            Assert.assertTrue(String.format("Bad correlation score %d: %.2f", counter, score), score > 100);
            log.info("The similarity between the Spectrum: " + s.getUUI() + "and the Consensus is: " + score);
        }
    }


    /**
     * Tests whether the consensus spectrum of the first 10 test spectra (all from the same peptide) are similar to the consensus.
     *
     * @throws Exception
     */
    @Test
    public void tesBenchaMarkPeakIntesityNormalization() throws Exception {
        File testFile = new File(Objects.requireNonNull(GreedySpectralClusterTest.class.getClassLoader().getResource("same_sequence_cluster.mgf")).toURI());
        MzSpectraReader readerCummulative = new MzSpectraReader(new ClusteringParameters(), (IBinarySpectrum s) -> s, testFile);

        Iterator<IBinarySpectrum> spectrumIterator = readerCummulative.readBinarySpectraIterator();
        List<IBinarySpectrum> spectraCummulative = new ArrayList<>();
        while (spectrumIterator.hasNext()) {
            if (spectraCummulative.size() > 10) {
                break;
            }
            spectraCummulative.add(spectrumIterator.next());
        }

        GreedyClusteringConsensusSpectrum consesusCumulative = new GreedyClusteringConsensusSpectrum("Test", (IBinarySpectrum s) -> s);
        consesusCumulative.addSpectra(spectraCummulative.toArray(new IBinarySpectrum[0]));
        IBinarySpectrum consensusCumulative = consesusCumulative.getConsensusSpectrum();


        MzSpectraReader reader = new MzSpectraReader(new ClusteringParameters(), testFile);
        spectrumIterator = reader.readBinarySpectraIterator();
        List<IBinarySpectrum> spectra = new ArrayList<>();
        while (spectrumIterator.hasNext()) {
            if (spectra.size() > 10) {
                break;
            }
            spectra.add(spectrumIterator.next());
        }

        GreedyClusteringConsensusSpectrum consensusSpectrum = new GreedyClusteringConsensusSpectrum("Test", (IBinarySpectrum s) -> s);
        consensusSpectrum.addSpectra(spectra.toArray(new IBinarySpectrum[0]));
        IBinarySpectrum consensus = consensusSpectrum.getConsensusSpectrum();

        IBinarySpectrumSimilarity similarity = new CombinedFisherIntensityTest();

        for (int i = 0; i < 10; i++) {
            double scoreCumulative = similarity.correlation(consensusCumulative, spectraCummulative.get(i));
            double score = similarity.correlation(consensus, spectra.get(i));
            Assert.assertTrue(String.format("Score %d is too low: %.2f", i, score), score > 85);
            Assert.assertTrue(scoreCumulative > 85);
            log.info("Spectrum: " + spectra.get(i).getUUI() + " CumulativeIntesity Score -  MaxIntensityScore: " + (scoreCumulative - score));

        }
    }


    /**
     * This test compare the {@link CumulativeIntensityNormalizer} and the {@link org.spectra.cluster.normalizer.MaxPeakNormalizer} to check who is producing better
     * results during the clustering process. The synthetic_first_pool_3xHCD_R1 file is used to retrieve the first 10 spectra associated with the PeptideSequence
     * AAAFYVR .
     * <p>
     * Todo: The current score for the Consensus Cluster compare with each Spectra is always lower than 80. Is that ok. ?
     *
     * @throws Exception
     */
    @Test
    public void tesBenchaMarkPeakIntesityNormalizationSytentic() throws Exception {
        File testFile = new File(Objects.requireNonNull(GreedySpectralClusterTest.class.getClassLoader().getResource("synthetic_first_pool_3xHCD_R1.mgf")).toURI());
        MzSpectraReader readerCumulative = new MzSpectraReader(new ClusteringParameters(), testFile);

        Iterator<IBinarySpectrum> spectrumIterator = readerCumulative.readBinarySpectraIterator();
        List<IBinarySpectrum> spectraCummulative = new ArrayList<>();
        while (spectrumIterator.hasNext()) {
            if (spectraCummulative.size() > 10) {
                break;
            }
            spectraCummulative.add(spectrumIterator.next());
        }

        GreedyClusteringConsensusSpectrum consesusCumulative = new GreedyClusteringConsensusSpectrum("Test", GreedyClusteringEngine.COMPARISON_FILTER);
        consesusCumulative.addSpectra(spectraCummulative.toArray(new IBinarySpectrum[0]));
        IBinarySpectrum consensusCumulative = consesusCumulative.getConsensusSpectrum();


        MzSpectraReader reader = new MzSpectraReader(new ClusteringParameters(), testFile);
        spectrumIterator = reader.readBinarySpectraIterator();
        List<IBinarySpectrum> spectra = new ArrayList<>();
        while (spectrumIterator.hasNext()) {
            if (spectra.size() > 10) {
                break;
            }
            spectra.add(spectrumIterator.next());
        }

        GreedyClusteringConsensusSpectrum consensusSpectrum = new GreedyClusteringConsensusSpectrum("Test", GreedyClusteringEngine.COMPARISON_FILTER);
        consensusSpectrum.addSpectra(spectra.toArray(new IBinarySpectrum[0]));
        IBinarySpectrum consensus = consensusSpectrum.getConsensusSpectrum();

        IBinarySpectrumSimilarity similarity = new CombinedFisherIntensityTest();

        for (int i = 0; i < 10; i++) {
            double scoreCumulative = similarity.correlation(consensusCumulative, spectraCummulative.get(i));
            double score = similarity.correlation(consensus, spectra.get(i));
            log.info("Spectrum: " + spectra.get(i).getUUI() + " score: " + score + " cumulative score: " + scoreCumulative + " CumulativeIntensity Score -  MaxIntensityScore: " + (scoreCumulative - score));

        }
    }

    @Test
    public void testLargeCluster() {
        // always put the same spectrum to test for overflows
        BinaryConsensusPeak[] existingPeaks = {
                new BinaryConsensusPeak(10, 100, 10),
                new BinaryConsensusPeak(20, 200, 5),
                new BinaryConsensusPeak(100, 1000, 30),
                new BinaryConsensusPeak(110, 1000, 30),
                new BinaryConsensusPeak(120, 10, 30),
                new BinaryConsensusPeak(130, 100, 30),
                new BinaryConsensusPeak(140, 100, 30),
                new BinaryConsensusPeak(150, 1000, 30),
                new BinaryConsensusPeak(160, 1000, 30),
                new BinaryConsensusPeak(170, 1000, 30),
                new BinaryConsensusPeak(180, 200, 30),
                new BinaryConsensusPeak(1000, 200, 30)
        };

        GreedyClusteringConsensusSpectrum consensusSpectrum = new GreedyClusteringConsensusSpectrum("0", 50, 5, 100, GreedyClusteringEngine.COMPARISON_FILTER);
        int precursorMz = new BasicIntegerNormalizer().binValue(1024.1993);

        for (int i = 0; i < 100_000; i++) {
            IBinarySpectrum binarySpectrum = new BinarySpectrum(String.valueOf(i + 1),
                    precursorMz,
                    2,
                    existingPeaks, GreedyClusteringEngine.COMPARISON_FILTER);

            consensusSpectrum.addSpectra(binarySpectrum);

            Assert.assertEquals(precursorMz, consensusSpectrum.getPrecursorMz());
            Assert.assertEquals(i + 1, consensusSpectrum.getSpectraCount());
        }

        GreedyClusteringConsensusSpectrum consensusSpectrum2 = new GreedyClusteringConsensusSpectrum("c1", 50, 5, 100, GreedyClusteringEngine.COMPARISON_FILTER);

        for (int i = 0; i < 15; i++) {
            IBinarySpectrum binarySpectrum = new BinarySpectrum(String.valueOf(i + 100_010),
                    precursorMz,
                    2,
                    existingPeaks,
                    GreedyClusteringEngine.COMPARISON_FILTER);

            consensusSpectrum2.addSpectra(binarySpectrum);
        }

        Assert.assertEquals(precursorMz, consensusSpectrum2.getPrecursorMz());
        consensusSpectrum.addConsensusSpectrum(consensusSpectrum2);
        Assert.assertEquals(precursorMz, consensusSpectrum.getPrecursorMz());
    }

    @Test
    public void testAverageChargeApproach() {
        int sumCharge = 10;
        int nSpectra = 5;

        Assert.assertEquals(2, Math.round(sumCharge / (float) nSpectra));

        sumCharge += 1;
        nSpectra++;

        Assert.assertEquals(2, Math.round(sumCharge / (float) nSpectra));

        sumCharge = 5;
        nSpectra = 5;

        Assert.assertEquals(1, Math.round(sumCharge / (float) nSpectra));

        nSpectra++;
        sumCharge += 2;

        Assert.assertEquals(1, Math.round(sumCharge / (float) nSpectra));
    }
}
