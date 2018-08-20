package org.spectra.cluster.utils.math;

import cern.colt.list.DoubleArrayList;
import cern.colt.list.IntArrayList;
import cern.colt.matrix.DoubleMatrix1D;
import cern.colt.matrix.impl.SparseDoubleMatrix1D;

@Deprecated
public class Matrix2DUtils {

    static public double product(DoubleMatrix1D v1, DoubleMatrix1D v2) {
        if (v1 instanceof SparseDoubleMatrix1D) {
            return productQuick(v1, v2);
        } else if (v2 instanceof SparseDoubleMatrix1D) {
            return productQuick(v2, v1);
        } else {
            return v1.zDotProduct(v2);
        }
    }

    static public double productQuick(IntArrayList idxA, DoubleArrayList valueA, DoubleMatrix1D b)
    {
        double prod = 0.0;
        for (int i = 0; i < idxA.size(); ++i) {
            double temp = b.getQuick(idxA.getQuick(i));
            if (temp != 0.0) {
                prod += valueA.getQuick(i) * temp;
            }
        }
        return prod;
    }

    static public double productQuick(DoubleMatrix1D v1, DoubleMatrix1D v2) {
        IntArrayList indexList = new IntArrayList();
        DoubleArrayList valueList = new DoubleArrayList();
        v1.getNonZeros(indexList, valueList);
        double prod = 0.0;
        for (int i = 0; i < indexList.size(); ++i) {
            double temp = v2.getQuick(indexList.getQuick(i));
            if (temp != 0.0) {
                prod += valueList.getQuick(i) * temp;
            }
        }
        return prod;
    }

    public static double getSqrSum(DoubleArrayList valueList)
    {
        double sum =0, tmp ;
        for (int i = 0; i < valueList.size(); i++)
        {
            tmp = valueList.get(i);
            sum += tmp * tmp;
        }

        return sum;
    }

    public static double getSqrSum(DoubleMatrix1D vector)
    {
        IntArrayList indexList = new IntArrayList();
        DoubleArrayList valueList = new DoubleArrayList();
        vector.getNonZeros(indexList, valueList);
        double sum =0 ;
        for (int i = 0; i < indexList.size(); i++)
        {
            sum += valueList.get(i) * valueList.get(i);
        }
        return sum;
    }
}
