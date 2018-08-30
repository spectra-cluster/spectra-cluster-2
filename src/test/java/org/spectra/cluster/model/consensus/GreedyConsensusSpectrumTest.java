package org.spectra.cluster.model.consensus;

import lombok.extern.slf4j.Slf4j;
import org.junit.Assert;
import org.junit.Test;
import org.spectra.cluster.filter.binaryspectrum.HighestPeakPerBinFunction;
import org.spectra.cluster.io.MzSpectraReader;
import org.spectra.cluster.model.cluster.GreedySpectralClusterTest;
import org.spectra.cluster.model.spectra.BinaryPeak;
import org.spectra.cluster.model.spectra.IBinarySpectrum;
import org.spectra.cluster.normalizer.BasicIntegerNormalizer;
import org.spectra.cluster.normalizer.CumulativeIntensityNormalizer;
import org.spectra.cluster.normalizer.SequestBinner;
import org.spectra.cluster.similarity.CombinedFisherIntensityTest;
import org.spectra.cluster.similarity.IBinarySpectrumSimilarity;

import java.io.File;
import java.util.*;

@Slf4j
public class GreedyConsensusSpectrumTest {

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

        GreedyConsensusSpectrum consensusSpectrum = new GreedyConsensusSpectrum();
        BinaryConsensusPeak[] mergedPeaks = consensusSpectrum.addPeaksToConsensus(existingPeaks, peaksToAdd);

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
                new BinaryConsensusPeak(110, 20,10)
        };

        BinaryConsensusPeak[] mergedPeaks = GreedyConsensusSpectrum.addPeaksToConsensus(existingPeaks, peaksToAdd);

        Assert.assertEquals(5, mergedPeaks.length);
        Assert.assertEquals(5, mergedPeaks[0].getMz());
        Assert.assertEquals(5, mergedPeaks[0].getCount());

        Assert.assertEquals(20, mergedPeaks[2].getMz());
        Assert.assertEquals(73, mergedPeaks[2].getIntensity());
        Assert.assertEquals(15, mergedPeaks[2].getCount());
        Assert.assertEquals(110, mergedPeaks[4].getMz());
        Assert.assertEquals(10, mergedPeaks[4].getCount());
    }

    @Test
    public void testAdaptPeakIntensities() {
        BinaryConsensusPeak[] existingPeaks = {
                new BinaryConsensusPeak(10, 100, 10),
                new BinaryConsensusPeak(20, 200, 5),
                new BinaryConsensusPeak(100, 1000, 30)
        };

        BinaryConsensusPeak[] adaptedPeaks = GreedyConsensusSpectrum.adaptPeakIntensities(existingPeaks, 30);

        Assert.assertEquals(3, adaptedPeaks.length);

        for (int i = 0; i < existingPeaks.length; i++) {
            Assert.assertEquals(existingPeaks[i].getMz(), adaptedPeaks[i].getMz());
            Assert.assertEquals(existingPeaks[i].getCount(), adaptedPeaks[i].getCount());
        }

        Assert.assertEquals(116, adaptedPeaks[0].getIntensity());
            Assert.assertEquals(212, adaptedPeaks[1].getIntensity());
        Assert.assertEquals(2550, adaptedPeaks[2].getIntensity());
    }

    @Test
    public void tesFilterNoise() {
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

        GreedyConsensusSpectrum consensusSpectrum = new GreedyConsensusSpectrum("0", 0, 5, 100);
        BinaryConsensusPeak[] filtered = consensusSpectrum.filterNoise(existingPeaks);

        int[] expectedMz = {10, 20, 100, 110, 150, 160, 170, 1000};

        Assert.assertEquals(expectedMz.length, filtered.length);

        for (int i = 0; i < expectedMz.length; i++) {
            Assert.assertEquals(expectedMz[i], filtered[i].getMz());
        }
    }

    /**
     * Tests whether the consensus spectrum of the first 10 test spectra (all from the same peptide) are similar to the consensus.
     * @throws Exception
     */
    @Test
    public void testGeneratesSimilarSpectrum() throws Exception {
        File testFile = new File(Objects.requireNonNull(GreedySpectralClusterTest.class.getClassLoader().getResource("same_sequence_cluster.mgf")).toURI());
        MzSpectraReader reader = new MzSpectraReader(testFile);

        Iterator<IBinarySpectrum> spectrumIterator = reader.readBinarySpectraIterator();
        List<IBinarySpectrum> spectra = new ArrayList<>();

        while (spectrumIterator.hasNext()) {
            if (spectra.size() > 10) {
                break;
            }

            spectra.add(spectrumIterator.next());
        }

        GreedyConsensusSpectrum consensusSpectrum = new GreedyConsensusSpectrum("Test");
        consensusSpectrum.addSpectra(spectra.toArray(new IBinarySpectrum[0]));

        long currentTime = System.currentTimeMillis();
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
     * @throws Exception
     */
    @Test
    public void tesBenchaMarkPeakIntesityNormalization() throws Exception {
        File testFile = new File(Objects.requireNonNull(GreedySpectralClusterTest.class.getClassLoader().getResource("same_sequence_cluster.mgf")).toURI());
        MzSpectraReader readerCummulative = new MzSpectraReader(testFile,new SequestBinner(), new CumulativeIntensityNormalizer(), new BasicIntegerNormalizer(), new HighestPeakPerBinFunction());

        Iterator<IBinarySpectrum> spectrumIterator = readerCummulative.readBinarySpectraIterator();
        List<IBinarySpectrum> spectraCummulative = new ArrayList<>();
        while (spectrumIterator.hasNext()) {
            if (spectraCummulative.size() > 10) {
                break;
            }
            spectraCummulative.add(spectrumIterator.next());
        }

        GreedyConsensusSpectrum consesusCumulative = new GreedyConsensusSpectrum("Test");
        consesusCumulative.addSpectra(spectraCummulative.toArray(new IBinarySpectrum[0]));
        IBinarySpectrum consensusCumulative = consesusCumulative.getConsensusSpectrum();


        MzSpectraReader reader = new MzSpectraReader(testFile);
        spectrumIterator = reader.readBinarySpectraIterator();
        List<IBinarySpectrum> spectra = new ArrayList<>();
        while (spectrumIterator.hasNext()) {
            if (spectra.size() > 10) {
                break;
            }
            spectra.add(spectrumIterator.next());
        }

        GreedyConsensusSpectrum consensusSpectrum = new GreedyConsensusSpectrum("Test");
        consensusSpectrum.addSpectra(spectra.toArray(new IBinarySpectrum[0]));
        IBinarySpectrum consensus = consensusSpectrum.getConsensusSpectrum();

        IBinarySpectrumSimilarity similarity = new CombinedFisherIntensityTest();

        for(int i = 0; i < 10; i++){
            double scoreCumulative  = similarity.correlation(consensusCumulative, spectraCummulative.get(i));
            double score = similarity.correlation(consensus, spectra.get(i));
            Assert.assertTrue(score > 100);
            Assert.assertTrue(scoreCumulative > 100);
            log.info("Spectrum: " + spectra.get(i).getUUI() + " CumulativeIntesity Score -  MaxIntensityScore: " + (scoreCumulative - score));

        }
    }


    /**
     * This test compare the {@link CumulativeIntensityNormalizer} and the {@link org.spectra.cluster.normalizer.MaxPeakNormalizer} to check who is producing better
     * results during the clustering process. The synthetic_first_pool_3xHCD_R1 file is used to retrieve the first 10 spectra associated with the PeptideSequence
     * AAAFYVR .
     *
     * Todo: The current score for the Consensus Cluster compare with each Spectra is always lower than 80. Is that ok. ?
     *
     * @throws Exception
     */
    @Test
    public void tesBenchaMarkPeakIntesityNormalizationSytentic() throws Exception {
        File testFile = new File(Objects.requireNonNull(GreedySpectralClusterTest.class.getClassLoader().getResource("synthetic_first_pool_3xHCD_R1.mgf")).toURI());
        MzSpectraReader readerCumulative = new MzSpectraReader(testFile,new SequestBinner(), new CumulativeIntensityNormalizer(), new BasicIntegerNormalizer(), new HighestPeakPerBinFunction());

        Iterator<IBinarySpectrum> spectrumIterator = readerCumulative.readBinarySpectraIterator();
        List<IBinarySpectrum> spectraCummulative = new ArrayList<>();
        while (spectrumIterator.hasNext()) {
            if (spectraCummulative.size() > 10) {
                break;
            }
            spectraCummulative.add(spectrumIterator.next());
        }

        GreedyConsensusSpectrum consesusCumulative = new GreedyConsensusSpectrum("Test");
        consesusCumulative.addSpectra(spectraCummulative.toArray(new IBinarySpectrum[0]));
        IBinarySpectrum consensusCumulative = consesusCumulative.getConsensusSpectrum();


        MzSpectraReader reader = new MzSpectraReader(testFile);
        spectrumIterator = reader.readBinarySpectraIterator();
        List<IBinarySpectrum> spectra = new ArrayList<>();
        while (spectrumIterator.hasNext()) {
            if (spectra.size() > 10) {
                break;
            }
            spectra.add(spectrumIterator.next());
        }

        GreedyConsensusSpectrum consensusSpectrum = new GreedyConsensusSpectrum("Test");
        consensusSpectrum.addSpectra(spectra.toArray(new IBinarySpectrum[0]));
        IBinarySpectrum consensus = consensusSpectrum.getConsensusSpectrum();

        IBinarySpectrumSimilarity similarity = new CombinedFisherIntensityTest();

        for(int i = 0; i < 10; i++){
            double scoreCumulative  = similarity.correlation(consensusCumulative, spectraCummulative.get(i));
            double score = similarity.correlation(consensus, spectra.get(i));
            log.info("Spectrum: " + spectra.get(i).getUUI() + " score: " + score + " cumulative score: " + scoreCumulative + " CumulativeIntensity Score -  MaxIntensityScore: " + (scoreCumulative - score));

        }
    }
}
