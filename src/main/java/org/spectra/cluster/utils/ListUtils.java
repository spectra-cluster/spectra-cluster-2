package org.spectra.cluster.utils;

import java.util.Iterator;
import java.util.List;

/**
 * A collection of helper functions to work with primitives
 */
public class ListUtils {
    /**
     * Convert a list of Integers to a primitive array of int.
     * @param list
     * @return
     */
    public static int[] intListToArray(List<Integer> list) {
        int[] ret = new int[list.size()];

        Iterator<Integer> it = list.iterator();

        for (int i = 0; i < list.size(); i++) {
            ret[i] = it.next();
        }

        return ret;
    }
}
