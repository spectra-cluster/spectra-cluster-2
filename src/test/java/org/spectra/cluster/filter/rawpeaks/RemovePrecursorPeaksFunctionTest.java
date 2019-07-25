package org.spectra.cluster.filter.rawpeaks;

import org.bigbio.pgatk.io.common.spectra.Spectrum;
import org.bigbio.pgatk.io.mgf.MgfIterableReader;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.net.URI;
import java.util.*;

public class RemovePrecursorPeaksFunctionTest {
    private List<Spectrum> allSpectra = new ArrayList<>(100);

    @Before
    public void setUp() throws Exception {
        URI uri = Objects.requireNonNull(getClass().getClassLoader().getResource("single-spectra.mgf")).toURI();
        MgfIterableReader mgfFile = new MgfIterableReader(new File(uri), true, false, true);

        while (mgfFile.hasNext()) {
            allSpectra.add(mgfFile.next());
        }
    }

    @Test
    public void testRemovePrecursorPeaks() {
        RemovePrecursorPeaksFunction function = new RemovePrecursorPeaksFunction(0.5);

        int nFiltered = 0;
        for (Spectrum s : allSpectra) {
            int before = s.getPeakList().size();
            Map<Double, Double> peaksBefore = new HashMap<>(s.getPeakList());
            function.apply(s);
            int after = s.getPeakList().size();

            Assert.assertTrue(after <= before);

            if (after < before) {
                nFiltered++;

                for (Double mzBefore : peaksBefore.keySet()) {
                    if (!s.getPeakList().containsKey(mzBefore)) {
                        // rough estimate of maxPeak
                        System.out.printf("Removed %.2f from spectrum %.2f @ %d\n", mzBefore, s.getPrecursorMZ(), s.getPrecursorCharge());
                    }
                }
            }
        }

        Assert.assertEquals(2, nFiltered);
    }
}
