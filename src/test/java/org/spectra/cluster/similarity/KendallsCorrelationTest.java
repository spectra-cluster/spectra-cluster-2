package org.spectra.cluster.similarity;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.spectra.cluster.model.spectra.BinarySpectrum;
import org.spectra.cluster.normalizer.IIntegerNormalizer;
import org.spectra.cluster.normalizer.SequestBinner;
import uk.ac.ebi.pride.tools.jmzreader.JMzReaderException;
import uk.ac.ebi.pride.tools.jmzreader.model.Spectrum;
import uk.ac.ebi.pride.tools.mgf_parser.MgfFile;

import java.io.File;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.*;

public class KendallsCorrelationTest {
    List<Spectrum> spectra = new ArrayList<>();

    @Before
    public void setUp() throws JMzReaderException, URISyntaxException {
        URI uri = BinarySpectrum.class.getClassLoader().getResource("same_sequence_cluster.mgf").toURI();
        MgfFile mgfFile = new MgfFile(new File(uri));
        Iterator<Spectrum> spectrumIterator = mgfFile.getSpectrumIterator();
        while (spectrumIterator.hasNext()) {
            spectra.add(spectrumIterator.next());
        }
    }

    @Test
    public void testCorrelation() {
        KendallsCorrelation myKendall = new KendallsCorrelation();
        org.apache.commons.math3.stat.correlation.KendallsCorrelation orgKendall = new org.apache.commons.math3.stat.correlation.KendallsCorrelation();
        IIntegerNormalizer normalizer = new SequestBinner();

        for (int i = 0; i < spectra.size() - 1; i++) {
            Spectrum s1 = spectra.get(i);
            Spectrum s2 = spectra.get(i + 1);

            // simply use the first n peaks
            int nPeaks = 100;
            int maxPeaks = Math.min(s1.getPeakList().size(), s2.getPeakList().size());
            if (nPeaks > maxPeaks) {
                nPeaks = maxPeaks;
            }

            // create the list of integers
            int[] intens1 = new int[nPeaks];
            int[] intens2 = new int[nPeaks];

            List<Double> allIntens1 = new ArrayList<>(s1.getPeakList().values());
            List<Double> allIntens2 = new ArrayList<>(s2.getPeakList().values());

            double[] doubles1 = new double[nPeaks];
            double[] doubles2 = new double[nPeaks];

            for (int j = 0; j < nPeaks; j++) {
                doubles1[j] = Math.round(allIntens1.get(j));
                intens1[j] = (int) Math.round(allIntens1.get(j));

                doubles2[j] = Math.round(allIntens2.get(j));
                intens2[j] = (int) Math.round(allIntens2.get(j));
            }

            // compare the taus
            double kOrg = orgKendall.correlation(doubles1, doubles2);
            double kNew = myKendall.correlation(intens1, intens2);

            Assert.assertEquals(kOrg, kNew, 0.0000001);

        }
    }
}
