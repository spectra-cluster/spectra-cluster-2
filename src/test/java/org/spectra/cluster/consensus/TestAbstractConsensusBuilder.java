package org.spectra.cluster.consensus;

import io.github.bigbio.pgatk.io.properties.IPropertyStorage;
import io.github.bigbio.pgatk.io.properties.InMemoryPropertyStorage;
import org.junit.Assert;
import org.junit.Test;
import org.spectra.cluster.cdf.MinNumberComparisonsAssessor;
import org.spectra.cluster.engine.GreedyClusteringEngine;
import org.spectra.cluster.engine.IClusteringEngine;
import org.spectra.cluster.io.spectra.MzSpectraReader;
import org.spectra.cluster.model.cluster.ICluster;
import org.spectra.cluster.model.consensus.GreedyConsensusSpectrum;
import org.spectra.cluster.normalizer.BasicIntegerNormalizer;
import org.spectra.cluster.normalizer.MaxPeakNormalizer;
import org.spectra.cluster.predicates.ShareHighestPeaksClusterPredicate;
import org.spectra.cluster.similarity.CombinedFisherIntensityTest;

import java.io.File;
import java.net.URI;
import java.util.*;
import java.util.stream.Collectors;

public class TestAbstractConsensusBuilder {
    private List<ICluster> loadClusters(IPropertyStorage propertyStorage) throws Exception {
        // open the file
        URI uri = Objects.requireNonNull(getClass().getClassLoader().getResource("single-spectra.mgf")).toURI();
        File mgfFile = new File(uri);

        IClusteringEngine engine = new GreedyClusteringEngine(BasicIntegerNormalizer.MZ_CONSTANT,
                1, 0.99f, 5, new CombinedFisherIntensityTest(),
                new MinNumberComparisonsAssessor(10000), new ShareHighestPeaksClusterPredicate(5),
                GreedyConsensusSpectrum.NOISE_FILTER_INCREMENT);

        MzSpectraReader spectraReader = new MzSpectraReader(mgfFile, GreedyClusteringEngine.COMPARISON_FILTER, engine);


        // read the spectra
        Iterator<ICluster> clusterIterator = spectraReader.readClusterIterator(propertyStorage);
        List<ICluster> clusters = new ArrayList<>(20);

        while (clusterIterator.hasNext()) {
            clusters.add(clusterIterator.next());
        }

        Assert.assertEquals(2, clusters.size());

        return clusters;
    }

    @Test
    public void testLoadOriginalPeaks() throws Exception {
        IPropertyStorage propertyStorage = new InMemoryPropertyStorage();
        List<ICluster> clusters = loadClusters(propertyStorage);

        // get the original peaks
        for (ICluster cluster : clusters) {
            List<ConsensusPeak> rawPeaks = AbstractConsensusSpectrumBuilder.loadOriginalPeaks(cluster, propertyStorage, true);

            Assert.assertEquals(50, rawPeaks.size());

            // max peak must always be the same
            double maxValue = rawPeaks.stream().mapToDouble(ConsensusPeak::getIntensity).max().getAsDouble();

            Assert.assertEquals(MaxPeakNormalizer.MAX_INTENSITY, maxValue, 0);
        }
    }

    @Test
    public void testAveragePrecursorMz () throws Exception {
        IPropertyStorage propertyStorage = new InMemoryPropertyStorage();
        List<ICluster> clusters = loadClusters(propertyStorage);

        // get the average precuror m/z for every cluster
        for (ICluster cluster : clusters) {
            Double averageMz = AbstractConsensusSpectrumBuilder.getAveragePrecursorMz(cluster, propertyStorage);

            Assert.assertEquals(400.29, averageMz, 0.01);
        }
    }

    @Test
    public void testMergePeaks() throws Exception {
        ConsensusPeak p1 = new ConsensusPeak(10.0, 1.0);
        ConsensusPeak p2 = new ConsensusPeak(10.1, 2.0);
        ConsensusPeak p3 = new ConsensusPeak(11.0, 1.0);

        List<ConsensusPeak> peaks = Arrays.stream(new ConsensusPeak[]{p1, p2, p3}).collect(Collectors.toList());

        List<ConsensusPeak> mergedPeaks = AbstractConsensusSpectrumBuilder.mergeConsensusPeaks(peaks, 0.5);

        Assert.assertEquals(2, mergedPeaks.size());
        Assert.assertEquals(10.05, mergedPeaks.get(0).getMz(), 0);
        Assert.assertEquals(2, mergedPeaks.get(0).getCount());
        Assert.assertEquals(1.5, mergedPeaks.get(0).getIntensity(), 0);
        Assert.assertEquals(1, mergedPeaks.get(1).getCount());
    }
}
