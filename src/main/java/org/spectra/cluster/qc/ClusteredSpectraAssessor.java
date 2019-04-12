package org.spectra.cluster.qc;

import lombok.Data;
import org.bigbio.pgatk.io.properties.IPropertyStorage;
import org.spectra.cluster.model.cluster.ICluster;

import java.util.HashMap;
import java.util.Map;

/**
 * Assess the total number of spectra that are found in clusters with >=
 * minClusterSize spectra.
 */
@Data
public class ClusteredSpectraAssessor implements IQcClusteringResultAssessor {
    private final int minClusterSize;

    @Override
    public QcAssessment assessResultQuality(ICluster[] clusters, IPropertyStorage propertyStorage) {
        int nTotalSpectra = 0;
        int clusteredSpectra = 0;

        for (ICluster c : clusters) {
            int size = c.getClusteredSpectraIds().size();

            nTotalSpectra += size;

            if (size >= minClusterSize) {
                clusteredSpectra += size;
            }
        }

        double relClusteredSpectra = clusteredSpectra / (double) nTotalSpectra;

        // define the quality
        QcAssessment.QUALITY quality;

        if (relClusteredSpectra >= 0.6) {
            quality = QcAssessment.QUALITY.GOOD;
        } else if (relClusteredSpectra >= 0.3) {
            quality = QcAssessment.QUALITY.MEDIUM;
        } else {
            quality = QcAssessment.QUALITY.POOR;
        }

        // save all assessments
        Map<String, String> assessments = new HashMap<>(3);
        assessments.put("clustered_spectra", String.valueOf(clusteredSpectra));
        assessments.put("total_spectra", String.valueOf(nTotalSpectra));
        assessments.put("rel_clustered_spectra", String.valueOf(relClusteredSpectra));

        return new QcAssessment(
                "ClusteredSpectra", QcAssessment.SCOPE.RESULT_SET, quality, assessments);
    }
}
