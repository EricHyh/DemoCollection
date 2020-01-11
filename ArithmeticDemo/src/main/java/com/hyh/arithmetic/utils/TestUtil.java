package com.hyh.arithmetic.utils;

import android.util.Log;

import com.hyh.arithmetic.tabs.TabsManagerHelper;
import com.hyh.arithmetic.tabs.TabsManagerInfo;

import java.util.Arrays;
import java.util.List;

/**
 * @author Administrator
 * @description
 * @data 2020/1/6
 */
public class TestUtil {

    private static final String TAG = "TestUtil";

    public static void test1() {
        int[] array = {0, 1, 2, 3, 1, 0, 5, 3, 2, 1, 4};//5+6=11

        int leftNum = 0;
        int leftIndex = 0;
        int rightNum = 0;
        int rightIndex = 0;
        int lastNum = 0;
        int total = 0;


        for (int index = 0; index < array.length; index++) {
            int num = array[index];

            if (num >= lastNum) {
                if (index - leftIndex == 1) {
                    leftNum = num;
                    leftIndex = index;
                } else if ((leftIndex > 0 && rightIndex == 0) || index - rightIndex == 1) {
                    rightNum = num;
                    rightIndex = index;

                }
            }
            if (index == array.length - 1 || num < lastNum) {
                if (leftIndex > 0 && rightIndex > 0) {
                    //计算
                    int min = Math.min(leftNum, rightNum);
                    for (int i = leftIndex + 1; i < rightIndex; i++) {
                        total += (min - array[i]);
                    }
                    leftNum = rightNum;
                    leftIndex = rightIndex;
                    rightNum = 0;
                    rightIndex = 0;
                }
            }
            lastNum = num;
        }

        Log.d(TAG, "onCreate: total = " + total);
    }

