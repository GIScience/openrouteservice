package org.heigit.ors.partitioning;

import com.carrotsearch.hppc.IntArrayList;

import java.util.*;
import java.util.Map.Entry;

/**
 * Helper class for sorting.
 * <p>
 *
 * @author Hendrik Leuschner
 */

public class  Sort {

    /**
     * #######################################################################################
     * S-T-A-T-I-C
     **/

    //Sort ids list by values
    public IntArrayList sortByValueReturnList(Integer[] ids, Double[] values, int cellId) {
        ArrayIndexComparator comparator = new ArrayIndexComparator(values);
        Integer[] indices = comparator.createIndexArray();
        //Sort the first cellArray in parallel
        Arrays.sort(indices, comparator);

        IntArrayList result = new IntArrayList();
        for (int entry : indices) {
            result.add(ids[entry]);
        }

        return result;
    }

    public IntArrayList sortByValueReturnList(Map<Integer, Double> map, boolean ASC, boolean isInt) {
        List<Entry<Integer, Double>> list = new ArrayList<>(map.entrySet());
        list.sort(Entry.comparingByValue());
        if (!ASC) {
            Collections.reverse(list);
            //Collections.sort(list, Collections.reverseOrder());
        }
        IntArrayList result = new IntArrayList();
        for (Entry<Integer, Double> entry : list) {
            result.add(entry.getKey().intValue());
        }

        return result;
    }
    public <K, V extends Comparable<? super V>> List<K> sortByValueReturnList(Map<K, V> map, boolean ASC) {
        List<Entry<K, V>> list = new ArrayList<>(map.entrySet());
        list.sort(Entry.comparingByValue());
        if (!ASC) {
            Collections.reverse(list);
            //Collections.sort(list, Collections.reverseOrder());
        }
        List<K> result = new ArrayList<>();
        for (Entry<K, V> entry : list) {
            result.add(entry.getKey());
        }

        return result;
    }


    private class ArrayIndexComparator implements Comparator<Integer>
    {
        private final Double[] array;

        public ArrayIndexComparator(Double[] array)
        {
            this.array = array;
        }

        public Integer[] createIndexArray()
        {
            Integer[] indexes = new Integer[array.length];
            for (int i = 0; i < array.length; i++)
            {
                indexes[i] = i; // Autoboxing
            }
            return indexes;
        }

        @Override
        public int compare(Integer index1, Integer index2)
        {
            // Autounbox from Integer to int to use as array indexes
            return array[index1].compareTo(array[index2]);
        }
    }
}
