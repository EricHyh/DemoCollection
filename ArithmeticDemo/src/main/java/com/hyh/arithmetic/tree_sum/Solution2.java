package com.hyh.arithmetic.tree_sum;

import android.annotation.SuppressLint;
import android.util.SparseArray;
import android.util.SparseIntArray;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Administrator
 * @description
 * @data 2020/6/10
 */
@SuppressLint("UseSparseArrays")
public class Solution2 {


    public int pathSum(TreeNode root, int sum) {
        Map<Integer, Integer> prefixSumCount = new HashMap<>();
        prefixSumCount.put(0, 1);
        return recursionPathSum(prefixSumCount, root, 0, sum);
    }

    public int recursionPathSum(Map<Integer, Integer> prefixSumCount, TreeNode node, int sum, int target) {
        if (node == null) return 0;

        int result = 0;

        sum += node.val;

        result = prefixSumCount.getOrDefault(sum - target, 0);

        Integer sumCount = prefixSumCount.getOrDefault(sum, 0);
        prefixSumCount.put(sum, sumCount + 1);

        result += recursionPathSum(prefixSumCount, node.left, sum, target);
        result += recursionPathSum(prefixSumCount, node.right, sum, target);

        prefixSumCount.put(sum, sumCount);

        return result;
    }
}