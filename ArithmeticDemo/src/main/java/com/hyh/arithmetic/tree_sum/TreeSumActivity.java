package com.hyh.arithmetic.tree_sum;

import android.app.Activity;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

/**
 * @author Administrator
 * @description
 * @data 2020/6/10
 */
public class TreeSumActivity extends Activity {

    private static final String TAG = "TreeSumActivity_";

    /**
     * 5
     * / \
     * 4   8
     * /   / \
     * 11  13  4
     * /  \    / \
     * 7    2  5   1
     */

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Button button = new Button(this);
        button.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        button.setText("测试");
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                TreeNode root = new TreeNode(5);
                root.left = new TreeNode(4);
                root.right = new TreeNode(8);
                root.left.left = new TreeNode(11);
                root.right.left = new TreeNode(13);
                root.right.right = new TreeNode(4);

                root.left.left.left = new TreeNode(7);
                root.left.left.right = new TreeNode(2);

                root.right.right.left = new TreeNode(5);
                root.right.right.right = new TreeNode(1);

                int result1 = new Solution().pathSum(root, 22);

                /*[1]
                0*/


                int result2 = new Solution().pathSum(new TreeNode(1), 0);


                /**
                 * [-2,null,-3]
                 * -5
                 */
                TreeNode treeNode = new TreeNode(-2);
                treeNode.right = new TreeNode(-3);
                int result3 = new Solution().pathSum(treeNode, -5);


                Log.d(TAG, "onClick: ");

            }
        });
        setContentView(button);
    }

}
