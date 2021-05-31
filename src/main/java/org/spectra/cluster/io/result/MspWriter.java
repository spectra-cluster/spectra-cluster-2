package org.spectra.cluster.io.result;

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
 * Writes the clustering result into an MSP file.
 *
 * @author jg
 */
@Slf4j
public class MspWriter implements IClusteringResultWriter {
    private final IConsensusSpectrumBuilder consensusSpectrumBuilder;
    private static final Pattern MOD_PATTERN = Pattern.compile("[+-][0-9.]+");

    /**
     * A simple helper function to describe a PTM in
     * the style used in MSP files.
     */
    @Data
    protected class MspMod {
        private final int position;
        private final String name;
        private final String aminoAcid;
    }

    /**
     * Initializes a new MspWriter
     *
     * @param consensusSpectrumBuilder The consensus spectrum builder to use for the
     *                                 actual spectra
     */
    public MspWriter(IConsensusSpectrumBuilder consensusSpectrumBuilder) {
        this.consensusSpectrumBuilder = consensusSpectrumBuilder;
    }

    @Override
    public void writeResult(Path resultFile, ObjectDBGreedyClusterStorage clusterStorage, IPropertyStorage spectraPropertyStorage) throws Exception {
        // ensure that the file does not exist
        if (Files.exists(resultFile))
            throw new Exception(String.format("Error: File '%s' already exists.", resultFile.toString()));

        log.info(String.format("Saving clustering result as MSP file '%s'", resultFile.toString()));

        // iterate over all clusters and convert them to strings
        while (clusterStorage.hasNext()) {
            GreedySpectralCluster cluster = (GreedySpectralCluster) clusterStorage.next();

            String clusterMspString = convertCluster(cluster, spectraPropertyStorage);

            Files.write(resultFile, clusterMspString.getBytes(), StandardOpenOption.CREATE, StandardOpenOption.APPEND);
        }
    }

    private String convertCluster(GreedySpectralCluster cluster, IPropertyStorage propertyStorage) {
        StringBuilder clusterString = new StringBuilder();

        // create the consensus spectrum
        Spectrum consensus = consensusSpectrumBuilder.createConsensusSpectrum(cluster, propertyStorage);

        // get the most common PSM
        Map<String, Long> sequenceCounts = cluster.getClusteredSpectraIds().stream()
                .map(id -> getProperty(propertyStorage, id, StoredProperties.SEQUENCE))
                .filter(Objects::nonNull)
                .collect(Collectors.groupingBy(Function.identity(), Collectors.counting()));

        // get the number of identified spectra
        long nIdentified = sequenceCounts.entrySet().stream()
                .mapToLong(Map.Entry::getValue)
                .sum();

        String maxSequence = "";
        double maxRatio = 0.0;
        if (sequenceCounts.size() < 1) {
            maxSequence = "UNIDENTIFIED";
        } else {
            maxSequence = sequenceCounts.entrySet().stream()
                .max(Map.Entry.comparingByValue())
                .map(Map.Entry::getKey).orElse(null);

            // get the max ratio
            maxRatio = sequenceCounts.get(maxSequence) / (double) nIdentified;
        }

        // create the name based on this information
        clusterString.append(String.format("Name: %s/%d\n", maxSequence, consensus.getPrecursorCharge()));
        // TODO: Handle Naa for unidentified cluster
        clusterString.append(String.format("Comment: Spec=Consensus Parent=%.4f Mods=%s Nreps=%d Naa=%d MaxRatio=%.3f\n",
                consensus.getPrecursorMZ(), getModString(maxSequence),
                cluster.getClusteredSpectraCount(), maxSequence.length(), maxRatio));
        clusterString.append(String.format("Num peaks: %d\n", consensus.getPeakList().size()));

        // add the peaks
        consensus.getPeakList().entrySet().stream()
                .sorted(Map.Entry.comparingByKey())
                .forEach(peak -> clusterString.append(String.format("%f %f\n", peak.getKey(), peak.getValue())));

        // empty line after each cluster
        clusterString.append("\n");

        return clusterString.toString();
    }

    /**
     * Creates an MSP Mod: String.
     *
     * @param sequence The sequence
     * @return The mod string
     */
    protected String getModString(String sequence) {
        List<MspMod> mods = extractModsFromSequence(sequence);

        if (mods.size() < 1)
            return "0";

        StringBuilder modString = new StringBuilder(String.valueOf(mods.size()));

        for (MspMod mod : mods) {
            modString.append(String.format("(%d,%s,%s)", mod.getPosition(), mod.getAminoAcid(), mod.getName()));
        }

        return modString.toString();
    }

    /**
     * Extract the PTMs from the peptide sequence with the PTM deltas.
     *
     * @param sequence The original sequence
     * @return A List of MspModS
     */
    protected List<MspMod> extractModsFromSequence(String sequence) {
        Matcher matcher = MOD_PATTERN.matcher(sequence);
        List<MspMod> mods = new ArrayList<>(3);
        int modOffset = 0;

        while (matcher.find()) {
            String modString = sequence.substring(matcher.start(), matcher.end());
            String position = "";

            if (matcher.start() == 0) {
                position = "c-term";
            } else if (matcher.end() == sequence.length()) {
                position = "n-term";
            } else {
                position = sequence.substring(matcher.start() - 1, matcher.start());
            }

            String modName = getModNameForDelta(Double.parseDouble(modString.substring(1)), position);

            if (matcher.start() == 0) {
                mods.add(new MspMod(matcher.start() - modOffset, modName, "["));
            } else if (matcher.end() == sequence.length()) {
                mods.add(new MspMod(matcher.start() - modOffset, modName, "]"));
            } else {
                mods.add(new MspMod(matcher.start() - modOffset, modName, sequence.substring(matcher.start() - 1, matcher.start())));
            }

            // keep track of the length of the mod strings
            modOffset += modString.length();
        }
        // create the pattern to extract the PTMs
        return mods;
    }

    /**
     * Return the modification name for the passed delta.
     *
     * @param delta The delta mass
     * @param position The position of the PTM
     * @return The mod string
     */
    private String getModNameForDelta(double delta, String position) {
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
            return ptms.get(0).getShortName();
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
}
