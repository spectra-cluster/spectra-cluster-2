package org.spectra.cluster.utils.io;


import org.spectra.cluster.model.commons.Tuple;
import org.spectra.cluster.model.spectra.ISpectrum;
import org.spectra.cluster.model.spectra.Spectrum;

import java.io.*;
import java.util.HashSet;
import java.util.Properties;
import java.util.Set;

/**
 *
 * @author Johannes Griss
 *
 */
public class ParserUtilities {

    private static final String BEGIN_IONS = "BEGIN IONS";
    private static final String END_IONS = "END IONS";
    private static final String BEGIN_CLUSTER = "BEGIN CLUSTER";

    /**
     * take a line like BEGIN CLUSTER Charge=2 Id=VVXVXVVX  return charge
     *
     * @param line The line to parse
     * @return The extracted charge state as integer
     */
    protected static int chargeFromClusterLine(String line) {
        line = line.replace(BEGIN_CLUSTER, "").trim();
        String[] split = line.split(" ");
        //noinspection ForLoopReplaceableByForEach
        for (int i = 0; i < split.length; i++) {
            String s = split[i];
            if (s.startsWith("Charge=")) {
                return (int) (0.5 + Double.parseDouble(s.substring("Charge=".length())));
            }
        }
        throw new IllegalArgumentException("no Charge= part in " + line);
    }

    protected static boolean storesPeakListFromClusterLine(String line) {
        line = line.replace(BEGIN_CLUSTER, "").trim();
        String[] split = line.split(" ");
        //noinspection ForLoopReplaceableByForEach
        for (int i = 0; i < split.length; i++) {
            String s = split[i];
            if (s.startsWith("ContainsPeaklist=")) {
                return Boolean.parseBoolean(s.substring("ContainsPeaklist=".length()));
            }
        }
        throw new IllegalArgumentException("no ContainsPeaklist= part in " + line);
    }

    private static final String[] NOT_HANDLED_MGF_TAGS = {
            "TOLU=",
            "TOL=",
            "USER00",
            "USER01",
            "USER04",
            "USER05",
            "USER06",
            "USER08",
            "USER09",
            "USER10",
            "USER11",
            //   "TAXONOMY=",
            //      "SEQ=",
            "COMP=",
            "TAG=",
            "ETAG=",
            "SCANS=",
            "IT_MODS=",
            "CLUSTER_SIZE=",
            "PRECURSOR_INTENSITY="
            //         "INSTRUMENT=",
    };

    /**
     * @param inp !null reader
     * @return The parsed ISpectrum object
     */
    public static ISpectrum readMGFScan(LineNumberReader inp) {
        return readMGFScan(inp, null);
    }

