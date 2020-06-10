package com.hyh.arithmetic.skills;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Solution2 {
    public int[] smallestSufficientTeam(String[] req_skills, List<List<String>> people) {
        Map<String, List<String>> mergeCache = new HashMap<>();
        for (int i = 0; i < people.size(); i++) {
            int[] ints = merge(req_skills, people, i + 1, mergeCache);
            if (ints != null) {
                return ints;
            }
        }
        return null;
    }

    private int[] merge(String[] req_skills, List<List<String>> people, int count, Map<String, List<String>> mergeCache) {
        if (count == 1) {
            for (int i = 0; i < people.size(); i++) {
                List<String> strings = people.get(i);
                if (contains(strings, req_skills)) {
                    return new int[]{i};
                } else {
                    mergeCache.put(String.valueOf(i), strings);
                }
            }
        } else {
            Index index = new Index(count, people.size() - 1);
            do {
                String prefix = index.getPrefix();
                int lastIndex = index.lastIndex();
                List<String> strings = mergeCache.get(prefix);
                List<String> person = people.get(lastIndex);
                ArrayList<String> mergeList = new ArrayList<>(strings);
                mergeList.addAll(person);
                if (contains(mergeList, req_skills)) {
                    return index.indexes;
                } else {
                    mergeCache.put(index.toString(), mergeList);
                }
            } while (index.increase());
        }
        return null;
    }

    private boolean contains(List<String> strings, String[] req_skills) {
        if (strings.size() < req_skills.length) return false;
        for (String req_skill : req_skills) {
            if (!strings.contains(req_skill)) return false;
        }
        return true;
    }

    private static class Index {

        int[] indexes;

        int size;

        int max;

        public Index(int count, int max) {
            this.indexes = new int[count];
            this.size = count;
            this.max = max;
            for (int i = 0; i < count; i++) {
                indexes[i] = count - 1 - i;
            }
        }

        public boolean increase() {
            if (size > max) return false;
            boolean increase = false;
            int i = 0;
            int max = this.max;
            while (i < size) {
                int index = indexes[i];
                if (max == index) {
                    i++;
                    max--;
                    continue;
                }
                increase = true;
                index++;
                indexes[i] = index;
                i--;
                while (i >= 0) {
                    index++;
                    indexes[i] = index;
                    i--;
                }
                break;
            }
            return increase;
        }

        public String getPrefix() {
            StringBuilder sb = new StringBuilder();
            for (int i = size - 1; i >= 1; i--) {
                sb.append(indexes[i]);
                if (i > 1) {
                    sb.append("-");
                }
            }
            return sb.toString();
        }

        public int lastIndex() {
            return indexes[0];
        }


        @Override
        public String toString() {
            StringBuilder sb = new StringBuilder();
            for (int i = size - 1; i >= 0; i--) {
                sb.append(indexes[i]);
                if (i > 0) {
                    sb.append("-");
                }
            }
            return sb.toString();
        }
    }
}
