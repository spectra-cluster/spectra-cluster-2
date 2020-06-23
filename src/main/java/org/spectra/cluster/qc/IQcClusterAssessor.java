package org.spectra.cluster.qc;

import io.github.bigbio.pgatk.io.properties.IPropertyStorage;
import org.spectra.cluster.model.cluster.ICluster;

/**
 * Defines an assessor that assesses the quality
 * of one cluster.
 *
 * @author jg
 */
public interface IQcClusterAssessor {
    /**
     * Assess the quality of the passed cluster potentially using additional
     * information from the property storage.
     * @param cluster The cluster to assess.
     * @param storage The property storage to use for additional information.
     * @return The assessment as a QcAssessment object.
     */
    QcAssessment assessQuality(ICluster cluster, IPropertyStorage storage);
}
