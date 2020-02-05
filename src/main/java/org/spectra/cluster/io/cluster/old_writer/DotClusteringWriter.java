package org.spectra.cluster.io.cluster.old_writer;

import lombok.extern.slf4j.Slf4j;
import org.bigbio.pgatk.io.common.PgatkIOException;
import org.bigbio.pgatk.io.properties.IPropertyStorage;
import org.bigbio.pgatk.io.properties.StoredProperties;
import org.spectra.cluster.model.cluster.ICluster;
import org.spectra.cluster.model.consensus.BinaryConsensusPeak;
import org.spectra.cluster.model.spectra.IBinarySpectrum;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.OpenOption;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * This writer writes clusters to the .clustering format as defined in the original spectra-cluster project.
 * This class is deprecated for now and we will work on it in the future.
 *
 * @author ypriverol
 * @author jgriss
 */

@Slf4j
@Deprecated
public class DotClusteringWriter implements IClusterWriter {
    private final boolean append;
    private final BufferedWriter writer;
    private final IPropertyStorage propertyStorage;

    /**
     * Creates a new DotClusteringWriter instance.
     * @param outputPath The file to create / write to
     * @param append If set to false, the existing file will be overwritten.
     * @param propertyStorage The property storage to use to fetch additional spectrum properties.
     * @throws IOException In case an existing file cannot be overwritten or
     *                     writing the header fails.
     */
    public DotClusteringWriter(Path outputPath, boolean append, IPropertyStorage propertyStorage) throws IOException {
        this.append = append;
        this.propertyStorage = propertyStorage;

        // open / create the file
        OpenOption createOption = this.append ? StandardOpenOption.APPEND : StandardOpenOption.CREATE;
        writer = Files.newBufferedWriter(outputPath, createOption);

        // write the header field only as a test
        if (!this.append) {
            writer.write("algorithm=spectra-cluster v2\n");
        }
    }

    @Override
    public void appendClusters(ICluster... clusters) throws IOException {
        for (ICluster cluster : clusters) {
            // write everything to a string builder first
            StringBuilder stringBuilder = new StringBuilder("=Cluster=\n");

            stringBuilder.append("id=").append(cluster.getId()).append('\n');
            // TODO: convert int precursor m/z back to actual precursor m/z
            stringBuilder.append("av_precursor_mz=").append(String.valueOf(cluster.getPrecursorMz())).append('\n');
            // precursor intensities are not really supported
            stringBuilder.append("av_precursor_intensity=1\n");

            addSequenceList(cluster.getClusteredSpectraIds(), stringBuilder);

            addConsensusPeaks(cluster, stringBuilder);

            // put the spectra
            for (String spectrumId : cluster.getClusteredSpectraIds()) {
                addSpectrum(spectrumId, stringBuilder);
            }

            writer.write(stringBuilder.toString());
        }
    }

    /**
     * Add the list of identified sequences to the cluster.
     * @param clusteredSpectraIds The ids of the spectra to process.
     * @param stringBuilder The string builder to put the line to.
     */
    private void addSequenceList(Set<String> clusteredSpectraIds, StringBuilder stringBuilder) {
        String[] sequences = clusteredSpectraIds.stream().map(id -> {
            try{
                String s = propertyStorage.get(id, StoredProperties.SEQUENCE);
                if (s == null || s.length() < 1) {
                    s = "UNIDENTIFIED";
                }
                return s;
            }catch (PgatkIOException ex){
                // Todo error
            }
            return null;
        }).toArray(String[]::new);

        Map<String, Integer> sequenceCounts = new HashMap<>(sequences.length);

        for (String s : sequences) {
            if (!sequenceCounts.containsKey(s)) {
                sequenceCounts.put(s, 1);
            } else {
                sequenceCounts.put(s, sequenceCounts.get(s) + 1);
            }
        }

        // create the string
        stringBuilder.append("sequence=[");
        boolean isFirst = true;
        for (Map.Entry<String, Integer> stringCount : sequenceCounts.entrySet()) {
            if (!isFirst) {
                stringBuilder.append(',');
            }
            stringBuilder.append(stringCount.getKey()).append(':').append(String.valueOf(stringCount.getValue()));
            isFirst = false;
        }
        stringBuilder.append("]\n");
    }

