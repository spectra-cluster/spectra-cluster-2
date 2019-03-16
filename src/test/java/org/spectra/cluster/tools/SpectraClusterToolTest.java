package org.spectra.cluster.tools;

import org.junit.Ignore;
import org.junit.Test;

public class SpectraClusterToolTest {
    @Ignore
    @Test
    public void testLocalBenchmark() throws Exception {
        // create the args
        String[] args = {
              "-o", "/tmp/test.clustering",
              "-p", "1", // precursor tolerance
              "-f", "0.5", // fragment tolerance
              "-mc", "0", // minimum comparisons (auto)
              "-s", "1", // start threshold
              "-e", "0.99", // end-threshold
              "-r", "5", // rounds
              "/home/jg/Projects/Testfiles/melanoma_heterogeneity/Melanom_metastasen_Griss_P-B_1_151120061620.fdr01.msgf.mgf",
                "/home/jg/Projects/Testfiles/melanoma_heterogeneity/Melanom_metastasen_Griss_P-B_2_151120164543.fdr01.msgf.mgf",
                "/home/jg/Projects/Testfiles/melanoma_heterogeneity/Melanom_metastasen_Griss_P-B_3_151121034515.fdr01.msgf.mgf",
                "/home/jg/Projects/Testfiles/melanoma_heterogeneity/Melanom_metastasen_Griss_P-B_4_151121141440.fdr01.msgf.mgf"
        };

        // start the clustering
        SpectraClusterTool.main(args);
    }
}
