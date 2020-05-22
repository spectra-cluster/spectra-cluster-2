package org.spectra.cluster.io;

import org.bigbio.pgatk.io.properties.InMemoryPropertyStorage;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.spectra.cluster.cdf.MinNumberComparisonsAssessor;
import org.spectra.cluster.cdf.SpectraPerBinNumberComparisonAssessor;
import org.spectra.cluster.engine.GreedyClusteringEngine;
import org.spectra.cluster.engine.IClusteringEngine;
import org.spectra.cluster.exceptions.SpectraClusterException;
import org.spectra.cluster.io.spectra.MzSpectraReader;
import org.spectra.cluster.model.cluster.ICluster;
import org.spectra.cluster.model.consensus.GreedyConsensusSpectrum;
import org.spectra.cluster.model.spectra.BinaryPeak;
import org.spectra.cluster.model.spectra.BinarySpectrum;
import org.spectra.cluster.model.spectra.IBinarySpectrum;
import org.spectra.cluster.normalizer.BasicIntegerNormalizer;
import org.spectra.cluster.predicates.ShareHighestPeaksClusterPredicate;
import org.spectra.cluster.similarity.CombinedFisherIntensityTest;

import java.io.File;
import java.net.URI;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;

/**
 * This code is licensed under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * ==Overview==
 *
 * @author ypriverol on 14/08/2018.
 */
public class MzSpectraReaderTest {

    MzSpectraReader spectraReader;
    MzSpectraReader clusteringReader;

    @Before
    public void setUp() throws Exception {

        URI uri = Objects.requireNonNull(BinarySpectrum.class.getClassLoader().getResource("single-spectra.mgf")).toURI();
        File mgfFile = new File(uri);
        spectraReader = new MzSpectraReader(mgfFile, GreedyClusteringEngine.COMPARISON_FILTER);

        uri = Objects.requireNonNull(BinarySpectrum.class.getClassLoader()
                .getResource("testfile.clustering")).toURI();

        IClusteringEngine engine = new GreedyClusteringEngine(BasicIntegerNormalizer.MZ_CONSTANT,
                1, 0.99f, 5, new CombinedFisherIntensityTest(),
                new MinNumberComparisonsAssessor(10000), new ShareHighestPeaksClusterPredicate(5),
                GreedyConsensusSpectrum.NOISE_FILTER_INCREMENT);

        File clusteringFile = new File(uri);
        clusteringReader = new MzSpectraReader(clusteringFile, GreedyClusteringEngine.COMPARISON_FILTER, engine);
    }

    @Test
    public void readBinarySpectraIterator() throws SpectraClusterException {

        Iterator<IBinarySpectrum> binaryIter = spectraReader.readBinarySpectraIterator();
        int count = 0;
        while(binaryIter.hasNext()){
            Assert.assertTrue(binaryIter.next().getCopyIntensityVector().length > 0);
            count++;
        }
        Assert.assertEquals(2, count);
    }

    @Test
    public void readClusteringIterator() throws SpectraClusterException {

        Iterator<ICluster> binaryIter = clusteringReader.readClusterIterator();
        int count = 0;
        while(binaryIter.hasNext()){
            ICluster cluster = binaryIter.next();
            Assert.assertTrue(cluster.getId() != null);
            count++;
        }
        Assert.assertEquals(107, count);
    }

    @Test
    public void testNoNullSpectra() throws Exception {
        File testFile = new File(MzSpectraReaderTest.class.getClassLoader().getResource("same_sequence_cluster.mgf").toURI());
        MzSpectraReader reader = new MzSpectraReader(testFile, GreedyClusteringEngine.COMPARISON_FILTER);
        Iterator<IBinarySpectrum> iterator = reader.readBinarySpectraIterator();

        while (iterator.hasNext()) {
            IBinarySpectrum spectrum = iterator.next();

            // make sure there are no null peaks
            BinaryPeak[] peaks = spectrum.getPeaks();

            for (BinaryPeak peak : peaks) {
                Assert.assertNotNull(peak);
            }
        }
    }

    @Test
    public void testPropertyLoading() throws Exception {
        File testFile = new File(MzSpectraReaderTest.class.getClassLoader().getResource("same_sequence_cluster.mgf").toURI());
        MzSpectraReader reader = new MzSpectraReader(testFile, GreedyClusteringEngine.COMPARISON_FILTER);

        InMemoryPropertyStorage storage = new InMemoryPropertyStorage();

        Iterator<IBinarySpectrum> iterator = reader.readBinarySpectraIterator(storage);
        List<String> specIds = new ArrayList<>();

        while (iterator.hasNext()) {
            IBinarySpectrum spectrum = iterator.next();
            specIds.add(spectrum.getUUI());
        }

        Assert.assertEquals(158, specIds.size());
        int nIdentified = 0;

        // test the properties exist
        for (String id : specIds) {
            if (storage.get(id, "Sequence") != null) {
                nIdentified++;
            }

            Assert.assertNotNull("Missing retention time for " + id, storage.get(id, "retention time"));
            Assert.assertNotNull("Missing title for " + id, storage.get(id, "spectrum title"));
        }

        Assert.assertEquals(136, nIdentified);

        Assert.assertEquals(7, storage.getAvailableProperties().size());
    }

    @Test
    public void testSpectrumListener() throws Exception {
        File testFile = new File(getClass().getClassLoader().getResource("synthetic_mixed_runs.mgf").toURI());
        MzSpectraReader reader = new MzSpectraReader(testFile, GreedyClusteringEngine.COMPARISON_FILTER);
        SpectraPerBinNumberComparisonAssessor assessor = new SpectraPerBinNumberComparisonAssessor(
                BasicIntegerNormalizer.MZ_CONSTANT, 1, BasicIntegerNormalizer.MZ_CONSTANT * 5000);

        reader.addSpectrumListener(assessor);

        // process all spectra
        Iterator<IBinarySpectrum> spectrumIterator = reader.readBinarySpectraIterator();

        while (spectrumIterator.hasNext()) {
            Assert.assertNotNull(spectrumIterator.next());
        }

        // true values were created using R script
        Assert.assertEquals(17, assessor.getNumberOfComparisons(BasicIntegerNormalizer.MZ_CONSTANT * 503, 1));
        Assert.assertEquals(59, assessor.getNumberOfComparisons(BasicIntegerNormalizer.MZ_CONSTANT * 633, 1));
        Assert.assertEquals(2, assessor.getNumberOfComparisons(BasicIntegerNormalizer.MZ_CONSTANT * 1258, 1));
        Assert.assertEquals(109, assessor.getNumberOfComparisons(BasicIntegerNormalizer.MZ_CONSTANT * 957, 1));

        Assert.assertEquals(1, assessor.getNumberOfComparisons(BasicIntegerNormalizer.MZ_CONSTANT * 300, 1));
    }
}