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
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;

public class TestAbstractConsensusBuilder {
    @Test
    public void testLoadOriginalPeaks() throws Exception {
        // open the file
        URI uri = Objects.requireNonNull(getClass().getClassLoader().getResource("single-spectra.mgf")).toURI();
        File mgfFile = new File(uri);

        IClusteringEngine engine = new GreedyClusteringEngine(BasicIntegerNormalizer.MZ_CONSTANT,
                1, 0.99f, 5, new CombinedFisherIntensityTest(),
                new MinNumberComparisonsAssessor(10000), new ShareHighestPeaksClusterPredicate(5),
                GreedyConsensusSpectrum.NOISE_FILTER_INCREMENT);

        MzSpectraReader spectraReader = new MzSpectraReader(mgfFile, GreedyClusteringEngine.COMPARISON_FILTER, engine);

        IPropertyStorage propertyStorage = new InMemoryPropertyStorage();

        // read the spectra
        Iterator<ICluster> clusterIterator = spectraReader.readClusterIterator(propertyStorage);
        List<ICluster> clusters = new ArrayList<>(20);

        while (clusterIterator.hasNext()) {
            clusters.add(clusterIterator.next());
        }

        Assert.assertEquals(2, clusters.size());

        // get the original peaks
        for (ICluster cluster : clusters) {
            List<ConsensusPeak> rawPeaks = AbstractConsensusSpectrumBuilder.loadOriginalPeaks(cluster, propertyStorage, true);

            Assert.assertEquals(50, rawPeaks.size());

            // max peak must always be the same
            double maxValue = rawPeaks.stream().mapToDouble(ConsensusPeak::getIntensity).max().getAsDouble();

            Assert.assertEquals(MaxPeakNormalizer.MAX_INTENSITY, maxValue, 0);
        }
    }
}
