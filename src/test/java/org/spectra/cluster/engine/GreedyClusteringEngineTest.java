package org.spectra.cluster.engine;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.spectra.cluster.cdf.MinNumberComparisonsAssessor;
import org.spectra.cluster.filter.binaryspectrum.HighestPeakPerBinFunction;
import org.spectra.cluster.filter.rawpeaks.*;
import org.spectra.cluster.io.*;
import org.spectra.cluster.model.cluster.ICluster;
import org.spectra.cluster.model.spectra.IBinarySpectrum;
import org.spectra.cluster.normalizer.BasicIntegerNormalizer;
import org.spectra.cluster.normalizer.MaxPeakNormalizer;
import org.spectra.cluster.normalizer.TideBinner;
import org.spectra.cluster.similarity.CombinedFisherIntensityTest;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;

public class GreedyClusteringEngineTest {
    IRawSpectrumFunction loadingFilter;

    private List<IBinarySpectrum> spectra = new ArrayList<>(30);
    Map<String, String> spectrumIdToSequence = new HashMap<>(30);
    Map<String, Double> spectrumIdToPrecursor = new HashMap<>(30);
    Map<String, Integer> spectrumIdToActualPrecursor = new HashMap<>(30);

    @Before
    public void setUp() throws Exception {
        loadingFilter = new RemoveImpossiblyHighPeaksFunction();
        loadingFilter.andThen(new RemovePrecursorPeaksFunction(0.5))
                .andThen(new RawPeaksWrapperFunction(new KeepNHighestRawPeaks(70)));

        File mgfFile = new File(GreedyClusteringEngineTest.class.getClassLoader().getResource("same_sequence_cluster.mgf").toURI());
        MzSpectraReader reader = new MzSpectraReader(mgfFile);
        Iterator<IBinarySpectrum> iterator = reader.readBinarySpectraIterator();

        while (iterator.hasNext()) {
            IBinarySpectrum s = iterator.next();
            spectrumIdToActualPrecursor.put(s.getUUI(), s.getPrecursorMz());
            spectra.add(s);
        }

        // get the spectra ids
        String[] ids = spectra.stream().map(IBinarySpectrum::getUUI).toArray(String[]::new);
        Double[] precursors = Files.lines(Paths.get(GreedyClusteringEngineTest.class.getClassLoader().getResource("same_sequence_cluster.mgf").toURI()))
                .filter(s -> s.startsWith("PEPMASS="))
                .map(s -> s.substring(8, 15))
                .map(Double::new)
                .toArray(Double[]::new);
        String[] lines = Files.lines(Paths.get(GreedyClusteringEngineTest.class.getClassLoader().getResource("same_sequence_cluster.mgf").toURI()))
                .toArray(String[]::new);
        String[] peptides = new String[ids.length];
        int currentPeptide = 0;
        boolean specHasPsm = false;

        for (String line : lines) {
            if (line.startsWith("SEQ=")) {
                peptides[currentPeptide] = line.substring(4);
                specHasPsm = true;
            }

            if (line.contains("END IONS")) {
                if (!specHasPsm) {
                    peptides[currentPeptide] = "";
                }

                specHasPsm = false;
                currentPeptide++;
            }
        }

        if (ids.length != precursors.length) {
            throw new Exception("Failed to extract peptides");
        }

        for (int i = 0; i < ids.length; i++) {
            spectrumIdToSequence.put(ids[i], peptides[i]);
            spectrumIdToPrecursor.put(ids[i], precursors[i]);
        }
    }

    @Test
    public void testClustering() throws Exception {
        IClusteringEngine engine = new GreedyClusteringEngine(10_000, 1, 0.99f, 5, new CombinedFisherIntensityTest(), new MinNumberComparisonsAssessor(10000), 5);

        ICluster[] clusters = engine.clusterSpectra(spectra.toArray(new IBinarySpectrum[0]));

        // This number of clusters is actually too high
        Assert.assertEquals(7, clusters.length);

        // all clusters must have only 1 peptide sequence
        for (int i = 0; i < clusters.length; i++) {
            ICluster cluster = clusters[i];

            System.out.println("----- " + cluster.getId() + " -------");
            cluster.getClusteredSpectraIds().forEach(
                    s -> {
                        System.out.println(String.format("%.2f - %d - %s", spectrumIdToPrecursor.get(s), spectrumIdToActualPrecursor.get(s), spectrumIdToSequence.get(s)));
                    }
            );

            Set<String> peptides = cluster.getClusteredSpectraIds().stream()
                    .map(s -> spectrumIdToSequence.get(s))
                    .filter(s -> s.length() > 0)
                    .collect(Collectors.toSet());

            Assert.assertEquals(String.format("Multiple peptides in cluster %d: %s", i, String.join(", ", peptides)), 1, peptides.size());
        }
    }

    @Test
    public void localTestSyntheticPeptides() throws Exception {
        File synthFile = new File(getClass().getClassLoader().getResource("synthetic_mixed_runs.mgf").toURI());
        String resultFile = synthFile.getAbsolutePath() + "_new.clustering";
        clusterFile(synthFile, Paths.get(resultFile));
    }

    @Test
    public void localTestImp() throws Exception {
        /**
         * This test is not intended to be run as part of the test suite
         */
        File synthFile = new File(getClass().getClassLoader().getResource("imp_hela_test.mgf").toURI());
        String resultFile = synthFile.getAbsolutePath() + "_new.clustering";
        clusterFile(synthFile, Paths.get(resultFile));
    }

    @Test
    public void localTestTwoSpectra() throws Exception {
        File twoSpecFile = new File(getClass().getClassLoader().getResource("two_spectra.mgf").toURI());
        String resultFile = twoSpecFile.getAbsolutePath() + "_new.clustering";
        clusterFile(twoSpecFile, Paths.get(resultFile));
    }

    private void clusterFile(File mgfFile, Path resultFile) throws Exception {
        // load the spectra
        IPropertyStorage localStorage = new InMemoryPropertyStorage();
        MzSpectraReader reader = new MzSpectraReader(mgfFile, new TideBinner(), new MaxPeakNormalizer(),
                new BasicIntegerNormalizer(), new HighestPeakPerBinFunction(), loadingFilter);
        Iterator<IBinarySpectrum> iterator = reader.readBinarySpectraIterator(localStorage);
        List<IBinarySpectrum> spectra = new ArrayList<>(1_000);

        while (iterator.hasNext()) {
            spectra.add(iterator.next());
        }

        // cluster everything
        IClusteringEngine engine = new GreedyClusteringEngine(10_000,
                1, 0.99f, 5, new CombinedFisherIntensityTest(),
                new MinNumberComparisonsAssessor(10000), 5);

        ICluster[] clusters = engine.clusterSpectra(spectra.toArray(new IBinarySpectrum[0]));

        IClusterWriter writer = new DotClusteringWriter(resultFile, false, localStorage);
        writer.appendClusters(clusters);
        writer.close();
    }
}
