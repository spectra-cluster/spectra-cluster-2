package org.spectra.cluster.normalizer;

import info.debatty.java.lsh.MinHash;
import org.spectra.cluster.filter.IFilter;

import java.util.Arrays;
import java.util.List;
import java.util.TreeSet;
import java.util.stream.Collectors;

/**
 * This code is licensed under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * ==Overview==
 *
 * @author ypriverol on 14/08/2018.
 */

public class LSHBinner  {

    public final static Integer NUMBER_KERNELS = 10;
    public final static Integer DICTIONARY = 2;
    public Integer VECTOR_SIZE = 2500;

    public Integer numberKernels;
    public Integer numberPeaksInKernel;
    public Integer vectorSize;

    private final MinHash minHashInstance;

    private static LSHBinner instance;

    /**
     * Singelton Pattern because {@link MinHash} need a unique instance
     * @return LSHBinner
     */
    public static LSHBinner getInstance(){
        if(instance == null)
            instance = new LSHBinner();
        return instance;
    }

    /**
     * Parametrized Singelton Instance
     * @param numberKernels Number of Kernels
     * @param numberPeaksInKernel Number of PEaks in a kernel
     * @param vector_size Vector Size.
     * @return LSHBinner
     */
    public static LSHBinner getInstance(int numberKernels, int numberPeaksInKernel,
                                        int vector_size){
        if(instance == null)
            instance = new LSHBinner(numberKernels, numberPeaksInKernel, vector_size);
        return instance;
    }

    private LSHBinner(int numberKernels, int numberPeaksInKernel, int vector_size){
        this.numberKernels = numberKernels;
        this.numberPeaksInKernel = numberPeaksInKernel;
        minHashInstance = new MinHash(numberKernels, numberPeaksInKernel, vector_size);
    }

    /**
     * Default LSHBinner use the @{@link SequestBinner} to normalize the mzValues and
     * default parameters to generate the {@link MinHash}.
     */
    private LSHBinner(){
        this.numberKernels = NUMBER_KERNELS;
        this.numberPeaksInKernel = DICTIONARY;
        this.vectorSize = VECTOR_SIZE;
        minHashInstance = new MinHash(numberKernels, numberPeaksInKernel, vectorSize);

    }

    /**
     * Perform the binning process using the mzValues as input. The binning of the mzValues is performed
     * using the initialize {@link IIntegerNormalizer} binner.
     *
     * @param mzValues The values that should be binned
     * @return LSH vector of integers
     */
    public int[] getKernels(int[] mzValues) {
        return lshbinner(mzValues);
    }

    /**
     * This function generates the LSH Vector for a Vector of integers using {@link MinHash}
     * @param vector original vector to be transform
     * @return returns hash vector
     */
    private int[] lshbinner(int[] vector){
        return minHashInstance.signature(new TreeSet<>(Arrays
                .stream(vector)
                .boxed()
                .collect(Collectors.toList()
                )
        ));
    }
}
