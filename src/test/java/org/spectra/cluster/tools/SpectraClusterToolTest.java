package org.spectra.cluster.tools;

import org.junit.Ignore;
import org.junit.Test;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class SpectraClusterToolTest {
    @Ignore
    @Test
    public void testLocalBenchmark() throws Exception {
        Path resultFile = Paths.get("/tmp/previous-clusters.zcl");
        String mgfFile = new File(SpectraClusterToolTest.class
                .getClassLoader().getResource("same_sequence_cluster.mgf").toURI()).getAbsolutePath();


        if (Files.exists(resultFile))
            Files.delete(resultFile);

        // create the args
        String[] args = {
              "-o", resultFile.toAbsolutePath().toString(),
              "-p", "1", // precursor tolerance
              "-f", "low", // fragment tolerance
              "-mc", "0", // minimum comparisons (auto)
              "-s", "1", // start threshold
              "-e", "0.99", // end-threshold
              "-r", "5", // rounds
              mgfFile
        };

        // start the clustering
        SpectraClusterTool.main(args);
    }


    @Ignore
    @Test
    public void testLocalUnsingClusterBenchmark() throws Exception {
        Path resultFile = Paths.get("/tmp/previous-clusters.zcl");
        String mgfFile = new File(SpectraClusterToolTest.class
                .getClassLoader().getResource("same_sequence_cluster.mgf").toURI()).getAbsolutePath();

        String clusterFile = new File(SpectraClusterToolTest.class
                .getClassLoader().getResource("previous-clusters.zcl").toURI()).getAbsolutePath();

        if (Files.exists(resultFile))
            Files.delete(resultFile);

        // create the args
        String[] args = {
                "-o", resultFile.toAbsolutePath().toString(),
                "-p", "1", // precursor tolerance
                "-f", "low", // fragment tolerance
                "-mc", "0", // minimum comparisons (auto)
                "-s", "1", // start threshold
                "-e", "0.99", // end-threshold
                "-r", "5", // rounds
                mgfFile, clusterFile
        };

        // start the clustering
        SpectraClusterTool.main(args);
    }
}
