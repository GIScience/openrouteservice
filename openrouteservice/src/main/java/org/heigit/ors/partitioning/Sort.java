package org.heigit.ors.partitioning;

import com.carrotsearch.hppc.IntArrayList;
import com.carrotsearch.hppc.IntDoubleHashMap;

import java.util.*;
import java.util.Map.Entry;

public class  Sort {

    /**
     * #######################################################################################
     * S-T-A-T-I-C
     **/
    public static <T extends Comparable<? super T>> List<T> asSortedList(Collection<T> c) {
        List<T> result = new ArrayList<>(c);
        Collections.sort(result);

        return result;
    }

//    public static <K, V extends Comparable<? super V>> List<K> sortByValueReturnList(Map<K, V> map, boolean ASC) {
//        List<Entry<K, V>> list = new ArrayList<>(map.entrySet());
//        list.sort(Entry.comparingByValue());
//        if (!ASC) {
//            Collections.reverse(list);
//            //Collections.sort(list, Collections.reverseOrder());
//        }
//        List<K> result = new ArrayList<>();
//        for (Entry<K, V> entry : list) {
//            result.add(entry.getKey());
//        }
//
//        return result;
//    }

    public static IntArrayList sortByValueReturnList(Map<Integer, Double> map, boolean ASC, boolean isInt) {
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
    public static <K, V extends Comparable<? super V>> List<K> sortByValueReturnList(Map<K, V> map, boolean ASC) {
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



    public static <K, V extends Comparable<? super V>> Map<K, V> sortByValueReturnMap(Map<K, V> map) {
        List<Entry<K, V>> list = new ArrayList<>(map.entrySet());
        list.sort(Entry.comparingByValue());

        Map<K, V> result = new LinkedHashMap<>();
        for (Entry<K, V> entry : list) {
            result.put(entry.getKey(), entry.getValue());
        }

        return result;
    }
}
