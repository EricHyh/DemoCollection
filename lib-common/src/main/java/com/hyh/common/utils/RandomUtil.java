package com.hyh.common.utils;

import java.util.List;

/**
 * @author Administrator
 * @description
 * @data 2019/8/30
 */

public class RandomUtil {

    public static int random(int start, int end) {
        int startOffset = start - 1;
        int diff = end - start + 1;
        double random = Math.random();//[0.0, 1.0)
        random = 1 - random;//(0.0, 1.0]
        random *= diff;
        return (int) Math.ceil(random) + startOffset;
    }

    public static <T> T randomSelect(List<T> list, PercentProvider<T> percentProvider) {
        return randomSelect(list, 100, percentProvider);
    }

    public static <T> T randomSelect(List<T> list, int totalPercent, PercentProvider<T> percentProvider) {
        if (list == null || list.isEmpty()) return null;
        int random = random(1, totalPercent);
        for (T t : list) {
            int percent = percentProvider.getPercent(t);
            if (random > percent) {
                random -= percent;
                continue;
            }
            return t;
        }
        return null;
    }

    public interface PercentProvider<T> {

        int getPercent(T t);

    }
}