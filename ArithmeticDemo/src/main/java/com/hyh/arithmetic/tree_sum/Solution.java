package com.hyh.arithmetic.tree_sum;

/**
 * @author Administrator
 * @description
 * @data 2020/6/10
 */
public class Solution {


    public int pathSum(TreeNode root, int sum) {
        if (root == null) return 0;
        int result = dfs(root, 0, sum, 0);
        if (root.left != null) {
            result += pathSum(root.left, sum);
        }
        if (root.right != null) {
            result += pathSum(root.right, sum);
        }
        return result;
    }


    private int dfs(TreeNode node, int sum, int targetSum, int result) {
        if (node == null) return result;
        sum = node.val + sum;
        if (sum == targetSum) {
            result++;
        }
        if (node.left != null) {
            result = dfs(node.left, sum, targetSum, result);
        }

        if (node.right != null) {
            result = dfs(node.right, sum, targetSum, result);
        }
        return result;
    }
}
