package org.spectra.cluster.similarity;


import cern.colt.matrix.impl.SparseDoubleMatrix1D;
import info.debatty.java.lsh.MinHash;
import lombok.extern.slf4j.Slf4j;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.spectra.cluster.model.spectra.BinarySpectrum;
import org.spectra.cluster.normalizer.LSHBinner;
import org.spectra.cluster.normalizer.SequestBinner;
import uk.ac.ebi.pride.tools.jmzreader.JMzReaderException;
import uk.ac.ebi.pride.tools.jmzreader.model.Spectrum;
import uk.ac.ebi.pride.tools.mgf_parser.MgfFile;

import java.io.File;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
public class JaccardComparatorTest {

    BinarySpectrum binarySpectrum1 = null;
    BinarySpectrum binarySpectrum2 = null;

    Spectrum spectrum1 = null;
    Spectrum spectrum2 = null;

    Iterator<Spectrum> specIt;
    Iterator<Spectrum> specItSequence;
    SequestBinner binnerNormalizer = new SequestBinner();


    @Before
    public void setUp() throws JMzReaderException, URISyntaxException {

        URI uri = Objects.requireNonNull(BinarySpectrum.class.getClassLoader().getResource("single-spectra.mgf")).toURI();
        MgfFile mgfFile = new MgfFile(new File(uri));

        specIt = mgfFile.getSpectrumIterator();

        spectrum1 = specIt.next();

        binarySpectrum1 = BinarySpectrum.builder()
                .precursorMZ((int)spectrum1.getPrecursorMZ().doubleValue())
                .precursorCharge(spectrum1.getPrecursorCharge())
                .mzPeaksVector(binnerNormalizer.binDoubles(spectrum1.getPeakList().entrySet().stream().map(Map.Entry::getKey).collect(Collectors.toList())))
                .intensityPeaksVector(binnerNormalizer.binDoubles(spectrum1.getPeakList().entrySet().stream().map(Map.Entry::getValue).collect(Collectors.toList())))
                .build();

        spectrum2 = specIt.next();

        binarySpectrum2 = BinarySpectrum.builder()
                .precursorMZ((int)spectrum2.getPrecursorMZ().doubleValue())
                .precursorCharge(spectrum2.getPrecursorCharge())
                .mzPeaksVector(binnerNormalizer.binDoubles(spectrum2.getPeakList().entrySet().stream().map(Map.Entry::getKey).collect(Collectors.toList())))
                .intensityPeaksVector(binnerNormalizer.binDoubles(spectrum2.getPeakList().entrySet().stream().map(Map.Entry::getValue).collect(Collectors.toList())))
                .build();

        /** Read the Spectra from similar files **/
        uri = Objects.requireNonNull(BinarySpectrum.class.getClassLoader().getResource("most_similar_1.mgf")).toURI();
        mgfFile = new MgfFile(new File(uri));

        specIt = mgfFile.getSpectrumIterator();

        uri = Objects.requireNonNull(BinarySpectrum.class.getClassLoader().getResource("same_sequence_cluster.mgf")).toURI();
        mgfFile = new MgfFile(new File(uri));

        specItSequence = mgfFile.getSpectrumIterator();

    }

    @Test
    public void computeVectorJaccard() {

        double similarity = JaccardComparator.computeVectorJaccard(binarySpectrum1.getMzPeaksVector(), binarySpectrum2.getMzPeaksVector());
        Assert.assertTrue(similarity - 0.99f < 0.1);
    }


    @Test
    public void computeSparseMatrixJaccard() {

        SparseDoubleMatrix1D sparseMatrix1 = new SparseDoubleMatrix1D(Arrays.stream(binnerNormalizer.binDoubles(spectrum1.getPeakList().entrySet().stream().map(Map.Entry::getKey).collect(Collectors.toList()))).asDoubleStream().toArray());
        SparseDoubleMatrix1D sparseMatrix2 = new SparseDoubleMatrix1D(Arrays.stream(binnerNormalizer.binDoubles(spectrum2.getPeakList().entrySet().stream().map(Map.Entry::getKey).collect(Collectors.toList()))).asDoubleStream().toArray());

        double similarity = JaccardComparator.computeSparseMatrixJaccard(sparseMatrix1, sparseMatrix2);

        Assert.assertTrue(similarity - 0.91f < 0.1);

    }

    @Test
    public void computeJaccardBatch(){
        List<BinarySpectrum> binarySpectrumList = readBinarySpectrumList(specIt);
        printSimilarityJaccard(binarySpectrumList);

        log.info("A new file would be analyzed -- ");

        binarySpectrumList = readBinarySpectrumList(specItSequence);
        printSimilarityJaccard(binarySpectrumList);
    }




    /**
     * This is how spectra is read from mgf into a list of BinarySpectrum
     * @param it {@link uk.ac.ebi.pride.tools.jmzreader.JMzReader} Iterator
     * @return List of {@link BinarySpectrum}
     */
    private List<BinarySpectrum> readBinarySpectrumList(Iterator<Spectrum> it){
        List<BinarySpectrum> binarySpectrumList = new ArrayList<>();
        while(it.hasNext()){
            Spectrum spec = it.next();
            binarySpectrumList.add(BinarySpectrum
                    .builder()
                    .precursorMZ((int) spec.getPrecursorMZ().doubleValue())
                    .precursorCharge(spec.getPrecursorCharge())
                    .mzPeaksVector(binnerNormalizer.binDoubles(spec.getPeakList().entrySet().stream().map(Map.Entry::getKey).collect(Collectors.toList())))
                    .intensityPeaksVector(binnerNormalizer.binDoubles(spec.getPeakList().entrySet().stream().map(Map.Entry::getValue).collect(Collectors.toList())))
                    .build()
            );
        }
        return binarySpectrumList;
    }

    /**
     * This function prints the binarySpectrumList
     *
     * @param binarySpectrumList List of {@link BinarySpectrum}
     */
    private void printSimilarityJaccard(List<BinarySpectrum> binarySpectrumList){
        LSHBinner lshBinner = new LSHBinner();
        for(int x = 0; x < binarySpectrumList.size(); x++){
            for(int y= x+1; y < binarySpectrumList.size(); y++){


                SparseDoubleMatrix1D sparseX = new SparseDoubleMatrix1D(Arrays.stream(binarySpectrumList.get(x).getMzPeaksVector()).asDoubleStream().toArray());
                SparseDoubleMatrix1D sparseY = new SparseDoubleMatrix1D(Arrays.stream(binarySpectrumList.get(y).getMzPeaksVector()).asDoubleStream().toArray());

                int[] vector1 = lshBinner.binVector(binarySpectrumList.get(x).getMzPeaksVector());
                int[] vector2 = lshBinner.binVector(binarySpectrumList.get(y).getMzPeaksVector());

                log.info("Spectrum: " + binarySpectrumList.get(x).getUUI() + " and Spectrum: " + binarySpectrumList.get(y).getUUI() + " SparseJaccard: " +
                        JaccardComparator.computeSparseMatrixJaccard(sparseX, sparseY) + " Jaccard: " + MinHash.jaccardIndex(Arrays.stream(binarySpectrumList.get(x).getMzPeaksVector()).boxed().collect(Collectors.toSet()), Arrays.stream(binarySpectrumList.get(y).getMzPeaksVector()).boxed().collect(Collectors.toSet())) + " LSH Jaccard: "
                        + MinHash.jaccardIndex(Arrays.stream(vector1).boxed().collect(Collectors.toSet()), Arrays.stream(vector2).boxed().collect(Collectors.toSet())));
            }
        }
    }
}
