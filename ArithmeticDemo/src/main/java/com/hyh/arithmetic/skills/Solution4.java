package com.hyh.arithmetic.skills;

import android.annotation.SuppressLint;

import java.util.ArrayList;
import java.util.List;

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
 * <p>
 * <p>
 * 1 <= req_skills.length <= 16
 * 1 <= people.length <= 60
 * 1 <= people[i].length, req_skills[i].length, people[i][j].length <= 16
 * req_skills 和 people[i] 中的元素分别各不相同
 * req_skills[i][j], people[i][j][k] 都由小写英文字母组成
 * 本题保证「必要团队」一定存在
 */
public class Solution4 {

    @SuppressLint("UseSparseArrays")
    public int[] smallestSufficientTeam(String[] req_skills, List<List<String>> people) {
        int req_skills_code = (int) (Math.pow(2, req_skills.length) - 1);
        List<Integer> people_code = new ArrayList<>();
        for (int i = 0; i < people.size(); i++) {
            List<String> person_skills = people.get(i);
            int person_code = 0;
            for (int j = 0; j < person_skills.size(); j++) {
                String skill = person_skills.get(j);
                int index = indexOf(req_skills, skill);
                if (index >= 0) {
                    person_code += Math.pow(2, index);
                }
            }
            people_code.add(person_code);
        }
        for (int i = 0; i < people_code.size(); i++) {
            Integer i_person_code = people_code.get(i);
            if (i_person_code == 0) continue;
            if (i == people_code.size() - 1) break;
            for (int j = i + 1; j < people_code.size(); j++) {
                Integer j_person_code = people_code.get(j);
                if ((i_person_code | j_person_code) == j_person_code) {
                    people_code.set(i, 0);
                } else if ((i_person_code | j_person_code) == i_person_code) {
                    people_code.set(j, 0);
                }
            }
        }

        Object[] preResult = new Object[req_skills.length];
        Object[] result = new Object[req_skills.length];

        /*Integer person_code = people_code.get(0);
        for (int i = 0; i < req_skills.length; i++) {
            int skills_code = (int) (Math.pow(2, i + 1) - 1);
            if ((person_code | skills_code) == person_code) {
                preResult[i] = new int[]{0};
            } else {
                break;
            }
        }*/

        int person_code = 0;
        for (int i = 0; i < people_code.size(); i++) {
            person_code |= people_code.get(i);
            for (int j = 0; j < req_skills.length; j++) {
                int skills_code = (int) (Math.pow(2, j + 1) - 1);
                if ((person_code | skills_code) == person_code) {
                    //result[i] = new int[]{0};
                } else {

                }
            }
        }




        /*for (int i = 0; i < req_skills.length; i++) {
            int skills_code = (int) (Math.pow(2, i + 1) - 1);
            int people_code_temp = 0;
            for (int j = 0; j < people_code.size(); j++) {
                people_code_temp |= people_code.get(j);
                if () {

                }
            }
            preResult = result;
        }*/

        return null;
    }

    private int indexOf(String[] req_skills, String skill) {
        for (int index = 0; index < req_skills.length; index++) {
            String req_skill = req_skills[index];
            if (req_skill.equals(skill)) return index;
        }
        return -1;
    }
}