package org.spectra.cluster.normalizer;

import cern.colt.bitvector.BitVector;
import lombok.Getter;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * This class provides functions to convert a
 * list of doubles (ie. m/z values) into a bit
 * vector.
 */
public class MzValuesToBitVectorConverter {

    public static final double DEFAULT_MIN_MZ = 0;
    public static final double DEFAULT_MAX_MZ = 2001.18;

    @Getter
    private final double minMz;
    @Getter
    private final double maxMz;

    @Getter
    private final int binOffset;
    @Getter
    private final int vectorSize;
    /**
     * The integer normalizer to use to convert the
     * m/z values into their location in the bit mask.
     * This should be some kind of binner.
     */
    private final IIntegerNormalizer integerNormalizer;

    /**
     * Creates a new MzValuesToBitVectorConverter to convert m/z values based on a discrete binning
     * approach into a BitVector.
     *
     * @param integerNormalizer The normalizer to use to convert m/z values into bin locations.
     * @param minMz The minimum m/z to support (used as a bin offset)
     * @param maxMz The maximum m/z to support (all m/z values above will be ignored).
     */
    public MzValuesToBitVectorConverter(IIntegerNormalizer integerNormalizer, double minMz, double maxMz) {
        this.minMz = minMz;
        this.maxMz = maxMz;
        this.integerNormalizer = integerNormalizer;

        // initialize the offset and bin vector size
        List<Double> minMaxList = new ArrayList<>(1);
        minMaxList.add(this.minMz);
        minMaxList.add(this.maxMz);
        int[] binoffset = integerNormalizer.binDoubles(minMaxList);

        this.binOffset = binoffset[0];
        this.vectorSize = binoffset[1] - this.binOffset + 1;
    }

    /**
     * Creates a new MzValuesToBitVectorConverter to convert m/z values based on a discrete binning
     * approach into a BitVector using default minimum and maximum m/z values.
     *
     * @param integerNormalizer The normalizer to use to convert m/z values into bin locations.
     */
    public MzValuesToBitVectorConverter(IIntegerNormalizer integerNormalizer) {
        this(integerNormalizer, DEFAULT_MIN_MZ, DEFAULT_MAX_MZ);
    }

    /**
     * Convert the list of mzValues into a bit vector.
     * @param mzValues The m/z values to convert
     * @return A BitVector representing whether peaks are present at a given bin.
     */
    BitVector mzToBitVector(List<Double> mzValues) {
        // remove mzValues that are too large
        mzValues = mzValues.stream().filter(value -> value <= this.maxMz).collect(Collectors.toList());

        // get the bins
        int[] bins = this.integerNormalizer.binDoubles(mzValues);

        // set the bin vector
        BitVector vector = new BitVector(this.vectorSize);
        for (int bin : bins) {
            if (bin < this.binOffset) {
                continue;
            }
            // sanity check
            if (bin > this.vectorSize - 1) {
                throw new IllegalStateException("Filtering of maximum m/z value failed.");
            }

            vector.put(bin - this.binOffset, true);
        }

        return vector;
    }
}
