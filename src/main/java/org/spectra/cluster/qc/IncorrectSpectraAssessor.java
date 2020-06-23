package org.spectra.cluster.qc;

import lombok.Data;
import io.github.bigbio.pgatk.io.common.PgatkIOException;
import io.github.bigbio.pgatk.io.properties.IPropertyStorage;
import io.github.bigbio.pgatk.io.properties.StoredProperties;
import org.spectra.cluster.model.cluster.ICluster;

import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;

/**
 * Assess the number of incorrectly clustered spectra based
 * on stored identification data. Only clusters with >= minCusterSize
 * spectra are being assessed.
 *
 * @author jg
 */
@Data
public class IncorrectSpectraAssessor implements IQcClusteringResultAssessor, IQcClusterAssessor {
    private final int minClusterSize;

    /**
     * Simply return a QcAssessment object to indicate
     * that the assessment was not possible.
     * @return A QcAssessment object
     */
    private QcAssessment getUnassessable() {
        Map<String, String> assessmentMap = new HashMap<>(3);
        assessmentMap.put("incorrect_spectra", "0");
        assessmentMap.put("identified_spectra", "0");
        assessmentMap.put("rel_incorrect_spectra", null);

        return new QcAssessment("IncorrectSpectra", QcAssessment.SCOPE.CLUSTER,
                QcAssessment.QUALITY.NOT_ASSESSABLE, assessmentMap);
    }

    @Override
    public QcAssessment assessQuality(ICluster cluster, IPropertyStorage storage) {
        int[] assessments = assessCluster(cluster, storage);

        // make sure there are identified spectra
        if (assessments[0] < 1) {
            return getUnassessable();
        }

        double relIncorrectSpectra = (assessments[0] - assessments[1]) / (double) assessments[0];

        // set the quality
        QcAssessment.QUALITY quality = QcAssessment.QUALITY.POOR;

        if (relIncorrectSpectra <= 0.01) {
            quality = QcAssessment.QUALITY.GOOD;
        } else if (relIncorrectSpectra <= 0.05) {
            quality = QcAssessment.QUALITY.MEDIUM;
        }

        // set the assessment map
        Map<String, String> assessmentMap = new HashMap<>(3);
        assessmentMap.put("incorrect_spectra", String.valueOf(assessments[0] - assessments[1]));
        assessmentMap.put("identified_spectra", String.valueOf(assessments[0]));
        assessmentMap.put("rel_incorrect_spectra", String.valueOf(relIncorrectSpectra));

        return new QcAssessment("IncorrectSpectra", QcAssessment.SCOPE.CLUSTER,
                quality, assessmentMap);
    }

    @Override
    public QcAssessment assessResultQuality(ICluster[] clusters, IPropertyStorage propertyStorage) {
        int identifiedSpectra = 0;
        int incorrectSpectra = 0;

        for (ICluster cluster : clusters) {
            // ignore small clusters
            if (cluster.getClusteredSpectraIds().size() < minClusterSize) {
                continue;
            }

            int[] spectra_counts = assessCluster(cluster, propertyStorage);

            identifiedSpectra += spectra_counts[0];
            incorrectSpectra += spectra_counts[0] - spectra_counts[1];
        }

        // make sure there are identified spectra
        if (identifiedSpectra < 1) {
            return getUnassessable();
        }

        double relIncorrectSpectra = incorrectSpectra / (double) identifiedSpectra;

        // set the quality
        QcAssessment.QUALITY quality = QcAssessment.QUALITY.POOR;

        if (relIncorrectSpectra <= 0.01) {
            quality = QcAssessment.QUALITY.GOOD;
        } else if (relIncorrectSpectra <= 0.05) {
            quality = QcAssessment.QUALITY.MEDIUM;
        }

        // set the assessment map
        Map<String, String> assessmentMap = new HashMap<>(3);
        assessmentMap.put("incorrect_spectra", String.valueOf(incorrectSpectra));
        assessmentMap.put("identified_spectra", String.valueOf(identifiedSpectra));
        assessmentMap.put("rel_incorrect_spectra", String.valueOf(relIncorrectSpectra));

        return new QcAssessment("IncorrectSpectra", QcAssessment.SCOPE.RESULT_SET,
                quality, assessmentMap);
    }

    /**
     * Count the total number of identified spectra as well as the frequency of the
     * most commonly identified peptide.
     * @param cluster The cluster to assess.
     * @param storage The property storage to use to retrieve the spectra.
     * @return [identified spectra, number spectra identified most commonly identified peptide]
     */
    private int[] assessCluster(ICluster cluster, IPropertyStorage storage) {
        int identifiedSpectra = 0;
        Map<String, Integer> sequenceCounts = new HashMap<>(20);

        for (String specId : cluster.getClusteredSpectraIds()) {
            try{
                String sequence = storage.get(specId, StoredProperties.SEQUENCE);

                if (sequence != null) {
                    identifiedSpectra++;
                }

                if (!sequenceCounts.containsKey(sequence)) {
                    sequenceCounts.put(sequence, 1);
                } else {
                    sequenceCounts.put(sequence, sequenceCounts.get(sequence) + 1);
                }
            }catch (PgatkIOException ex){
                //
            }

        }

        if (identifiedSpectra < 1) {
            int[] result = {0, 0};
            return result;
        }

        int maxCount = sequenceCounts.values().stream().max(Comparator.naturalOrder()).get();

        int[] result = {identifiedSpectra, maxCount};

        return result;
    }
}
