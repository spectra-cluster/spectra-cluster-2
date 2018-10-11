package org.spectra.cluster.filter.rawpeaks;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.spectra.cluster.normalizer.TideBinnerTest;
import uk.ac.ebi.pride.tools.jmzreader.model.Spectrum;
import uk.ac.ebi.pride.tools.mgf_parser.MgfFile;

import java.io.File;
import java.net.URI;
import java.util.*;

public class RemoveImpossiblyHighPeaksFunctionTest {
    private List<Spectrum> allSpectra = new ArrayList<>(100);

    @Before
    public void setUp() throws Exception {
        URI uri = Objects.requireNonNull(TideBinnerTest.class.getClassLoader().getResource("single-spectra.mgf")).toURI();
        MgfFile mgfFile = new MgfFile(new File(uri));
        Iterator<Spectrum> specIt = mgfFile.getSpectrumIterator();

        while (specIt.hasNext()) {
            allSpectra.add(specIt.next());
        }
    }

    @Test
    public void testRemoveHighPeaks() {
        RemoveImpossiblyHighPeaksFunction function = new RemoveImpossiblyHighPeaksFunction();

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
                        Assert.assertTrue(mzBefore  > (s.getPrecursorMZ() * s.getPrecursorCharge() + 50));
                    }
                }
            }
        }

        Assert.assertEquals(2, nFiltered);
    }
}