    public static void test2() {

        /*{
            String[] originalTabs = {"a", "b", "c", "d", "e", "f", "g", "h", "i", "k", "l", "m", "n"};
            String[] usedTabs = {"a", "b", "c", "d", "e", "f", "g", "h", "i", "k", "l", "m", "n"};
            String[] moreTabs = {};

            List<String> originalTabList = Arrays.asList(originalTabs);
            TabsManagerInfo tabsManagerInfo = new TabsManagerInfo(Arrays.asList(usedTabs), Arrays.asList(moreTabs));

            TabsManagerInfo result = TabsManagerHelper.computeOriginalTabsManagerInfo(originalTabList, tabsManagerInfo);

            Log.d(TAG, "onCreate: ");
        }

        {
            String[] originalTabs = {"a", "b", "c", "d", "e", "f", "g", "h", "i", "k", "l", "m", "n"};
            String[] usedTabs = {"a", "b", "c", "d", "e", "f", "g", "h", "i", "k"};
            String[] moreTabs = {"l", "m", "n"};

            List<String> originalTabList = Arrays.asList(originalTabs);
            TabsManagerInfo tabsManagerInfo = new TabsManagerInfo(Arrays.asList(usedTabs), Arrays.asList(moreTabs));

            TabsManagerInfo result = TabsManagerHelper.computeOriginalTabsManagerInfo(originalTabList, tabsManagerInfo);

            Log.d(TAG, "onCreate: ");
        }

        {
            String[] originalTabs = {"a", "b", "c", "d", "e", "f", "g", "h", "i", "k", "l", "m", "n"};
            String[] usedTabs = {"a", "e", "f", "g", "h", "b", "c", "d", "i", "k"};
            String[] moreTabs = {"l", "m", "n"};

            List<String> originalTabList = Arrays.asList(originalTabs);
            TabsManagerInfo tabsManagerInfo = new TabsManagerInfo(Arrays.asList(usedTabs), Arrays.asList(moreTabs));

            TabsManagerInfo result = TabsManagerHelper.computeOriginalTabsManagerInfo(originalTabList, tabsManagerInfo);

            Log.d(TAG, "onCreate: ");
        }

        {
            String[] originalTabs = {"a", "b", "c", "d", "e", "f", "g", "h", "i", "k", "l", "m", "n"};
            String[] usedTabs = {"a", "e", "f", "g", "b", "c", "d", "i", "k"};
            String[] moreTabs = {"l", "n"};

            List<String> originalTabList = Arrays.asList(originalTabs);
            TabsManagerInfo tabsManagerInfo = new TabsManagerInfo(Arrays.asList(usedTabs), Arrays.asList(moreTabs));

            TabsManagerInfo result = TabsManagerHelper.computeOriginalTabsManagerInfo(originalTabList, tabsManagerInfo);

            Log.d(TAG, "onCreate: ");
        }*/


        /*{
            String[] originalTabs = {"a", "b", "c", "d", "e", "f", "g", "h", "i", "k", "l", "m", "n"};
            String[] usedTabs = {"a", "b", "c", "d", "e", "f", "g", "h", "i", "k", "l", "m", "n", "o", "p", "q"};
            String[] moreTabs = {};

            List<String> originalTabList = Arrays.asList(originalTabs);
            TabsManagerInfo tabsManagerInfo = new TabsManagerInfo(Arrays.asList(usedTabs), Arrays.asList(moreTabs));

            TabsManagerInfo result = TabsManagerHelper.computeOriginalTabsManagerInfo(originalTabList, tabsManagerInfo);

            Log.d(TAG, "onCreate: ");
        }

        {
            String[] originalTabs = {"a", "b", "c", "d", "e", "f", "g", "h", "i", "k", "l", "m", "n"};
            String[] usedTabs = {"a", "b", "d", "e", "f", "g", "i", "k", "l", "n", "o", "p", "q"};
            String[] moreTabs = {"c", "h", "m"};

            List<String> originalTabList = Arrays.asList(originalTabs);
            TabsManagerInfo tabsManagerInfo = new TabsManagerInfo(Arrays.asList(usedTabs), Arrays.asList(moreTabs));

            TabsManagerInfo result = TabsManagerHelper.computeOriginalTabsManagerInfo(originalTabList, tabsManagerInfo);

            Log.d(TAG, "onCreate: ");
        }

        {
            String[] originalTabs = {"a", "b", "c", "d", "e", "f", "g", "h", "i", "k", "l", "m", "n"};
            String[] usedTabs = {"a", "b", "c", "d", "e", "f", "g", "h", "i", "k", "l", "m", "n", "o", "p", "q"};
            String[] moreTabs = {"o", "p", "q"};

            List<String> originalTabList = Arrays.asList(originalTabs);
            TabsManagerInfo tabsManagerInfo = new TabsManagerInfo(Arrays.asList(usedTabs), Arrays.asList(moreTabs));

            TabsManagerInfo result = TabsManagerHelper.computeOriginalTabsManagerInfo(originalTabList, tabsManagerInfo);

            Log.d(TAG, "onCreate: ");
        }

        {
            String[] originalTabs = {"a", "b", "c", "d", "e", "f", "g", "h", "i", "k", "l", "m", "n"};
            String[] usedTabs = {"a", "e", "c", "i", "d", "f", "h", "k", "l", "m", "n", "o", "g", "p", "b", "q"};
            String[] moreTabs = {"o", "p", "q"};

            List<String> originalTabList = Arrays.asList(originalTabs);
            TabsManagerInfo tabsManagerInfo = new TabsManagerInfo(Arrays.asList(usedTabs), Arrays.asList(moreTabs));

            TabsManagerInfo result = TabsManagerHelper.computeOriginalTabsManagerInfo(originalTabList, tabsManagerInfo);

            Log.d(TAG, "onCreate: ");
        }


        {
            String[] originalTabs = {"a", "b", "c", "d", "e", "f", "g", "h", "i", "k", "l", "m", "n"};
            String[] usedTabs = {"a", "e", "c", "i", "d", "f", "h", "k", "l", "m", "n", "o", "g", "p", "b", "q"};
            String[] moreTabs = {};

            List<String> originalTabList = Arrays.asList(originalTabs);
            TabsManagerInfo tabsManagerInfo = new TabsManagerInfo(Arrays.asList(usedTabs), Arrays.asList(moreTabs));

            TabsManagerInfo result = TabsManagerHelper.computeOriginalTabsManagerInfo(originalTabList, tabsManagerInfo);

            Log.d(TAG, "onCreate: ");
        }*/

        /*{
            String[] originalTabs = {"a", "b", "c", "d", "e", "f", "g", "h", "i", "k", "l", "m", "n"};
            String[] usedTabs = {"a", "e", "c", "i", "d", "f", "h", "k", "l", "m", "n", "o", "g", "p", "b", "q"};
            String[] moreTabs = {"b", "g", "n", "q"};

            List<String> originalTabList = Arrays.asList(originalTabs);
            TabsManagerInfo tabsManagerInfo = new TabsManagerInfo(Arrays.asList(usedTabs), Arrays.asList(moreTabs));

            TabsManagerInfo result = TabsManagerHelper.computeOriginalTabsManagerInfo(originalTabList, tabsManagerInfo);

            Log.d(TAG, "onCreate: ");
        }*/


        /*{
            String[] originalTabs = {"a", "b", "c", "d", "e", "f", "g", "h", "i", "k", "l", "m", "n"};
            String[] usedTabs = {"b", "a", "c", "d", "e", "f", "g", "h", "i", "k", "l", "m", "n"};
            String[] moreTabs = {};

            List<String> originalTabList = Arrays.asList(originalTabs);
            TabsManagerInfo tabsManagerInfo = new TabsManagerInfo(Arrays.asList(usedTabs), Arrays.asList(moreTabs));

            TabsManagerInfo result = TabsManagerHelper.computeOriginalTabsManagerInfo(originalTabList, tabsManagerInfo);

            Log.d(TAG, "onCreate: ");
        }*/

        /*{
            String[] originalTabs = {"a", "b", "c", "d", "e", "f", "g", "h", "i", "k", "l", "m", "n"};
            String[] usedTabs = {"b", "c", "d", "e", "f", "g", "h", "i", "k", "l", "m", "n"};
            String[] moreTabs = {};

            List<String> originalTabList = Arrays.asList(originalTabs);
            TabsManagerInfo tabsManagerInfo = new TabsManagerInfo(Arrays.asList(usedTabs), Arrays.asList(moreTabs));

            TabsManagerInfo result = TabsManagerHelper.computeOriginalTabsManagerInfo(originalTabList, tabsManagerInfo);

            Log.d(TAG, "onCreate: ");
        }

        {
            String[] originalTabs = {"a", "b", "c", "d", "e", "f", "g", "h", "i", "k", "l", "m", "n"};
            String[] usedTabs = {"b", "c", "d", "e", "f", "g", "h", "i", "k", "l", "m", "n"};
            String[] moreTabs = {"a"};

            List<String> originalTabList = Arrays.asList(originalTabs);
            TabsManagerInfo tabsManagerInfo = new TabsManagerInfo(Arrays.asList(usedTabs), Arrays.asList(moreTabs));

            TabsManagerInfo result = TabsManagerHelper.computeOriginalTabsManagerInfo(originalTabList, tabsManagerInfo);

            Log.d(TAG, "onCreate: ");
        }*/


        {
            String[] originalTabs = {"a", "b", "c", "d", "e", "f", "g", "h", "i", "k", "l", "m", "n"};
            String[] usedTabs = {"1", "2", "3", "4", "5", "6", "7", "8", "9", "10", "11", "12"};
            String[] moreTabs = {};

            List<String> originalTabList = Arrays.asList(originalTabs);
            TabsManagerInfo tabsManagerInfo = new TabsManagerInfo(Arrays.asList(usedTabs), Arrays.asList(moreTabs));

            TabsManagerInfo result = TabsManagerHelper.computeOriginalTabsManagerInfo(originalTabList, tabsManagerInfo);

            Log.d(TAG, "onCreate: ");
        }

        {
            String[] originalTabs = {"a", "b", "c", "d", "e", "f", "g", "h", "i", "k", "l", "m", "n"};
            String[] usedTabs = {"1", "2", "3", "4", "5", "6", "7", "8", "9", "10", "11", "12"};
            String[] moreTabs = {"a", "b", "c"};

            List<String> originalTabList = Arrays.asList(originalTabs);
            TabsManagerInfo tabsManagerInfo = new TabsManagerInfo(Arrays.asList(usedTabs), Arrays.asList(moreTabs));

            TabsManagerInfo result = TabsManagerHelper.computeOriginalTabsManagerInfo(originalTabList, tabsManagerInfo);

            Log.d(TAG, "onCreate: ");
        }

        {
            String[] originalTabs = {"a", "b", "c", "d", "e", "f", "g", "h", "i", "k", "l", "m", "n"};
            String[] usedTabs = {"1", "2", "3", "4", "b", "6", "7", "8", "h", "10", "11", "12"};
            String[] moreTabs = {"a", "b", "c"};

            List<String> originalTabList = Arrays.asList(originalTabs);
            TabsManagerInfo tabsManagerInfo = new TabsManagerInfo(Arrays.asList(usedTabs), Arrays.asList(moreTabs));

            TabsManagerInfo result = TabsManagerHelper.computeOriginalTabsManagerInfo(originalTabList, tabsManagerInfo);

            Log.d(TAG, "onCreate: ");
        }

        {
            String[] originalTabs = {"a", "b", "c", "d", "e", "f", "g", "h", "i", "k", "l", "m", "n"};
            String[] usedTabs = {"1", "2", "a", "4", "b", "6", "7", "8", "h", "10", "11", "12"};
            String[] moreTabs = {};

            List<String> originalTabList = Arrays.asList(originalTabs);
            TabsManagerInfo tabsManagerInfo = new TabsManagerInfo(Arrays.asList(usedTabs), Arrays.asList(moreTabs));

            TabsManagerInfo result = TabsManagerHelper.computeOriginalTabsManagerInfo(originalTabList, tabsManagerInfo);

            Log.d(TAG, "onCreate: ");
        }
    }
}
