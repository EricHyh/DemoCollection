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


    public static class TreeNode {
        int val;
        TreeNode left;
        TreeNode right;

        TreeNode(int x) {
            val = x;
        }
    }

    int maxSum = Integer.MIN_VALUE;

    private int maxGain(TreeNode node) {
        if (node == null) return 0;

        int leftMaxGain = Math.max(maxGain(node.left), 0);
        int rightMaxGain = Math.max(maxGain(node.right), 0);

        int sum = node.val + leftMaxGain + rightMaxGain;

        maxSum = Math.max(maxSum, sum);

        return node.val + Math.max(leftMaxGain, rightMaxGain);
    }

    public ListNode mergeKLists(ListNode[] lists) {
        if (lists == null || lists.length <= 0) return null;
        if (lists.length == 1) return lists[0];
        ListNode result = lists[0];
        for (int i = 1; i < lists.length; i++) {
            result = merge(result, lists[i]);
        }
        return result;
    }


    public ListNode merge(ListNode node1, ListNode node2) {
        if (node1 == null) return node2;
        if (node2 == null) return node1;
        if (node1.val > node2.val) {
            node2.next = merge(node1, node2.next);
            return node2;
        } else {
            node1.next = merge(node1.next, node2);
            return node1;
        }
    }


    /**
     * 输入: [3,2,3,1,2,4,5,5,6] 和 k = 4
     * 输出: 4
     */
    public int findKthLargest(int[] nums, int k) {

        return 0;
    }


    public void sort(int[] nums, int k) {
        findIndex(nums, 0, nums.length - 1);
    }


    public int findIndex(int[] nums, int start, int end) {
        int targetIndex = start;
        int target = nums[start];
        boolean pos = true;
        while (start < end) {
            if (pos) {
                int endNum = nums[end];
                if (target > endNum) {
                    nums[targetIndex] = endNum;
                    nums[end] = target;
                    targetIndex = end;
                    start++;
                    pos = false;
                } else {
                    end--;
                }
            } else {
                int startNum = nums[start];
                if (target < startNum) {
                    nums[targetIndex] = startNum;
                    nums[end] = target;
                    targetIndex = start;
                    end--;
                    pos = true;
                } else {
                    start++;
                }
            }
        }
        return targetIndex;
    }





    /*public int findIndex(int[] kArray, int size, int target) {
        if (size == 0) return 0;
        int start = 0;
        int end = size;
        int cursor = size / 2;
        while (cursor > start && cursor < end) {
            int num = kArray[cursor];
            if (num == target) return cursor;
            if (num > target) {
                end = cursor;
                cursor = start + (end - start) / 2;
                if (cursor == start) return start;
            } else {
                start = cursor;
                cursor = start + (end - start) / 2;
                if (cursor == start) return end;
            }
        }
        return cursor;
    }*/


    //[[1,4,5],[1,3,4],[2,6]]
    /*public static ListNode merge(ListNode node1, ListNode node2) {
        if (node1 == null) return node2;
        if (node2 == null) return node1;

        ListNode root = node1;
        ListNode parentNode1 = null;

        ListNode nextNode1 = node1;
        ListNode nextNode2 = node2;
        while (true) {
            if (nextNode1.val < nextNode2.val) {
                if (nextNode1.next == null) {
                    nextNode1.next = nextNode2;
                    break;
                } else {
                    parentNode1 = nextNode1;
                    nextNode1 = nextNode1.next;
                }
            } else {
                ListNode curNode2 = nextNode2;
                nextNode2 = nextNode2.next;
                curNode2.next = nextNode1;
                if (parentNode1 == null) {
                    parentNode1 = curNode2;
                    root = parentNode1;
                } else {
                    parentNode1.next = curNode2;
                    parentNode1 = curNode2;
                }
                if (nextNode2 == null) break;
            }
        }
        return root;
    }*/


}