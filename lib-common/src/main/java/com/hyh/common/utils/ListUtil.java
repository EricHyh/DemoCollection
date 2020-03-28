package com.hyh.common.utils;

import java.util.List;
import java.util.ListIterator;

/**
 * @author Administrator
 * @description
 * @data 2019/12/28
 */
public class ListUtil {

    public static <T> boolean isEmpty(List<T> list) {
        return list == null || list.isEmpty();
    }

    public static <T> boolean move(List<T> list, int sourceIndex, int targetIndex) {
        if (isEmpty(list)) return false;
        int size = list.size();
        if (size <= sourceIndex || size <= targetIndex) return false;
        if (sourceIndex == targetIndex) {
            return true;
        }
        list.add(targetIndex, list.remove(sourceIndex));
        return true;
    }

    public static <T> boolean equals(List<T> list1, List<T> list2) {
        if (isEmpty(list1) && isEmpty(list2)) return true;
        if (list1 == null || list2 == null) return false;

        if (list1 == list2) return true;

        ListIterator<T> e1 = list1.listIterator();
        ListIterator<T> e2 = list2.listIterator();
        while (e1.hasNext() && e2.hasNext()) {
            T o1 = e1.next();
            T o2 = e2.next();
            if (!(o1 == null ? o2 == null : o1.equals(o2))) return false;
        }
        return !(e1.hasNext() || e2.hasNext());
    }

    public static <T> int size(List<T> list) {
        return list == null ? 0 : list.size();
    }
}