package org.spectra.cluster.filter;

import org.junit.Assert;
import org.junit.Test;
import org.spectra.cluster.io.MzSpectraReader;
import org.spectra.cluster.model.spectra.BinaryPeak;
import org.spectra.cluster.model.spectra.IBinarySpectrum;

import java.io.File;
import java.util.*;

public class HighestPeakPerBinFilterTest {
    @Test
    public void testFilter() throws Exception {
        // get the spectra
        File peakList = new File(HighestPeakPerBinFilterTest.class.getClassLoader().getResource("same_sequence_cluster.mgf").toURI());
        MzSpectraReader reader = new MzSpectraReader(peakList);
        Iterator<IBinarySpectrum> spectrumIterator = reader.readBinarySpectraIterator();
        List<IBinarySpectrum> allSpectra = new ArrayList<>();

        while (spectrumIterator.hasNext()) {
            IBinarySpectrum s = spectrumIterator.next();

            // sort the peaks
            Arrays.parallelSort(s.getPeaks(), Comparator.comparingInt(BinaryPeak::getMz));

            allSpectra.add(s);
        }

        // filter the spectra
        IFilter filter = new HighestPeakPerBinFilter();

        for (int i = 0; i < allSpectra.size(); i++) {
            IBinarySpectrum s = allSpectra.get(i);
            IBinarySpectrum filtered = filter.filter(s);

            Assert.assertTrue(filtered.getPeaks().length < s.getPeaks().length);

            // make sure there are no duplicate m/z
            int[] mz = filtered.getMzVector();
            Set<Integer> uniqueMz = new HashSet<>();
            for (int m : mz) {
                uniqueMz.add(m);
            }

            Assert.assertEquals(String.format("Failed to filter spectrum %d", i), uniqueMz.size(), mz.length);
        }
    }
}
