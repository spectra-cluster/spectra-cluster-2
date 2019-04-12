package org.spectra.cluster.qc;

import org.bigbio.pgatk.io.properties.IPropertyStorage;
import org.spectra.cluster.model.cluster.ICluster;

/**
 * Assesses the quality of a complete clustering result.
 */
public interface IQcClusteringResultAssessor {
    /**
     * Assess the quality of a complete clustering result.
     * @param clusters The clusters that constitute the complete result set.
     * @param propertyStorage The property storage to use for additional information.
     * @return The result as a QcAssessment.
     */
    QcAssessment assessResultQuality(ICluster[] clusters, IPropertyStorage propertyStorage);
}
