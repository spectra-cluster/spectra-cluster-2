package org.spectra.cluster.io.result;

import io.github.bigbio.pgatk.io.common.CvParam;
import io.github.bigbio.pgatk.io.common.spectra.Spectrum;
import io.github.bigbio.pgatk.io.properties.IPropertyStorage;
import io.github.bigbio.pgatk.io.properties.StoredProperties;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.spectra.cluster.consensus.IConsensusSpectrumBuilder;
import org.spectra.cluster.io.cluster.ObjectDBGreedyClusterStorage;
import org.spectra.cluster.model.cluster.GreedySpectralCluster;
import uk.ac.ebi.pride.utilities.pridemod.ModReader;
import uk.ac.ebi.pride.utilities.pridemod.model.PTM;
import uk.ac.ebi.pride.utilities.pridemod.model.UniModPTM;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.*;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * Writes the clustering result into a .clustering file.
 *
 * @author jg
 */
@Slf4j
public class DotClusteringWriter implements IClusteringResultWriter {
    private final IConsensusSpectrumBuilder consensusSpectrumBuilder;
    private static final Pattern MOD_PATTERN = Pattern.compile("[+-][0-9.]+");

    /**
     * A simple helper function to describe a PTM in
     * the style used in .clustering files.
     */
    @Data
    protected class ClusteringMod {
        private final int position;
        private final String accession;
    }

    @Data
    /**
     * A simple class to capture sequence frequencies
     * within a cluster.
     */
    private class SequenceCounts {
        private final String sequence;
        private final int count;
    }

    /**
     * Initializes a new DotClusteringWriter
     *
     * @param consensusSpectrumBuilder The consensus spectrum builder to use for the
     *                                 actual spectra
     */
    public DotClusteringWriter(IConsensusSpectrumBuilder consensusSpectrumBuilder) {
        this.consensusSpectrumBuilder = consensusSpectrumBuilder;
    }

    @Override
    public void writeResult(Path resultFile, ObjectDBGreedyClusterStorage clusterStorage, IPropertyStorage spectraPropertyStorage) throws Exception {
        // ensure that the file does not exist
        if (Files.exists(resultFile))
            throw new Exception(String.format("Error: File '%s' already exists.", resultFile.toString()));

        log.info(String.format("Saving clustering result as .clustering file '%s'", resultFile.toString()));

        // iterate over all clusters and convert them to strings
        while (clusterStorage.hasNext()) {
            GreedySpectralCluster cluster = (GreedySpectralCluster) clusterStorage.next();

            String clusterDotClusteringstring = convertCluster(cluster, spectraPropertyStorage);

            Files.write(resultFile, clusterDotClusteringstring.getBytes(), StandardOpenOption.CREATE, StandardOpenOption.APPEND);
        }
    }

    /**
     * Converts the passed clustering to its string representation in the
     * .clustering format
     * @param cluster
     * @param propertyStorage
     * @return
     */
    private String convertCluster(GreedySpectralCluster cluster, IPropertyStorage propertyStorage) {
        // create the consensus spectrum based on the original data to
        // avoid introducing any changes through our binning and integer
        // mapping approach
        Spectrum consensus = consensusSpectrumBuilder.createConsensusSpectrum(cluster, propertyStorage);

        // start creating the string representation
        StringBuilder clusterString = new StringBuilder("=Cluster=\n");

        // add the cluster id and general properties
        clusterString.append(String.format("id=%s\n", cluster.getId()));
        clusterString.append(String.format("av_precursor_mz=%f\n", consensus.getPrecursorMZ()));
        clusterString.append(String.format("av_precursor_intens=%f\n", consensus.getPrecursorIntensity()));
        clusterString.append(String.format("sequence=[%s]\n", createSequenceCountString(cluster, propertyStorage)));

        // add the consensus spectrum information
        String mzString = consensus.getPeakList().entrySet().stream()
                .sorted(Comparator.comparingDouble(Map.Entry::getKey))
                .map(Map.Entry::getKey)
                .map(String::valueOf)
                .collect(Collectors.joining(","));
        String intensityString = consensus.getPeakList().entrySet().stream()
                .sorted(Comparator.comparingDouble(Map.Entry::getKey))
                .map(Map.Entry::getValue)
                .map(String::valueOf)
                .collect(Collectors.joining(","));

        clusterString.append("consensus_mz=").append(mzString).append("\n");
        clusterString.append("consensus_intens=").append(intensityString).append("\n");

        // get the count as the additional parameter
        CvParam countParam = ((List<CvParam>) consensus.getAdditional()).get(0);
        if (countParam.getAccession().equals("CV:0001")) {
            clusterString.append("consensus_peak_counts=").append(countParam.getValue()).append("\n");
        }

        // add the spectrum entries
        String mostCommonSequence = cluster.getClusteredSpectraIds().stream()
                .map(id -> getProperty(propertyStorage, id, StoredProperties.SEQUENCE))
                .filter(Objects::nonNull)
                .map(DotClusteringWriter::getCleanSequence)
                .collect(Collectors.groupingBy(Function.identity(), Collectors.counting()))
                .entrySet()
                .stream()
                .max(Comparator.comparingLong(Map.Entry::getValue))
                .map(Map.Entry::getKey).orElse(null);

        clusterString.append(createSpectrumEntries(cluster, propertyStorage, mostCommonSequence));

        return clusterString.toString();
    }

