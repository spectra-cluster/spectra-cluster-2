package org.spectra.cluster.similarity;

import cern.jet.random.HyperGeometric;
import cern.jet.random.engine.RandomEngine;
import org.junit.Assert;
import org.junit.Test;
import org.spectra.cluster.filter.HighestIntensityNPeaksFilter;
import org.spectra.cluster.filter.IFilter;
import org.spectra.cluster.io.MzSpectraReader;
import org.spectra.cluster.model.spectra.BinaryPeak;
import org.spectra.cluster.model.spectra.BinarySpectrum;
import org.spectra.cluster.model.spectra.IBinarySpectrum;

import java.io.File;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class CombinedFisherIntensityTestTest {
    @Test
    public void testScoreGeneration() throws Exception {
        // read the original scores
        URI uri = BinarySpectrum.class.getClassLoader().getResource("same_sequence_cluster_scores.tsv").toURI();
        Stream <String> scoreLineStream = Files.lines(Paths.get(uri));
        List<Double> scores = scoreLineStream.map(Double::new).collect(Collectors.toList());

        // get the spectra
        File peakList = new File(CombinedFisherIntensityTestTest.class.getClassLoader().getResource("same_sequence_cluster.mgf").toURI());
        MzSpectraReader reader = new MzSpectraReader(peakList);
        Iterator<IBinarySpectrum> spectrumIterator = reader.readBinarySpectraIterator();
        List<IBinarySpectrum> allSpectra = new ArrayList<>();

        while (spectrumIterator.hasNext()) {
            IBinarySpectrum s = spectrumIterator.next();

            // sort the peaks
            Arrays.parallelSort(s.getPeaks(), Comparator.comparingInt(BinaryPeak::getMz));

            allSpectra.add(s);
        }

        IBinarySpectrumSimilarity similarity = new CombinedFisherIntensityTest();

        IBinarySpectrum s1 = allSpectra.get(0);
        IFilter peakFilter = new HighestIntensityNPeaksFilter(20);
        s1 = peakFilter.filter(s1);

        // do the comparison
        for (int i = 1; i < allSpectra.size(); i++) {
            IBinarySpectrum s2 = peakFilter.filter(allSpectra.get(i));

            double score = similarity.correlation(s1, s2);
            double orgScore = scores.get(i - 1);

            if (orgScore == Double.POSITIVE_INFINITY) {
                continue;
            }

            Assert.assertFalse(String.format("Comparison %d failed", i), Double.isNaN(score));
            // score differences are caused by
            // 1) binning and the thereby caused different number of peaks and different fragment tolerance
            // 2) different intensity normalisation in the original spectra-cluster code
            Assert.assertEquals(score, orgScore, 30);

        }
    }

    @Test
    public void testHgt() {
        // Fails: minBin = 134, maxBin = 1789, peaks1 = 87, peaks2 = 383, shared = 62
        double score = new HyperGeometric(1655, 383, 87, RandomEngine.makeDefault()).pdf(62);

        Assert.assertFalse(Double.isNaN(score));
        System.out.println(String.valueOf(score));
    }
}
