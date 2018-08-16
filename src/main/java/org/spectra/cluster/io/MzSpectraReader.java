package org.spectra.cluster.io;

import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.spectra.cluster.model.commons.IteratorConverter;
import org.spectra.cluster.model.spectra.BinarySpectrum;
import org.spectra.cluster.model.spectra.IBinarySpectrum;
import org.spectra.cluster.normalizer.IIntegerNormalizer;
import org.spectra.cluster.normalizer.SequestBinner;
import uk.ac.ebi.pride.tools.apl_parser.AplFile;
import uk.ac.ebi.pride.tools.dta_parser.DtaFile;
import uk.ac.ebi.pride.tools.jmzreader.JMzReader;
import uk.ac.ebi.pride.tools.jmzreader.JMzReaderException;
import uk.ac.ebi.pride.tools.jmzreader.model.Spectrum;
import uk.ac.ebi.pride.tools.mgf_parser.MgfFile;
import uk.ac.ebi.pride.tools.ms2_parser.Ms2File;
import uk.ac.ebi.pride.tools.mzdata_wrapper.MzMlWrapper;
import uk.ac.ebi.pride.tools.mzxml_parser.MzXMLFile;
import uk.ac.ebi.pride.tools.pkl_parser.PklFile;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.Arrays;
import java.util.Iterator;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * This code is licensed under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 *
 * Read Spectra from files into {@link org.spectra.cluster.model.spectra.BinarySpectrum} representation
 *
 *
 * @author ypriverol on 14/08/2018.
 */
@Slf4j
public class MzSpectraReader {

    /** Pattern for validating mzML format */
    private static final Pattern mzMLHeaderPattern = Pattern.compile("^[^<]*(<\\?xml [^>]*>\\s*(<!--[^>]*-->\\s*)*)?<(mzML)|(indexedmzML) xmlns=.*", Pattern.MULTILINE);

    /** Pattern for validating mzXML format */
    private static final Pattern mzXmlHeaderPattern = Pattern.compile("^[^<]*(<\\?xml [^>]*>\\s*(<!--[^>]*-->\\s*)*)?<(mzXML) xmlns=.*", Pattern.MULTILINE);

    /** This enum type Capture the two file types supported in Spectra Cluster **/
    public enum MzFileType{

        MGF("MGF", ".mgf"),
        MZML("MZML", ".mzml"),
        MS2("MS2", ".ms2"),
        APL("APL", ".apl"),
        PKL("PKL", ".pkl"),
        DTA("DTA", ".dta"),
        MZXML("MZXML", "mzXML");

        private String name;
        private String extension;

        MzFileType(String name, String extension){
            this.name = name;
            this.extension = extension;
        }

        public String getName() {
            return name;
        }

        public String getExtension() {
            return extension;
        }
    }

    private JMzReader jMzReader;

    @Setter
    private IIntegerNormalizer mzBinner;

    @Setter
    private IIntegerNormalizer intensityBinner;

    /**
     * Create a Reader from a file. The file type accepted are mgf or mzml
     * @param file File to be read
     * @throws Exception File not supported
     */
    public  MzSpectraReader(File file, IIntegerNormalizer mzBinner, IIntegerNormalizer intensityBinner) throws Exception {
        try{
            Class<?> peakListclass = isValidPeakListFile(file);
            if( peakListclass != null){
                if(peakListclass == MgfFile.class)
                    jMzReader = new MgfFile(file);
                else if (peakListclass == AplFile.class)
                    jMzReader = new AplFile(file);
                else if(peakListclass == Ms2File.class)
                    jMzReader = new Ms2File(file);
                else if(peakListclass == PklFile.class)
                    jMzReader = new PklFile(file);
                else if(peakListclass == DtaFile.class)
                    jMzReader = new PklFile(file);
            } else if(isValidMzML(file))
                jMzReader = new MzMlWrapper(file);
            else if(isValidmzXML(file))
                jMzReader = new MzXMLFile(file);
        }catch (JMzReaderException e){
            String message = "The file type provided is not support -- " + Arrays.toString(MzFileType.values());
            log.error(message);
            throw new Exception(message);
        }

        this.mzBinner = mzBinner;
        this.intensityBinner = intensityBinner;
    }

