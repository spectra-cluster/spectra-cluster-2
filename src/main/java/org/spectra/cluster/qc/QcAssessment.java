package org.spectra.cluster.qc;

import lombok.Data;

import java.util.Map;

/**
 * Represents the result of one assessment.
 */
@Data
public class QcAssessment {
    public enum SCOPE {
        CLUSTER,
        RESULT_SET
    }

    public enum QUALITY {
        GOOD,
        MEDIUM,
        POOR,
        NOT_ASSESSABLE
    }

    private final String name;
    private final SCOPE scope;
    private final QUALITY quality;
    /** Holds the "raw" assessments as key - value pairs **/
    private final Map<String, String> assessments;
}
