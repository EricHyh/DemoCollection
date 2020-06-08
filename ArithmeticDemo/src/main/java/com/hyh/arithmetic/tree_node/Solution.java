package com.hyh.arithmetic.tree_node;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Administrator
 * @description
 * @data 2020/6/8
 */
public class Solution {


    public List<Integer> distanceK(TreeNode root, TreeNode target, int K) {
        List<Integer> values = new ArrayList<>();
        if (K == 0) {
            values.add(target.val);
            return values;
        }
        if (root.val == target.val) {
            collectNodeValue(target, K, values);
            return values;
        }

        TreeNode targetParent = null;


        TreeNode cur = target;
        boolean isLeftEmpty = false;

        while (true) {
            TreeNode parent = findParent(root, cur);

            boolean isLeft;
            if (parent.left != null && parent.left.val == cur.val) {
                parent.left = null;
                isLeft = true;
            } else {
                parent.right = null;
                isLeft = false;
            }
            if (targetParent == null) {
                targetParent = parent;
                if (parent.val == root.val) break;
                cur = parent;
                isLeftEmpty = isLeft;
            } else {
                if (isLeftEmpty) {
                    cur.left = parent;
                } else {
                    cur.right = parent;
                }
                if (parent.val == root.val) break;
                cur = parent;
                isLeftEmpty = isLeft;
            }
        }

        collectNodeValue(target, K, values);
        collectNodeValue(targetParent, K - 1, values);

        return values;
    }


    private TreeNode findParent(TreeNode node, TreeNode target) {
        if (node.left != null) {
            if (node.left.val == target.val) {
                return node;
            }
            TreeNode parent = findParent(node.left, target);
            if (parent != null) {
                return parent;
            }
        }
        if (node.right != null) {
            if (node.right.val == target.val) {
                return node;
            }
            return findParent(node.right, target);
        }
        return null;
    }

    private void collectNodeValue(TreeNode node, int depth, List<Integer> values) {
        if (node == null) return;
        if (depth == 0) {
            values.add(node.val);
            return;
        }
        if (node.left != null) {
            collectNodeValue(node.left, depth - 1, values);
        }
        if (node.right != null) {
            collectNodeValue(node.right, depth - 1, values);
        }
    }
}