    /**
     * @param inp  !null reader
     * @param line if non null the firat line of the stricture
     * @return The parsed ISpetrum object
     */
    @SuppressWarnings("ConstantConditions")
    private static ISpectrum readMGFScan(LineNumberReader inp, String line) {

        Properties props = new Properties();
        //noinspection UnnecessaryLocalVariable,UnusedDeclaration,UnusedAssignment
        String annotation = null;
        try {
            if (line == null)
                line = inp.readLine();

            double massToChargeCalledPpMass = 0;
            int dcharge = 1;
            String title = null;
            while (line != null) {
                line = line.trim();

                if ("".equals(line)) {
                    line = inp.readLine();
                    continue;
                }
                if (BEGIN_IONS.equals(line)) {
                    line = inp.readLine();
                    break;
                }

                line = inp.readLine();
            }
            if (line == null)
                return null;

            Set<Tuple<Float, Float>> holder = new HashSet<>();

            // add scan items
            while (line != null) {
                line = line.trim();
                // ignore empty lines
                if (line.length() == 0) {
                    line = inp.readLine();
                    continue;
                }

                // give up on lines not starting with a letter
                if (!Character.isLetterOrDigit(line.charAt(0))) {
                    line = inp.readLine();
                    continue;

                }


                if (line.contains("=")) {
                    if (line.startsWith("TITLE=")) {
                        line = inp.readLine();
                        continue;
                    }
                    if (line.startsWith("PEPMASS=")) {
                        massToChargeCalledPpMass = parsePepMassLine(line);
                        line = inp.readLine();
                        continue;
                    }
                    if (line.startsWith("CHARGE=")) {
                        line = line.replace("+", "");
                        final String substring = line.substring("CHARGE=".length());
                        if (substring.contains("."))
                            dcharge = (int) (0.5 + Double.parseDouble(substring));
                        else
                            dcharge = Integer.parseInt(substring);
                        line = inp.readLine();
                        continue;
                    }
                    if (line.startsWith("RTINSECONDS=")) {
                        //          retentionTime = line.substring("RTINSECONDS=".length());
                        line = inp.readLine();
                        continue;
                    }

                    if (line.startsWith("TAXONOMY=")) {
                        line = inp.readLine();
                        continue;
                    }

                    if (line.startsWith("TAXON=")) {
                        line = inp.readLine();
                        continue;
                    }

                    if (line.startsWith("USER02=")) {
                        line = inp.readLine();
                        continue;
                    }
                    if (line.startsWith("USER03=")) {
                        line = inp.readLine();
                        continue;
                    }
                    if (KnownProperties.addMGFProperties(props, line)) {
                        line = inp.readLine();
                        continue;
                    }

                    boolean tagIsNotHandled = false;
                    // ignored for now
                    //noinspection ForLoopReplaceableByForEach
                    for (int i = 0; i < NOT_HANDLED_MGF_TAGS.length; i++) {
                        String notHandledMgfTag = NOT_HANDLED_MGF_TAGS[i];
                        if (line.startsWith(notHandledMgfTag)) {
                            tagIsNotHandled = true;
                            line = inp.readLine();
                            break;
                        }

                    }
                    if (tagIsNotHandled)
                        continue;
                    // huh???
                    throw new IllegalStateException("Cannot parse MGF line " + line);
                }
                if (END_IONS.equals(line)) {
                    Spectrum spectrum = Spectrum.builder()
                            .peaks(holder)
                            .precursorCharge(dcharge)
                            .precursorMZ((float) massToChargeCalledPpMass).build();

                    props.clear();
                    return spectrum;
                } else {
                    line = line.replace("\t", " ");
                    String[] items = line.split(" ");
                    if (items.length >= 2) {
                        try {
                            float peakMass = Float.parseFloat(items[0].trim());
                            float peakIntensity = Float.parseFloat(items[1].trim());
                            Tuple<Float, Float> added = new Tuple<>(peakMass, peakIntensity);
                            holder.add(added);
                        } catch (NumberFormatException e) {
                            handleBadMGFData(line);
                        }
                    } else {
                        handleBadMGFData(line);
                    }
                    line = inp.readLine();

                }
            }
            return null; // or should an exception be thrown - we did not hit an END IONS tag
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * *******************************
     * Error handling code for MGF parse failure
     * *******************************
     */
    private static final int MAX_NUMBER_BAD_MGF_LINES = 2000;
    private static int gNumberBadMGFLines = 0;

    /**
     * we cannot parse a line of the form mass peak i.e.  370.2438965 3.906023979 in an
     * mgf file - the first  MAX_NUMBER_BAD_MGF_LINES output a message on stderr than
     * exceptions are thrown
     *
     * @param line !null line we cannot handle
     * @throws IllegalStateException after  MAX_NUMBER_BAD_MGF_LINES are seen
     */
    private static void handleBadMGFData(String line) throws IllegalStateException {
        if (gNumberBadMGFLines++ > MAX_NUMBER_BAD_MGF_LINES)
            throw new IllegalStateException("cannot read MGF data line " + line +
                    " failing after " + gNumberBadMGFLines + " errors");
    }

    /**
     * parse an mgf file - ised in testing
     *
     * @param filename !null name of an existing readible file
     */
    @SuppressWarnings("UnusedDeclaration")
    public static void guaranteeMGFParse(String filename) {
        try {
            guaranteeMGFParse(new FileInputStream(filename));
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);

        }
    }

    /**
     * parse an mgf file - used in testing
     *
     * @param is !null open inputstream
     */
    private static void guaranteeMGFParse(InputStream is) {
        LineNumberReader inp = new LineNumberReader(new InputStreamReader(is));
        ISpectrum scan = readMGFScan(inp, null);
        while (scan != null) {
            scan = readMGFScan(inp, null);
        }

    }

    /**
     * convert   PEPMASS=459.17000000000002 8795.7734375   into  459.17
     *
     * @param pLine line as above
     * @return indicasted mass
     */
    private static double parsePepMassLine(final String pLine) {
        final double mass;
        String numeric = pLine.substring("PEPMASS=".length());
        String massStr = numeric.split(" ")[0];
        mass = Double.parseDouble(massStr);
        return mass;
    }


    private static String buildMGFTitle(String line) {
        line = line.trim();
        int sequenceIndex = line.indexOf(",sequence=");
        String titleAndId = "TITLE=id=";
        int spectrumIdIndex = line.indexOf(titleAndId);

        if (sequenceIndex > -1) {
            if (spectrumIdIndex > -1)
                return line.substring(spectrumIdIndex + titleAndId.length(), sequenceIndex);
        } else {
            return line.substring(spectrumIdIndex + titleAndId.length());
        }

        return null;
    }


    /**
     * turn strings, resources and filenames into line number readers
     *
     * @param des The description to parse
     * @return A LineNumberReader that opened the description
     */
    public static LineNumberReader getDescribedReader(String des) {
        // maybe a string
        if (des.startsWith("str://")) {
            String substring = des.substring("str://".length());
            Reader isr = new StringReader(substring);
            return new LineNumberReader(isr);
        }

        // maybe a resource
        if (des.startsWith("res://")) {
            String substring = des.substring("res://".length());
            InputStream inputStream = ParserUtilities.class.getResourceAsStream(substring);
            if (inputStream == null)
                return null;
            return new LineNumberReader(new InputStreamReader(inputStream));
        }

        File f = new File(des);
        if (f.exists() && !f.isDirectory() && f.canRead()) {
            try {
                Reader isr = new FileReader(f);
                return new LineNumberReader(isr);
            } catch (FileNotFoundException e) {
                return null;
            }

        }

        return null;
    }

}
