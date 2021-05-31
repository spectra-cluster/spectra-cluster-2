package org.spectra.cluster.filter;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.spectra.cluster.filter.binaryspectrum.HighestIntensityNPeaksFunction;
import org.spectra.cluster.filter.binaryspectrum.HighestPeakPerBinFunction;
import org.spectra.cluster.filter.binaryspectrum.IBinarySpectrumFunction;
import org.spectra.cluster.io.spectra.MzSpectraReader;
import org.spectra.cluster.model.spectra.BinaryPeak;
import org.spectra.cluster.model.spectra.IBinarySpectrum;
import org.spectra.cluster.util.ClusteringParameters;

import java.io.File;
import java.util.*;

public class HighestPeakPerBinFilterTest {


    Iterator<IBinarySpectrum> spectrumIterator;

    @Before
    public void setUp() throws Exception {
        File peakList = new File(Objects.requireNonNull(HighestPeakPerBinFilterTest.class.getClassLoader().getResource("same_sequence_cluster.mgf")).toURI());
        MzSpectraReader reader = new MzSpectraReader(new ClusteringParameters(), peakList);
        spectrumIterator = reader.readBinarySpectraIterator();
    }

    @Test
    public void testFilter() {
        // get the spectra

        List<IBinarySpectrum> allSpectra = new ArrayList<>();
        while (spectrumIterator.hasNext()) {
            IBinarySpectrum s = spectrumIterator.next();

            // sort the peaks
            Arrays.parallelSort(s.getPeaks(), Comparator.comparingInt(BinaryPeak::getMz));

            allSpectra.add(s);
        }

        // filter the spectra
        IBinarySpectrumFunction filter = new HighestPeakPerBinFunction();
        applyFilters(allSpectra, filter);
    }


    @Test
    public void testcombinedFilter() {
        // get the spectra
        List<IBinarySpectrum> allSpectra = new ArrayList<>();

        while (spectrumIterator.hasNext()) {
            IBinarySpectrum s = spectrumIterator.next();

            // sort the peaks
            Arrays.parallelSort(s.getPeaks(), Comparator.comparingInt(BinaryPeak::getMz));

            allSpectra.add(s);
        }

        // filter the spectra
        IBinarySpectrumFunction filter = new HighestPeakPerBinFunction();
        filter.andThen(new HighestIntensityNPeaksFunction(50));

        applyFilters(allSpectra, filter);

    }

    private void applyFilters(List<IBinarySpectrum> allSpectra, IBinarySpectrumFunction filter){

        for (int i = 0; i < allSpectra.size(); i++) {
            IBinarySpectrum s = allSpectra.get(i);
            IBinarySpectrum filtered = filter.apply(s);

            Assert.assertTrue(filtered.getPeaks().length <= s.getPeaks().length);

            // make sure there are no duplicate m/z
            int[] mz = filtered.getCopyMzVector();
            Set<Integer> uniqueMz = new HashSet<>();
            for (int m : mz) {
                uniqueMz.add(m);
            }

            Assert.assertEquals(String.format("Failed to filter spectrum %d", i), uniqueMz.size(), mz.length);
        }
    }
}
