package org.spectra.cluster.qc;

import org.junit.Assert;
import org.junit.Test;

public class ClusteredSpectraAssessorTest extends AbstractAssessorTest {

    @Test
    public void testAssessment() {
        IQcClusteringResultAssessor assessor = new ClusteredSpectraAssessor(1);
        QcAssessment assessment = assessor.assessResultQuality(clusters, properties);

        Assert.assertEquals(QcAssessment.SCOPE.RESULT_SET, assessment.getScope());
        Assert.assertEquals(QcAssessment.QUALITY.GOOD, assessment.getQuality());
        Assert.assertEquals(assessment.getAssessments().get("total_spectra"), assessment.getAssessments().get("clustered_spectra"));
        Assert.assertEquals(1.0, Double.parseDouble(assessment.getAssessments().get("rel_clustered_spectra")), 0.0001);

        assessor = new ClusteredSpectraAssessor(3);
        assessment = assessor.assessResultQuality(clusters, properties);

        Assert.assertEquals(QcAssessment.SCOPE.RESULT_SET, assessment.getScope());
        Assert.assertEquals(QcAssessment.QUALITY.GOOD, assessment.getQuality());
        Assert.assertEquals("794", assessment.getAssessments().get("total_spectra"));
        Assert.assertEquals("730", assessment.getAssessments().get("clustered_spectra"));
        Assert.assertEquals(0.9194, Double.parseDouble(assessment.getAssessments().get("rel_clustered_spectra")), 0.0001);
    }
}
