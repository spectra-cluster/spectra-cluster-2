package org.spectra.cluster.io;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

/**
 * uk.ac.ebi.pride.spectracluster.spectrum.KnownProperties
 * This class has properties of spectra and clusters known well enough to be hard coded and hints about
 * how they are writtem to MGF and CGF files
 *
 * see http://www.matrixscience.com/help/data_file_help.html
 * for defined MGF Keys
 *
 * @author Johannes Griss
 */
class KnownProperties {

    // Known properties keys
    private static final String IDENTIFIED_PEPTIDE_KEY = "identifiedPeptide";
    private static final String ANNOTATION_KEY = "annotation";
    private static final String TAXONOMY_KEY = "accession";
    private static final String PROTEIN_KEY = "protein"; // database: protein
    private static final String MODIFICATION_KEY = "modification"; // database: protein
    private static final String INSTRUMENT_KEY = "instrument";
    private static final String SPECTRUM_TITLE = "custom_title";

    private static final String IDENTIFIED_PEPTIDE_MGF_KEY = "SEQ";
    private static final String ANNOTATION_MGF_KEY = "USER00";
    private static final String TAXONOMY_MGF_KEY = "TAXONOMY";
    private static final String PROTEIN_MGF_KEY = "USER02";
    private static final String MODIFICATION_MGF_KEY = "USER03";
    private static final String INSTRUMENT_MGF_KEY = "INSTRUMENT";
    private static final String SPECTRUM_MGF_TITLE = "USER04";


    private static final String UNKNOWN_MGF_KEY = "USER12";

    // ===========================
    // Known cluster Properties
    public static final String MOST_COMMON_PEPTIDE_KEY = "mostCommonPeptide";
    public static final String N_HIGHEST_PEAKS = "n_highest_peaks"; // highest peaks to use for comparison
    // future uses
    public static final String PEPTIDES_LIST_COMMA_DELIMITED = "peptidesListCommaDelimited";
    public static final String PEPTIDE_PURITY_STRING = "peptidePurityString";


    // =====================
    /**
     * this section related to tags in MGF files where
     * SEQ, USER00, USER01, USER02 .. User12 are allowed
     */
    private static final Map<String, String> INTERNAL_KEY_TO_MGF_KEY = new HashMap<>();
    private static final Map<String, String> INTERNAL_MGF_KEY_TO_KEY = new HashMap<>();

    static {
        INTERNAL_KEY_TO_MGF_KEY.put(IDENTIFIED_PEPTIDE_KEY, IDENTIFIED_PEPTIDE_MGF_KEY);
        INTERNAL_KEY_TO_MGF_KEY.put(ANNOTATION_KEY, ANNOTATION_MGF_KEY);
        INTERNAL_KEY_TO_MGF_KEY.put(TAXONOMY_KEY, TAXONOMY_MGF_KEY);
        INTERNAL_KEY_TO_MGF_KEY.put(PROTEIN_KEY, PROTEIN_MGF_KEY);
        INTERNAL_KEY_TO_MGF_KEY.put(MODIFICATION_KEY, MODIFICATION_MGF_KEY);
        INTERNAL_KEY_TO_MGF_KEY.put(INSTRUMENT_KEY, INSTRUMENT_MGF_KEY);
        INTERNAL_KEY_TO_MGF_KEY.put(SPECTRUM_TITLE, SPECTRUM_MGF_TITLE);

        INTERNAL_MGF_KEY_TO_KEY.put(IDENTIFIED_PEPTIDE_MGF_KEY, IDENTIFIED_PEPTIDE_KEY);
        INTERNAL_MGF_KEY_TO_KEY.put(ANNOTATION_MGF_KEY, ANNOTATION_KEY);
        INTERNAL_MGF_KEY_TO_KEY.put(TAXONOMY_MGF_KEY, TAXONOMY_KEY);
        INTERNAL_MGF_KEY_TO_KEY.put(PROTEIN_MGF_KEY, PROTEIN_KEY);
        INTERNAL_MGF_KEY_TO_KEY.put(MODIFICATION_MGF_KEY, MODIFICATION_KEY);
        INTERNAL_MGF_KEY_TO_KEY.put(INSTRUMENT_MGF_KEY, INSTRUMENT_KEY);
        INTERNAL_MGF_KEY_TO_KEY.put(SPECTRUM_MGF_TITLE, SPECTRUM_TITLE);

    }

    private static final Map<String, String> KEY_TO_MGF_KEY = Collections.unmodifiableMap(INTERNAL_KEY_TO_MGF_KEY);
    private static final Map<String, String> MGF_KEY_TO_KEY = Collections.unmodifiableMap(INTERNAL_MGF_KEY_TO_KEY);

    /**
     * take property - value pair to a line to insert in MGF
     * USER12 is any unknown
     *
     * @param property Property name
     * @param value The property's value
     * @return The encoded property / value pair based on the names of known properties.
     */
    public static String toMGFLine(String property, String value) {
        String key = KEY_TO_MGF_KEY.get(property);
        if (key == null) {
            return UNKNOWN_MGF_KEY + "=" + property + "=" + value;
        } else {
            return key + "=" + value;
        }
    }

    /**
     * parse an mgf line like SEQ or USER00..USER12
     * @param props  properties to add
     * @param line The MGF line to test.
     * @return   true if successfully handled
     */
    public static boolean addMGFProperties(Properties props,String line) {
        if(line.contains("="))  {
            int index = line.indexOf("=") ;
            String key = line.substring(0,index);
            String value = line.substring(index + 1,line.length());
            return handleKnownProperty(props,key, value);
        }
        else {
            return false;
        }
    }

    private static boolean handleUnknownProperty(Properties props,String key1, String key2, String value) {
        if (!UNKNOWN_MGF_KEY.equals(key1))
            return false;
        props.setProperty(key2,value);
        return true;
    }

    private static boolean handleKnownProperty(Properties props,String key, String value) {
        String realKey =  MGF_KEY_TO_KEY.get(key);
        if (realKey == null)
            return false;
        props.setProperty(realKey,value);
        return true;
    }
}