    /**
     * Default constructor use as a MzBinner the {@link org.spectra.cluster.normalizer.SequestBinner} and as
     * intensity binner the
     * //Todo: Intensity Binner
     * @param file File
     */
    public MzSpectraReader(File file) throws Exception {
        this(file, new SequestBinner(), new SequestBinner());
    }

    /**
     * Return the iterator with the {@link IBinarySpectrum} transformed from the
     * {@link Spectrum} file.
     *
     * @return Iterator of {@link BinarySpectrum} spectra
     */
    public Iterator<IBinarySpectrum> readBinarySpectraIterator() {
        return new IteratorConverter<>(jMzReader.getSpectrumIterator(),
                spectrum -> BinarySpectrum.builder()
                        .precursorCharge(spectrum.getPrecursorCharge())
                        .precursorMZ((int) spectrum.getPrecursorMZ().doubleValue())
                        .mzPeaksVector(mzBinner.binDoubles(spectrum.getPeakList()
                                .entrySet().stream().map(Map.Entry::getKey).collect(Collectors.toList())))
                        .intensityPeaksVector(intensityBinner.binDoubles(spectrum.getPeakList()
                                .entrySet().stream().map(Map.Entry::getKey).collect(Collectors.toList())))
                        .build());
    }

    /**
     * Get the Class for the specific Peak List reader
     * @param file File to be read
     * @return Class Reader {@link JMzReader} Adapter
     */
    private Class<?> isValidPeakListFile(File file){

        String filename = file.getName().toLowerCase();

        if (filename.endsWith(MzFileType.DTA.getExtension()))
            return DtaFile.class;
        else if (filename.endsWith(MzFileType.MGF.getExtension()))
            return MgfFile.class;
        else if (filename.endsWith(MzFileType.MS2.getExtension()))
            return Ms2File.class;
        else if (filename.endsWith(MzFileType.PKL.getExtension()))
            return PklFile.class;
        else if (filename.endsWith(MzFileType.APL.getExtension()))
            return AplFile.class;

        return null;
    }

    /**
     * Check if the following file provided is a proper mzML. In this case an extra check is done
     * to see if the file inside contains mzML data.
     *
     * @param file File to be processed
     * @return True if is a valide mzML
     */
    private boolean isValidMzML(File file){
        return checkXMLValidFile(file, mzMLHeaderPattern, MzFileType.MZML);
    }

    /**
     * Check if the following file provided is a proper mzXML. In this case an extra check is done
     * to see if the file inside contains mzXML data.
     *
     * @param file File to be processed
     * @return True if is a valide mzXML
     */
    private boolean isValidmzXML(File file){
        return checkXMLValidFile(file, mzXmlHeaderPattern, MzFileType.MZXML);
    }

    /**
     * This function that check if the first lines of an XML file contains an specific
     * Pattern. It also validates that the file extension correspond to the {@link MzFileType}
     * @param file File
     * @param pattern Pattern to be search
     * @param fileType {@link MzFileType}
     * @return True if the extension and the pattern match .
     */
    private boolean checkXMLValidFile(File file, Pattern pattern, MzFileType fileType){
        boolean valid = false;
        String filename = file.getName().toLowerCase();
        if(filename.endsWith(fileType.getExtension())){
            BufferedReader reader = null;
            try {
                reader = new BufferedReader(new FileReader(file));
                StringBuilder content = new StringBuilder();
                for (int i = 0; i < 10; i++) {
                    content.append(reader.readLine());
                }
                Matcher matcher = pattern.matcher(content);
                valid = matcher.find();
            } catch (Exception e) {
                log.error("Failed to read the provided file -- " + file.getAbsolutePath(), e);
            } finally {
                if (reader != null) {
                    try {
                        reader.close();
                    } catch (IOException e) {
                        log.error("The File is not an valid -- + " + fileType.getName() +  " -- " + file.getAbsolutePath(), e);
                    }
                }
            }
        }
        return valid;

    }



}
