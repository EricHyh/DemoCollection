package com.hyh.arithmetic.search_words;

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
public class SearchWordsActivity extends Activity {

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
                char[][] chars = new char[7][4];
                chars[0] = new char[]{'a', 'a', 'a', 'a'};
                chars[1] = new char[]{'a', 'a', 'a', 'a'};
                chars[2] = new char[]{'a', 'a', 'a', 'a'};
                chars[3] = new char[]{'a', 'a', 'a', 'a'};
                chars[4] = new char[]{'b', 'c', 'd', 'e'};
                chars[5] = new char[]{'f', 'i', 'j', 'k'};
                chars[6] = new char[]{'l', 'm', 'n', 'o'};

                String[] strings = new String[]{"aaaaaaaab", "aaaaaaaac", "aaaaaaaad", "aaaaaaaae"
                        , "aaaaaaaaf", "aaaaaaaai", "aaaaaaaaj", "aaaaaaaak"
                        , "aaaaaaaal", "aaaaaaaam", "aaaaaaaan", "aaaaaaaao"
                        , "aaaaaaaaab", "aaaaaaaabc", "aaaaaaaacd", "aaaaaaade"
                        , "aaaaaaaaef", "aaaaaaaafi", "aaaaaaaaij", "aaaaaaakl"
                        , "aaaaaaaalm", "aaaaaaaamn", "aaaaaaaano", "aaaaaaaoa"
                        , "aaaaaaaabf", "aaaaaaaaed", "aaaaaaaadj", "aaaaaaacb"
                };
                List<String> words = new Solution5().findWords(chars, strings);
                Log.d(TAG, "onClick: ");
            }
        });
        setContentView(button);
    }
}
