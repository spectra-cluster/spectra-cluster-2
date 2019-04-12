package org.spectra.cluster.filter.rawpeaks;

import java.util.*;

/**
 * This code is licensed under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 *
 * Remove all the Ion reporters
 *
 * @author ypriverol on 21/08/2018.
 */
public class RemoveReporterIonPeaksFunction implements IRawPeakFunction{

    private final double fragmentIonTolerance;
    private final static double FRAGMENT_ION_TOLERANCE = 0.5;
    private final REPORTER_TYPE reporterType;

    public enum REPORTER_TYPE {
        ITRAQ,
        TMT,
        ALL
    }

    public RemoveReporterIonPeaksFunction(double fragmentIonTolerance, REPORTER_TYPE reporterType) {
        this.fragmentIonTolerance = fragmentIonTolerance;
        this.reporterType = reporterType;
    }

    /**
     * By default all reporter ions within the defined fragment
     * tolerance are removed.
     */
    public RemoveReporterIonPeaksFunction() {
        this(FRAGMENT_ION_TOLERANCE, REPORTER_TYPE.ALL);
    }


    @Override
    public Map<Double, Double> apply(Map<Double, Double> peaks) {
        // get the m/z values
        Double[] reporterMzValues = getReporterMz(reporterType);
        Map<Double, Double> filteredPeaks = new HashMap<>(peaks.size());

        for(Map.Entry peak: peaks.entrySet()){
            double peakMz = (double) peak.getKey();
            // ignore any peak that could be a neutral loss
            boolean isReporterPeak = false;
            for (double reporterMz : reporterMzValues) {
                if (isWithinRange(reporterMz - fragmentIonTolerance, reporterMz + fragmentIonTolerance, peakMz)) {
                    isReporterPeak = true;
                    break;
                }
            }
            if (!isReporterPeak) {
                filteredPeaks.entrySet().add(peak);
            }
        }
        return filteredPeaks;
    }

    /**
     * Returns the m/z values for the reporter ions relevant
     * to the passed reporterType.
     * @param reporterType Type of ION Reporter to Remove @{@link REPORTER_TYPE}
     * @return List of Double mz
     */
    private Double[] getReporterMz(REPORTER_TYPE reporterType) {
        switch (reporterType) {
            case ITRAQ:
                // 305.1 for iTRAQ 9 was explicitly not added yet
                return new Double[] {113.1, 114.1, 115.1, 116.1, 117.1, 118.1, 119.1, 121.1};
            case TMT:
                // 230.1697 represents the complete TMT tag
                return new Double[] {126.127725, 127.12476, 127.131079, 128.128114, 128.134433, 129.131468,
                        129.137787, 130.134822, 130.141141, 131.138176, 230.1697};
            case ALL:
            default:
                // merge all known reporters
                List<Double> reporterMz = new ArrayList<>(REPORTER_TYPE.values().length);

                for (REPORTER_TYPE rt : REPORTER_TYPE.values()) {
                    if (rt == REPORTER_TYPE.ALL)
                        continue;
                    Double[] curRtsMz = getReporterMz(rt);
                    Collections.addAll(reporterMz, curRtsMz);
                }

                // change to float[]
                Double[] returnVal = new Double[reporterMz.size()];
                for (int i = 0; i < reporterMz.size(); i++)
                    returnVal[i] = reporterMz.get(i);

                return returnVal;
        }
    }

    private boolean isWithinRange(double min, double max, double value) {
        return (value >= min && value <= max);
    }
}
