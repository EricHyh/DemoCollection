package com.hyh.arithmetic.skills;

import java.lang.reflect.Array;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
public class Solution1 {

    public int[] smallestSufficientTeam(String[] req_skills, List<List<String>> people) {
        Map<String, List<String>> mergeCache = new HashMap<>();


    }

    public boolean merge(String[] req_skills, List<List<String>> people, int count, Map<String, List<String>> mergeCache) {
        if (count == 1) {
            for (int i = 0; i < people.size(); i++) {
                List<String> strings = people.get(i);
            }
        }
        return false;
    }

    public boolean contains(List<String> strings, String[] req_skills) {
        return false;
    }


    private static class Index {

        int[] indexes;

        int size;

        int max;

        public Index(int count, int max) {
            this.indexes = new int[count];
            this.size = count;
            this.max = max;
        }

        public void increase() {
            int i = 0;
            int max = this.max;

            int index = indexes[i];
            index++;
            if(index>){

            }



            if (index <= max) {
                indexes[i] = index;
                int j = i - 1;
                while (j >= 0) {
                    index++;

                    int lastIndex = indexes[j];
                    if (lastIndex < index) {
                        indexes[j] = index;
                        j--;
                    } else {
                        break;
                    }
                }
            } else {

            }
        }
    }
}