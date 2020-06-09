package org.spectra.cluster.util;

import org.bigbio.pgatk.io.common.PgatkIOException;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;

public class ClusterUtils {

    /**
     * Deletes all files within the path including the path.
     *
     * If filePath points to a directory, all contents within that directory and the
     * directory itself is removed.
     *
     * Handle with care!!!
     *
     * @param filePath Path to the directory or file that should be deleted
     * @throws PgatkIOException
     */
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
