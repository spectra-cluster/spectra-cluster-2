package org.spectra.cluster.filter.rawpeaks;

import io.github.bigbio.pgatk.io.common.spectra.Spectrum;
import io.github.bigbio.pgatk.io.mgf.MgfIterableReader;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class DeisotoperFunctionTest {
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
    public void testDeisotoping() {
        DeisotoperFunction function = new DeisotoperFunction(40);

        Spectrum s = allSpectra.get(0);

        // put an arbitrary super high peak
        s.getPeakList().put(new ArrayList<>(s.getPeakList().keySet()).get(1) - 1.003355, 1000.0);

        int before = s.getPeakList().size();
        function.apply(s);
        Assert.assertTrue(s.getPeakList().size() < before);
    }
}
