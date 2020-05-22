package org.spectra.cluster.qc;

import org.junit.Assert;
import org.junit.Test;

public class IncorrectSpectraAssessorTest extends AbstractAssessorTest {

    @Test
    public void testAssessment() {
        IQcClusteringResultAssessor assessor = new IncorrectSpectraAssessor(1);
        QcAssessment assessment = assessor.assessResultQuality(clusters, properties);

        Assert.assertEquals(QcAssessment.SCOPE.RESULT_SET, assessment.getScope());
        Assert.assertEquals(QcAssessment.QUALITY.GOOD, assessment.getQuality());
        Assert.assertEquals("794", assessment.getAssessments().get("identified_spectra"));
        Assert.assertEquals("0", assessment.getAssessments().get("incorrect_spectra"));
        Assert.assertEquals(0, Double.parseDouble(assessment.getAssessments().get("rel_incorrect_spectra")), 0.0001);

        assessor = new IncorrectSpectraAssessor(3);
        assessment = assessor.assessResultQuality(clusters, properties);

        Assert.assertEquals(QcAssessment.SCOPE.RESULT_SET, assessment.getScope());
        Assert.assertEquals(QcAssessment.QUALITY.GOOD, assessment.getQuality());
        Assert.assertEquals("730", assessment.getAssessments().get("identified_spectra"));
        Assert.assertEquals("0", assessment.getAssessments().get("incorrect_spectra"));
        Assert.assertEquals(0, Double.parseDouble(assessment.getAssessments().get("rel_incorrect_spectra")), 0.0001);
    }
}
