package org.spectra.cluster.similarity;


import io.github.bigbio.pgatk.io.common.spectra.Spectrum;
import io.github.bigbio.pgatk.io.mgf.MgfIterableReader;
import lombok.extern.slf4j.Slf4j;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.spectra.cluster.engine.GreedyClusteringEngine;
import org.spectra.cluster.filter.binaryspectrum.HighestIntensityNPeaksFunction;
import org.spectra.cluster.io.spectra.MzSpectraReader;
import org.spectra.cluster.model.spectra.BinarySpectrum;
import org.spectra.cluster.model.spectra.IBinarySpectrum;
import org.spectra.cluster.normalizer.FactoryNormalizer;
import org.spectra.cluster.normalizer.LSHBinner;
import org.spectra.cluster.normalizer.MaxPeakNormalizer;
import org.spectra.cluster.normalizer.TideBinner;
import org.spectra.cluster.util.ClusteringParameters;

import java.io.File;
import java.net.URI;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;

@Slf4j
public class JaccardCorrelationTest {

    BinarySpectrum binarySpectrum1 = null;
    BinarySpectrum binarySpectrum2 = null;

    Spectrum spectrum1 = null;
    Spectrum spectrum2 = null;

    FactoryNormalizer binnerNormalizer = new FactoryNormalizer(new TideBinner(), new MaxPeakNormalizer());


    @Before
    public void setUp() throws Exception {

        URI uri = Objects.requireNonNull(BinarySpectrum.class.getClassLoader().getResource("single-spectra.mgf")).toURI();
        MgfIterableReader mgfFile = new MgfIterableReader(new File(uri), true, false, true);


        if(mgfFile.hasNext())
            spectrum1 = mgfFile.next();

        binarySpectrum1 = new BinarySpectrum((int)spectrum1.getPrecursorMZ().doubleValue(), spectrum1.getPrecursorCharge(),
                binnerNormalizer.normalizePeaks(spectrum1.getPeakList()), GreedyClusteringEngine.COMPARISON_FILTER);

        if(mgfFile.hasNext())
            spectrum2 = mgfFile.next();

        binarySpectrum2 = new BinarySpectrum((int)spectrum2.getPrecursorMZ().doubleValue(), spectrum2.getPrecursorCharge(),
                binnerNormalizer.normalizePeaks(spectrum1.getPeakList()), GreedyClusteringEngine.COMPARISON_FILTER);

        /* Read the Spectra from similar files **/
        uri = Objects.requireNonNull(BinarySpectrum.class.getClassLoader().getResource("most_similar_1.mgf")).toURI();
        mgfFile = new MgfIterableReader(new File(uri), true, false, true);

    }

    @Test
    public void jaccardCorrelationTest() {

        JaccardCorrelation correlation = new JaccardCorrelation();

        double similarity = correlation.correlation(binarySpectrum1.getCopyMzVector(),
                binarySpectrum2.getCopyMzVector());
        Assert.assertTrue(similarity - 0.99f < 0.1);
    }

    @Test
    public void lshBinnerJaccardCorrelationTest(){
        LSHBinner lshBinner = LSHBinner.getInstance();
        JaccardCorrelation correlation = new JaccardCorrelation();

        int[] vector1 = lshBinner.getKernels(binarySpectrum1.getCopyMzVector());
        int[] vector2 = lshBinner.getKernels(binarySpectrum2.getCopyMzVector());

        double similarity = correlation.correlation(vector1, vector2);
        Assert.assertTrue(similarity - 0.99f < 0.1);

    }

    @Test
    public void testJaccardInSyntheticPeptides() throws Exception {
        URI uri = Objects.requireNonNull(BinarySpectrum.class.getClassLoader().getResource("synthetic_first_pool_3xHCD_R1.mgf")).toURI();
        MzSpectraReader reader = new MzSpectraReader(new ClusteringParameters(), new File(uri));

        Iterator<IBinarySpectrum> specIt = reader.readBinarySpectraIterator();
        List<IBinarySpectrum> spectra = new ArrayList<>();
        int numSpectra = 0;
        while (specIt.hasNext() && numSpectra < 11){
            spectra.add(specIt.next());
            numSpectra++;
        }

        LSHBinner lshBinner = LSHBinner.getInstance();
        JaccardCorrelation correlation = new JaccardCorrelation();
        HighestIntensityNPeaksFunction functionNpeaks = new HighestIntensityNPeaksFunction(100);



        for(int i = 0; i < spectra.size(); i++){
            for(int j = i+1; j < spectra.size(); j++){
                double similarity = correlation.correlation(spectra.get(i).getCopyMzVector(),
                        spectra.get(j).getCopyMzVector());

                int[] vector1 = lshBinner.getKernels(spectra.get(i).getCopyMzVector());
                int[] vector2 = lshBinner.getKernels(spectra.get(j).getCopyMzVector());
                double similarityLSH = correlation.correlation(vector1, vector2);

                System.out.println("New Comparison");
                log.info("The Jaccard for Spectrum: " + i + " and " + j + " is: " + similarity + " and LSH Score is: " + similarityLSH + " Difference: " + (similarity - similarityLSH));
                Assert.assertTrue(Math.abs(similarity - similarityLSH) < 1);


                similarity = correlation.correlation(functionNpeaks.apply(spectra.get(i)).getCopyMzVector(),
                        functionNpeaks.apply(spectra.get(j)).getCopyMzVector());

                vector1 = lshBinner.getKernels(functionNpeaks.apply(spectra.get(i)).getCopyMzVector());
                vector2 = lshBinner.getKernels(functionNpeaks.apply(spectra.get(j)).getCopyMzVector());
                similarityLSH = correlation.correlation(vector1, vector2);

                log.info("The Jaccard for 100 Intensity Peaks: " + i + " and " + j + " is: " + similarity + " and LSH Score is: " + similarityLSH + " Difference: " + (similarity - similarityLSH));
                Assert.assertTrue(Math.abs(similarity - similarityLSH) < 1);
            }
        }




    }


}