    /**
     * Adds information about the spectrum to the output file. The actual
     * data will be retrieved from the property storage.
     * @param spectrumId The spectrum's id
     * @param stringBuilder The StringBuilder to put the information to.
     */
    private void addSpectrum(String spectrumId, StringBuilder stringBuilder) {
        try{
            String filename = propertyStorage.get(spectrumId, StoredProperties.ORG_FILENAME);
            String fileId = propertyStorage.get(spectrumId, StoredProperties.FILE_INDEX);
            String title = propertyStorage.get(spectrumId, StoredProperties.TITLE);
            String sequence = propertyStorage.get(spectrumId, StoredProperties.SEQUENCE);
            String precursorMz = propertyStorage.get(spectrumId, StoredProperties.PRECURSOR_MZ);
            String charge = propertyStorage.get(spectrumId, StoredProperties.CHARGE);
            String ptms = propertyStorage.get(spectrumId, StoredProperties.PTMS);
            String retentionTime = propertyStorage.get(spectrumId, StoredProperties.RETENTION_TIME);
            stringBuilder
                    .append("SPEC\t")
                    .append("#file=").append(filename != null ? filename : "")
                    .append("#id=").append(fileId != null ? fileId : "")
                    .append("#title=").append(title != null ? title : "")
                    .append('\t')
                    // no longer supported - most common peptide
                    .append("false").append('\t')
                    .append(sequence != null ? sequence : "").append('\t')
                    .append(precursorMz != null ? precursorMz : "").append('\t')
                    .append(charge != null ? charge : "").append('\t')
                    // species is not supported
                    .append('\t')
                    .append(ptms != null ? ptms : "").append('\t')
                    // score currently not supported
                    .append("0\t")
                    // JSON encoded stuff
                    .append('{');

            boolean isFirst = true;
            if (retentionTime != null) {
                stringBuilder.append("\"RT\": ").append(retentionTime);
                isFirst = false;
            }

            // TODO: put other JSON propertes - ie is_decoy etc.

            stringBuilder.append("}\n");

        }catch (PgatkIOException ex){
            //Todo error
        }



    }

    /**
     * Adds the lines representing the consensus spectrum to the passed StringBuilder instance.
     * @param cluster The cluster who's consensus spectrum to put.
     * @param stringBuilder The StringBuilder to which the lines are added.
     */
    private void addConsensusPeaks(ICluster cluster, StringBuilder stringBuilder) {
        IBinarySpectrum spectrum = cluster.getConsensusSpectrum();
        BinaryConsensusPeak[] peaks = Arrays.stream(spectrum.getPeaks()).map(p -> (BinaryConsensusPeak) p).toArray(BinaryConsensusPeak[]::new);

        stringBuilder.append("consensus_peak_counts=");
        boolean isFirst = true;
        for (BinaryConsensusPeak p : peaks) {
            if (!isFirst) {
                stringBuilder.append(',');
            } else {
                isFirst = false;
            }

            stringBuilder.append(String.valueOf(p.getCount()));
        }
        stringBuilder.append('\n');

        stringBuilder.append("consensus_mz=");
        isFirst = true;
        for (BinaryConsensusPeak p : peaks) {
            if (!isFirst) {
                stringBuilder.append(',');
            } else {
                isFirst = false;
            }

            // TODO: Convert to actual decimal number
            stringBuilder.append(String.valueOf(p.getMz()));
        }
        stringBuilder.append('\n');

        stringBuilder.append("consensus_intens=");
        isFirst = true;
        for (BinaryConsensusPeak p : peaks) {
            if (!isFirst) {
                stringBuilder.append(',');
            } else {
                isFirst = false;
            }

            // TODO: Convert to actual decimal number
            stringBuilder.append(String.valueOf(p.getIntensity()));
        }
        stringBuilder.append('\n');
    }

    @Override
    public void close() throws IOException {
        writer.close();
    }
}
