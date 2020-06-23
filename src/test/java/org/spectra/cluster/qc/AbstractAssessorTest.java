package org.spectra.cluster.qc;

import io.github.bigbio.pgatk.io.properties.IPropertyStorage;
import io.github.bigbio.pgatk.io.properties.InMemoryPropertyStorage;
import org.junit.Before;
import org.spectra.cluster.cdf.SpectraPerBinNumberComparisonAssessor;
import org.spectra.cluster.engine.GreedyClusteringEngine;
import org.spectra.cluster.engine.IClusteringEngine;
import org.spectra.cluster.filter.binaryspectrum.HighestPeakPerBinFunction;
import org.spectra.cluster.filter.rawpeaks.*;
import org.spectra.cluster.io.spectra.MzSpectraReader;
import org.spectra.cluster.model.cluster.ICluster;
import org.spectra.cluster.model.spectra.BinarySpectrum;
import org.spectra.cluster.normalizer.BasicIntegerNormalizer;
import org.spectra.cluster.normalizer.MaxPeakNormalizer;
import org.spectra.cluster.normalizer.TideBinner;
import org.spectra.cluster.predicates.IComparisonPredicate;
import org.spectra.cluster.predicates.ShareNComparisonPeaksPredicate;
import org.spectra.cluster.similarity.CombinedFisherIntensityTest;

import java.io.File;
import java.net.URI;
import java.util.*;

public abstract class AbstractAssessorTest {
    protected ICluster[] clusters;
    protected IPropertyStorage properties;

    @Before
    public void setUp() throws Exception {
        // count the spectra per bin for the comparison assessor
        SpectraPerBinNumberComparisonAssessor comparisonAssessor = new SpectraPerBinNumberComparisonAssessor(
                Math.round(2 * BasicIntegerNormalizer.MZ_CONSTANT), 0, BasicIntegerNormalizer.MZ_CONSTANT * 3000);

        // set the loading filter
        IRawSpectrumFunction loadingFilter = new RemoveImpossiblyHighPeaksFunction()
                .specAndThen(new RemovePrecursorPeaksFunction(0.5))
                .specAndThen(new RawPeaksWrapperFunction(new KeepNHighestRawPeaks(40)));

        properties = new InMemoryPropertyStorage();

        // open the file
        URI uri = Objects.requireNonNull(BinarySpectrum.class.getClassLoader().getResource("synthetic_mixed_runs.mgf")).toURI();
        File mgfFile = new File(uri);

        IComparisonPredicate<ICluster> firstRoundPredicate = new ShareNComparisonPeaksPredicate(5);
        IClusteringEngine engine = new GreedyClusteringEngine(
                1 * BasicIntegerNormalizer.MZ_CONSTANT,
                1, 0.99F, 5, new CombinedFisherIntensityTest(),
                comparisonAssessor, firstRoundPredicate,
                100);

        MzSpectraReader spectraReader = new MzSpectraReader( new TideBinner(), new MaxPeakNormalizer(),
                new BasicIntegerNormalizer(), new HighestPeakPerBinFunction(), loadingFilter,
                GreedyClusteringEngine.COMPARISON_FILTER, engine, mgfFile);
        spectraReader.addSpectrumListener(comparisonAssessor);

        // get all spectra
        List<ICluster> spectra = new ArrayList<>(100);
        Iterator<ICluster> it = spectraReader.readClusterIterator(properties);

        while (it.hasNext()) {
            spectra.add(it.next());
        }

        // cluster the spectra
        spectra.sort(Comparator.comparingInt(ICluster::getPrecursorMz));
        clusters = engine.clusterSpectra(spectra.toArray(new ICluster[spectra.size()]));
    }
}
