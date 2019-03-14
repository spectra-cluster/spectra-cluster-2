package org.spectra.cluster.engine;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.spectra.cluster.cdf.MinNumberComparisonsAssessor;
import org.spectra.cluster.filter.binaryspectrum.HighestPeakPerBinFunction;
import org.spectra.cluster.filter.rawpeaks.*;
import org.spectra.cluster.io.cluster.DotClusteringWriter;
import org.spectra.cluster.io.cluster.IClusterWriter;
import org.spectra.cluster.io.properties.IPropertyStorage;
import org.spectra.cluster.io.properties.InMemoryPropertyStorage;
import org.spectra.cluster.io.spectra.MzSpectraReader;
import org.spectra.cluster.model.cluster.ICluster;
import org.spectra.cluster.model.spectra.IBinarySpectrum;
import org.spectra.cluster.normalizer.BasicIntegerNormalizer;
import org.spectra.cluster.normalizer.MaxPeakNormalizer;
import org.spectra.cluster.normalizer.TideBinner;
import org.spectra.cluster.similarity.CombinedFisherIntensityTest;

import java.io.File;
import java.net.URL;
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

    /** These tests are only intended for local comparisons and debugging */
    private static final boolean runLocalTests = false;

    @Before
    public void setUp() throws Exception {
        loadingFilter = new RemoveImpossiblyHighPeaksFunction()
                        .specAndThen(new RemovePrecursorPeaksFunction(0.5))
                        .specAndThen(new RawPeaksWrapperFunction(new KeepNHighestRawPeaks(40)));

        File mgfFile = new File(GreedyClusteringEngineTest.class.getClassLoader().getResource("same_sequence_cluster.mgf").toURI());
        MzSpectraReader reader = new MzSpectraReader(mgfFile,
                new TideBinner(),
                new MaxPeakNormalizer(),
                new BasicIntegerNormalizer(),
                new HighestPeakPerBinFunction(),
                loadingFilter,
                GreedyClusteringEngine.COMPARISON_FILTER);
        Iterator<IBinarySpectrum> iterator = reader.readBinarySpectraIterator();

        while (iterator.hasNext()) {
            IBinarySpectrum s = iterator.next();
            spectrumIdToActualPrecursor.put(s.getUUI(), s.getPrecursorMz());
            spectra.add(s);
        }

        // sort the spectra
        spectra.sort(Comparator.comparingInt(IBinarySpectrum::getPrecursorMz));

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
        IClusteringEngine engine = new GreedyClusteringEngine(BasicIntegerNormalizer.MZ_CONSTANT,
                1, 0.99f, 5, new CombinedFisherIntensityTest(),
                new MinNumberComparisonsAssessor(10000), 5);

        ICluster[] clusters = engine.clusterSpectra(spectra.toArray(new IBinarySpectrum[0]));

        boolean verbose = false;

        // This number of clusters is actually too high
        Assert.assertEquals(8, clusters.length);

        // all clusters must have only 1 peptide sequence
        for (ICluster cluster : clusters) {
            if (verbose) {
                System.out.println("----- " + cluster.getId() + " -------");
                cluster.getClusteredSpectraIds().forEach(
                        s -> System.out.println(String.format("%.2f - %d - %s", spectrumIdToPrecursor.get(s), spectrumIdToActualPrecursor.get(s), spectrumIdToSequence.get(s)))
                );
            }

            Set<String> peptides = cluster.getClusteredSpectraIds().stream()
                    .map(s -> spectrumIdToSequence.get(s))
                    .filter(s -> s.length() > 0)
                    .collect(Collectors.toSet());

            // TODO: We actually should get clean clusters in this test
            // Assert.assertEquals(String.format("Multiple peptides in cluster %d: %s", i, String.join(", ", peptides)), 1, peptides.size());
        }
    }

    @Test
    public void localTestSyntheticPeptides() throws Exception {
        if (!runLocalTests) {
            return;
        }

        // only run the test if the file is present
        URL testFile = getClass().getClassLoader().getResource("synthetic_mixed_runs.mgf");
        if (testFile == null) {
            return;
        }

        File synthFile = new File(testFile.toURI());
        String resultFile = synthFile.getAbsolutePath() + "_new.clustering";
        clusterFile(synthFile, Paths.get(resultFile));
    }

    @Test
    public void localTestImp() throws Exception {
        if (!runLocalTests) {
            return;
        }

        // only run the test if the file is present
        URL testFile = getClass().getClassLoader().getResource("imp_hela_test.mgf");
        if (testFile == null) {
            return;
        }

        File synthFile = new File(testFile.toURI());
        String resultFile = synthFile.getAbsolutePath() + "_new.clustering";
        clusterFileIteratively(synthFile, Paths.get(resultFile));
    }

    @Test
    public void localTestTwoSpectra() throws Exception {
        if (!runLocalTests) {
            return;
        }

        // only run the test if the file is present
        URL testFile = getClass().getClassLoader().getResource("two_spectra.mgf");
        if (testFile == null) {
            return;
        }

        File twoSpecFile = new File(testFile.toURI());
        String resultFile = twoSpecFile.getAbsolutePath() + "_new.clustering";
        clusterFile(twoSpecFile, Paths.get(resultFile));
    }

    @Test
    public void localTestSingleClusterImp() throws Exception {
        if (!runLocalTests) {
            return;
        }

        // only run the test if the file is present
        URL testFile = getClass().getClassLoader().getResource("imp_single_cluster.mgf");
        if (testFile == null) {
            return;
        }

        File twoSpecFile = new File(testFile.toURI());
        String resultFile = twoSpecFile.getAbsolutePath() + "_new.clustering";
        ICluster[] clusters = clusterFile(twoSpecFile, Paths.get(resultFile));

        Assert.assertEquals(1, clusters.length);
    }

    private ICluster[]  clusterFile(File mgfFile, Path resultFile) throws Exception {
        // load the spectra
        IPropertyStorage localStorage = new InMemoryPropertyStorage();
        MzSpectraReader reader = new MzSpectraReader(mgfFile, new TideBinner(), new MaxPeakNormalizer(),
                new BasicIntegerNormalizer(), new HighestPeakPerBinFunction(), loadingFilter, GreedyClusteringEngine.COMPARISON_FILTER);
        Iterator<IBinarySpectrum> iterator = reader.readBinarySpectraIterator(localStorage);
        List<IBinarySpectrum> spectra = new ArrayList<>(1_000);

        while (iterator.hasNext()) {
            spectra.add(iterator.next());
        }

        // sort according to m/z
        spectra.sort(Comparator.comparingInt(IBinarySpectrum::getPrecursorMz));

        for (int i = 0; i < spectra.size(); i++) {
            if (spectra.get(i).getPrecursorMz() > 977 * BasicIntegerNormalizer.MZ_CONSTANT) {
                System.out.println("977 starting at " + String.valueOf(i));
                // change the id
                IBinarySpectrum orgSpec = spectra.get(i);
                //spectra.set(i, new BinarySpectrum("the_spec",
                //        orgSpec.getPrecursorMz(), orgSpec.getPrecursorCharge(), orgSpec.getPeaks()));
                break;
            }
        }

        // cluster everything
        IClusteringEngine engine = new GreedyClusteringEngine(BasicIntegerNormalizer.MZ_CONSTANT,
                1, 0.99f, 5, new CombinedFisherIntensityTest(),
                new MinNumberComparisonsAssessor(10_000), 5);

        ICluster[] clusters = engine.clusterSpectra(spectra.toArray(new IBinarySpectrum[0]));

        IClusterWriter writer = new DotClusteringWriter(resultFile, false, localStorage);
        writer.appendClusters(clusters);
        writer.close();

        System.out.println("Results written to " + resultFile.toString());

        return clusters;
    }

    private void  clusterFileIteratively(File mgfFile, Path resultFile) throws Exception {
        // load the spectra
        IPropertyStorage localStorage = new InMemoryPropertyStorage();
        MzSpectraReader reader = new MzSpectraReader(mgfFile, new TideBinner(), new MaxPeakNormalizer(),
                new BasicIntegerNormalizer(), new HighestPeakPerBinFunction(), loadingFilter, GreedyClusteringEngine.COMPARISON_FILTER);
        Iterator<IBinarySpectrum> iterator = reader.readBinarySpectraIterator(localStorage);
        List<IBinarySpectrum> spectra = new ArrayList<>(1_000);

        while (iterator.hasNext()) {
            spectra.add(iterator.next());
        }

        // sort according to m/z
        spectra.sort(Comparator.comparingInt(IBinarySpectrum::getPrecursorMz));

        // cluster everything
        float[] thresholds = {0.99f, 0.98f, 0.95f, 0.995f};

        for (float t : thresholds) {
            Path thisResult = Paths.get(resultFile.toString() + '_' + String.valueOf(t));

            IClusteringEngine engine = new GreedyClusteringEngine(BasicIntegerNormalizer.MZ_CONSTANT,
                    1, t, 5, new CombinedFisherIntensityTest(),
                    new MinNumberComparisonsAssessor(10_000), 5);

            ICluster[] clusters = engine.clusterSpectra(spectra.toArray(new IBinarySpectrum[0]));

            IClusterWriter writer = new DotClusteringWriter(thisResult, false, localStorage);
            writer.appendClusters(clusters);
            writer.close();

            System.out.println("Results written to " + thisResult.toString());
        }
    }
}
