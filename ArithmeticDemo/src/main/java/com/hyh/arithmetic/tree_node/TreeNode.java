package com.hyh.arithmetic.tree_node;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Administrator
 * @description
 * @data 2020/6/8
 */
public class TreeNode {

    int val;
    TreeNode left;
    TreeNode right;

    TreeNode(int x) {
        val = x;
    }


    //[3,5,1,6,2,0,8,null,null,7,4];
    public static TreeNode create(Integer[] array) {
        TreeNode root = new TreeNode(array[0]);
        List<TreeNode> nextList = new ArrayList<>();
        nextList.add(root);
        int index = 1;
        while (nextList.size() > 0 && index < array.length) {
            TreeNode node = nextList.remove(0);
            Integer value = array[index++];
            node.left = value == null ? null : new TreeNode(value);
            if (index > array.length) {
                break;
            }
            value = array[index++];
            node.right = value == null ? null : new TreeNode(value);
            if (node.left != null) {
                nextList.add(node.left);
            }
            if (node.right != null) {
                nextList.add(node.right);
            }
        }
        return root;
    }

    public static Integer[] toArray(TreeNode root) {
        List<Integer> values = new ArrayList<>();
        List<TreeNode> nextList = new ArrayList<>();
        int notNullSize = 1;
        nextList.add(root);
        while (notNullSize > 0) {
            TreeNode node = nextList.remove(0);
            if (node == null) {
                values.add(null);
                continue;
            }
            notNullSize--;
            values.add(node.val);
            boolean isLeftNull = true;
            if (node.left != null) {
                nextList.add(node.left);
                notNullSize++;
                isLeftNull = false;
            }
            if (node.right != null) {
                if (isLeftNull) {
                    nextList.add(null);
                }
                nextList.add(node.right);
                notNullSize++;
            } else {
                if (isLeftNull && notNullSize > 0) {
                    nextList.add(null);
                }
                if (!isLeftNull || notNullSize > 0) {
                    nextList.add(null);
                }
            }
        }
        return values.toArray(new Integer[0]);
    }
}