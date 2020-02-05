package org.spectra.cluster.util;

import org.bigbio.pgatk.io.common.PgatkIOException;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;

public class ClusterUtils {

    public static void cleanFilePersistence(File filePath) throws PgatkIOException {
        try {
            Files.walk(filePath.toPath())
                    .sorted(Comparator.reverseOrder())
                    .map(Path::toFile)
                    .forEach(File::delete);
        } catch (IOException e) {
            throw new PgatkIOException("Error deleting the persistence files  -- " + filePath);
        }
    }
}
