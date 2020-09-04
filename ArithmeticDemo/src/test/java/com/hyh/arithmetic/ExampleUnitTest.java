package com.hyh.arithmetic;

import org.junit.Test;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import static org.junit.Assert.assertEquals;

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
public class ExampleUnitTest {
    @Test
    public void addition_isCorrect() {
        assertEquals(4, 2 + 2);
    }

    public List<int[]> find(int[] array, int targetSum) {
        List<int[]> result = new ArrayList<>();

        //Map<Integer, LinkedList<Integer>> diffMap = new HashMap<>();

        for (int i = 0; i < array.length; i++) {
            int num = array[i];

        }
        return result;
    }

    public String multiply(String num1, String num2) {
        LinkedList<Integer> num_list1 = new LinkedList<>();

    }

    public LinkedList<Integer> convert(String num1) {
        LinkedList<Integer> list = new LinkedList<>();
        for (int i = num1.length() - 1; i >= 0; i--) {
            int num = num1.charAt(i) - '0';
            list.add(num);
        }
        return list;
    }

    public String multiply(LinkedList<Integer> num1, LinkedList<Integer> num2) {
        Iterator<Integer> iterator1 = num1.iterator();
        while (iterator1.hasNext()) {
            Integer integer1 = iterator1.next();
        }
    }

    public void multiply(int num, int place, LinkedList<Integer> numList) {
        while () {
            Integer integer = numList.pollFirst();
        }
        for (int i = 1; i < place; i++) {
            numList.addFirst(0);
        }
    }
}