package com.hyh.arithmetic.skills;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * 规划了一份需求的技能清单 req_skills，并打算从备选人员名单 people 中选出些人组成一个「必要团队」
 * （ 编号为 i 的备选人员 people[i] 含有一份该备选人员掌握的技能列表）。
 * 所谓「必要团队」，就是在这个团队中，对于所需求的技能列表 req_skills 中列出的每项技能，团队中至少有一名成员已经掌握。
 * 我们可以用每个人的编号来表示团队中的成员：例如，团队 team = [0, 1, 3] 表示掌握技能分别为 people[0]，people[1]，和 people[3] 的备选人员。
 * 请你返回 任一 规模最小的必要团队，团队成员用人员编号表示。你可以按任意顺序返回答案，本题保证答案存在。
 * <p>
 * 示例 1：
 * 输入：req_skills = ["java","nodejs","reactjs"],
 * people = [["java"],["nodejs"],["nodejs","reactjs"]]
 * 输出：[0,2]
 * <p>
 * 示例 2：
 * 输入：req_skills = ["algorithms","math","java","reactjs","csharp","aws"],
 * people = [["algorithms","math","java"],["algorithms","math","reactjs"],["java","csharp","aws"],["reactjs","csharp"],["csharp","math"],["aws","java"]]
 * 输出：[1,2]
 */
public class Solution3 {

    public int[] smallestSufficientTeam(String[] req_skills, List<List<String>> people) {
        Map<String, List<String>> lastCache = null;
        Map<String, List<String>> mergeCache = new HashMap<>();
        int count = 1;
        do {
            int[] res = merge(req_skills, people, count, lastCache, mergeCache);
            if (res != null) return res;
            lastCache = mergeCache;
            count++;
        } while (people.size() >= count);

        return null;
    }

    private int[] merge(String[] req_skills,
                        List<List<String>> people,
                        int count,
                        Map<String, List<String>> lastCache,
                        Map<String, List<String>> mergeCache) {
        if (count == 1) {
            for (int index = 0; index < people.size(); index++) {
                List<String> strings = people.get(index);
                if (contains(strings, req_skills)) {
                    return new int[]{index};
                } else {
                    Set<Map.Entry<String, List<String>>> entrySet = mergeCache.entrySet();
                    boolean contains = false;
                    for (Map.Entry<String, List<String>> entry : entrySet) {
                        if (contains(entry.getValue(), strings)) {
                            contains = true;
                            break;
                        }
                    }
                    if (contains) {
                        continue;
                    }
                    mergeCache.put(String.valueOf(index), strings);
                }
            }
        } else {
            Index index = new Index(count, people.size() - 1);
            do {
                String prefix = index.getPrefix();
                int lastIndex = index.lastIndex();
                List<String> strings = lastCache.get(prefix);
                if (strings == null) continue;
                List<String> person = people.get(lastIndex);
                List<String> mergeList = new ArrayList<>(strings);
                mergeList.addAll(person);

                Set<Map.Entry<String, List<String>>> entrySet = mergeCache.entrySet();
                boolean contains = false;
                for (Map.Entry<String, List<String>> entry : entrySet) {
                    if (contains(entry.getValue(), mergeList)) {
                        contains = true;
                        break;
                    }
                }
                if (contains) continue;
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

    private boolean contains(List<String> strings1, List<String> strings2) {
        if (strings1.size() < strings2.size()) return false;
        for (String string : strings2) {
            if (!strings1.contains(string)) return false;
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

        public void setMax(int max) {
            this.max = max;
        }
    }
}