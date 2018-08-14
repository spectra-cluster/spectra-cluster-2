package org.spectra.cluster.normalizer;

import info.debatty.java.lsh.MinHash;
import info.debatty.java.lsh.SuperBit;

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
@Deprecated
public class LSHBinner implements IIntegerNormalizer{

    IIntegerNormalizer firstBinner;
    public final static Integer NUMBER_KERNELS = 25;
    public final static Integer DICTIONARY = 2;
    public Integer numberKernels;
    public Integer numberPeaksInKernel;

    public LSHBinner(IIntegerNormalizer firstBinner, int numberKernels, int numberPeaksInKernel){
        this.firstBinner = firstBinner;
        this.numberKernels = numberKernels;
        this.numberPeaksInKernel = numberPeaksInKernel;
    }

    public LSHBinner(){
        this.firstBinner = new SequestBinner();
        this.numberKernels = NUMBER_KERNELS;
        this.numberPeaksInKernel = DICTIONARY;
    }

    @Override
    public int[] binDoubles(List<Double> valuesToBin) {
        int [] vector = firstBinner.binDoubles(valuesToBin);
        return LSHBinner(vector);
    }

    /**
     * This function takes a vector of integers and return the Vector of size numberKernels of integers
      * @param valuesToBin Integer Vector
     * @return Vector of LSH values
     */
    public int[] binVector(int[] valuesToBin) {
        return LSHBinner(valuesToBin);
    }

    /**
     * This function generates the LSH Vector for a Vector of integers using {@link MinHash}
     * @param vector original vector to be transform
     * @return returns hash vector
     */
    private int[] LSHBinner(int[] vector){
        MinHash minhash = new MinHash(numberPeaksInKernel, numberKernels);
        return minhash.signature(new TreeSet<>(Arrays.stream(vector).boxed().collect(Collectors.toList())));
    }
}
