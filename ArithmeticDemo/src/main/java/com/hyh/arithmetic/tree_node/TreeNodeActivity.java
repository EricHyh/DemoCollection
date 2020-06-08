package com.hyh.arithmetic.tree_node;

import android.app.Activity;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import java.util.List;

/**
 * @author Administrator
 * @description
 * @data 2020/6/8
 */
public class TreeNodeActivity extends Activity {

    private static final String TAG = "TreeNodeActivity_";


    /**
     * [0,1,null,3,2]
     * 2
     * 1
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
                //[3,5,1,6,2,0,8,null,null,7,4];
                //TreeNode node = TreeNode.create(new Integer[]{3, 5, 1, 6, 2, 0, 8, null, null, 7, 4});

                TreeNode node = TreeNode.create(new Integer[]{0, 1, null, 3, 2});

                List<Integer> values = new Solution().distanceK(node, node.left.right, 1);
                Log.d(TAG, "onClick: ");
            }
        });
        setContentView(button);
    }
}