    /**
     * Create the lines representing the spectra in the cluster.
     * @param cluster
     * @param propertyStorage
     * @return
     */
    private String createSpectrumEntries(GreedySpectralCluster cluster, IPropertyStorage propertyStorage, String mostCommonSequence) {
        StringBuilder spectrumEntries = new StringBuilder();

        for (String specId : cluster.getClusteredSpectraIds()) {
            spectrumEntries.append("SPEC\t");

            // add the spectrum's id
            String idLine = String.format("#file=%s#id=%s#title=%s\t",
                    getProperty(propertyStorage, specId, StoredProperties.ORG_FILENAME),
                    getProperty(propertyStorage, specId, StoredProperties.FILE_INDEX),
                    getProperty(propertyStorage, specId, StoredProperties.FILE_SCAN)
            );

            spectrumEntries.append(idLine);

            // add if it's the most common sequence
            String sequence = getProperty(propertyStorage, specId, StoredProperties.SEQUENCE);
            String cleanSequence = getCleanSequence(sequence);

            if (mostCommonSequence != null && mostCommonSequence.equals(cleanSequence))
                spectrumEntries.append("TRUE\t");
            else
                spectrumEntries.append("FALSE\t");

            if (cleanSequence != null)
                spectrumEntries.append(cleanSequence).append("\t");
            else
                spectrumEntries.append("UNIDENTIFIED\t");

            spectrumEntries.append(getProperty(propertyStorage, specId, StoredProperties.PRECURSOR_MZ)).append("\t");
            spectrumEntries.append(getProperty(propertyStorage, specId, StoredProperties.CHARGE)).append("\t");

            // species is unknown
            spectrumEntries.append("NA\t");

            // add the mods
            String modString = extractModsFromSequence(sequence).stream()
                    .map(mod -> String.format("%d-%s", mod.getPosition(), mod.getAccession()))
                    .collect(Collectors.joining(","));
            spectrumEntries.append(modString).append("\t");

            // score is unknown
            spectrumEntries.append("\n");
        }

        return spectrumEntries.toString();
    }

    /**
     * Create a string representation of the sequence counts in reverse order (starting with
     * the most frequent sequence) in the format "SEQUENCE:COUNT,..."
     *
     * @param cluster The cluster to create the sequence counts for
     * @param propertyStorage The property storage
     * @return The string representation of the sequence counts
     */
    private String createSequenceCountString(GreedySpectralCluster cluster, IPropertyStorage propertyStorage) {
        // get the sequence counts
        String sequenceCounts = cluster.getClusteredSpectraIds().stream()
                .map(id -> getProperty(propertyStorage, id, StoredProperties.SEQUENCE))
                .filter(Objects::nonNull)
                .map(DotClusteringWriter::getCleanSequence)
                .collect(Collectors.groupingBy(Function.identity(), Collectors.counting()))
                .entrySet()
                .stream()
                .map(entry -> new SequenceCounts(entry.getKey(), entry.getValue().intValue()))
                .sorted(Comparator.comparingInt(SequenceCounts::getCount))
                .map(sequencCount -> String.format("%s:%d", sequencCount.getSequence(), sequencCount.getCount()))
                .collect(Collectors.joining(","));

        return sequenceCounts;
    }

    /**
     * Extract the PTMs from the peptide sequence with the PTM deltas.
     *
     * @param sequence The original sequence
     * @return A List of MspModS
     */
    protected List<ClusteringMod> extractModsFromSequence(String sequence) {
        if (sequence == null)
            return Collections.emptyList();

        Matcher matcher = MOD_PATTERN.matcher(sequence);
        List<ClusteringMod> mods = new ArrayList<>(3);
        int modOffset = 0;

        while (matcher.find()) {
            String modString = sequence.substring(matcher.start(), matcher.end());
            String acc = getModAccForDelta(Double.parseDouble(modString.substring(1)));

            mods.add(new ClusteringMod(modOffset, acc));

            // keep track of the length of the mod strings
            modOffset += modString.length();
        }
        // create the pattern to extract the PTMs
        return mods;
    }

    /**
     * Return the modification accession for the passed delta.
     *
     * @param delta The delta mass
     * @return The mod string
     */
    private String getModAccForDelta(double delta) {
        ModReader modReader = ModReader.getInstance();
        List<PTM> ptms = modReader.getAnchorMassModification(delta, 0.001, "");

        // only use UNIMOD
        ptms = ptms.stream()
                .filter(p -> p instanceof UniModPTM)
                .sorted(Comparator.comparing(PTM::getAccession))
                .collect(Collectors.toList());

        if (ptms.size() < 1) {
            return String.valueOf(delta);
        } else {
            // simply return the first
            return ptms.get(0).getAccession();
        }
    }

    /**
     * Retrieves a property from the property storage but converts any
     * exception to an IllegalStateExcpetion
     *
     * @param storage
     * @param id
     * @param property
     * @return
     */
    private String getProperty(IPropertyStorage storage, String id, String property) {
        try {
            return storage.get(id, property);
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
    }

    /**
     * Returns the peptide sequence without any potentially annotated PTMs
     * @param sequence
     * @return
     */
    public static String getCleanSequence(String sequence) {
        if (sequence == null)
            return null;

        return sequence.replaceAll("[^A-Z]", "");
    }
}
