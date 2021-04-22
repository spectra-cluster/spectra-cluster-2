package org.spectra.cluster.consensus;

import io.github.bigbio.pgatk.io.common.spectra.Spectrum;
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
import org.spectra.cluster.predicates.ShareHighestPeaksClusterPredicate;
import org.spectra.cluster.similarity.CombinedFisherIntensityTest;
import org.spectra.cluster.util.ClusteringParameters;

import java.io.File;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;

public class AverageConsensusSpectrumBuilderTest {
    @Test
    public void testAverageSpectrum() throws Exception {
        ClusteringParameters params = new ClusteringParameters();
        IConsensusSpectrumBuilder builder = new AverageConsensusSpectrumBuilder(params);

        // load the spectra
        File mgfFile = new File(getClass().getClassLoader().getResource("imp_single_cluster.mgf").toURI());
        IPropertyStorage localStorage = new InMemoryPropertyStorage();
        MzSpectraReader reader = new MzSpectraReader(new ClusteringParameters(), mgfFile);

        Iterator<ICluster> iterator = reader.readClusterIterator(localStorage);
        List<ICluster> spectra = new ArrayList<>(1_000);

        while (iterator.hasNext()) {
            spectra.add(iterator.next());
        }

        // sort according to m/z
        spectra.sort(Comparator.comparingInt(ICluster::getPrecursorMz));

        // cluster everything
        IClusteringEngine engine = new GreedyClusteringEngine(BasicIntegerNormalizer.MZ_CONSTANT,
                1, 0.99f, 5, new CombinedFisherIntensityTest(),
                new MinNumberComparisonsAssessor(10_000), new ShareHighestPeaksClusterPredicate(5),
                GreedyConsensusSpectrum.NOISE_FILTER_INCREMENT);

        ICluster[] clusters = engine.clusterSpectra(spectra.toArray(new ICluster[spectra.size()]));

        Assert.assertEquals(1, clusters.length);

        // create the consensus spectrum
        Spectrum consensusSpectrum = builder.createConsensusSpectrum(clusters[0], localStorage);

        Assert.assertNotNull(consensusSpectrum);
        Assert.assertEquals(2, (int) consensusSpectrum.getPrecursorCharge());
        Assert.assertEquals(2, (int) consensusSpectrum.getMsLevel());
        Assert.assertEquals(69, consensusSpectrum.getPeakList().size());
        Assert.assertEquals(402.717, consensusSpectrum.getPrecursorMZ(), 0.001);
    }
}
