package org.heigit.ors.fastisochrones.partitioning;

import com.carrotsearch.hppc.IntArrayList;

import java.util.*;
import java.util.Map.Entry;

/**
 * Helper class for sorting.
 * <p>
 *
 * @author Hendrik Leuschner
 */
public class Sort {
    //Sort ids list by values
    public IntArrayList sortByValueReturnList(Integer[] ids, Double[] values) {
        ArrayIndexComparator comparator = new ArrayIndexComparator(values);
        Integer[] indices = comparator.createIndexArray();
        //Sort the first cellArray in parallel
        Arrays.sort(indices, comparator);

        IntArrayList result = new IntArrayList(indices.length);
        for (int entry : indices) {
            result.add(ids[entry]);
        }

        return result;
    }

    public <K, V extends Comparable<? super V>> List<K> sortByValueReturnList(Map<K, V> map, boolean ascending) {
        List<Entry<K, V>> list = new ArrayList<>(map.entrySet());
        list.sort(Entry.comparingByValue());
        if (!ascending) {
            Collections.reverse(list);
        }
        List<K> result = new ArrayList<>(list.size());
        for (Entry<K, V> entry : list) {
            result.add(entry.getKey());
        }

        return result;
    }

    private class ArrayIndexComparator implements Comparator<Integer> {
        private final Double[] array;

        public ArrayIndexComparator(Double[] array) {
            this.array = array;
        }

        public Integer[] createIndexArray() {
            Integer[] indexes = new Integer[array.length];
            for (int i = 0; i < array.length; i++) {
                indexes[i] = i; // Autoboxing
            }
            return indexes;
        }

        @Override
        public int compare(Integer index1, Integer index2) {
            // Autounbox from Integer to int to use as array indexes
            return array[index1].compareTo(array[index2]);
        }
    }
}
