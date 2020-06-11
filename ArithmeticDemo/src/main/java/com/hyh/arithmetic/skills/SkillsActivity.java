package com.hyh.arithmetic.skills;

import android.app.Activity;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class SkillsActivity extends Activity {

    private static final String TAG = "SkillsActivity_";

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Button button = new Button(this);
        button.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        button.setText("测试");
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String[] req_skills1 = {"java", "nodejs", "reactjs"};
                List<List<String>> people1 = new ArrayList<>();
                people1.add(Collections.singletonList("java"));
                people1.add(Collections.singletonList("nodejs"));
                people1.add(Arrays.asList("nodejs", "reactjs"));

                //int[] ints1 = new Solution1().smallestSufficientTeam(req_skills1, people1);
                /**
                 * ["algorithms","math","java","reactjs","csharp","aws"]
                 * [["algorithms","math","java"],
                 * ["algorithms","math","reactjs"],
                 * ["java","csharp","aws"],[
                 * "reactjs","csharp"],
                 * ["csharp","math"],
                 * ["aws","java"]]
                 */

                /*String[] req_skills2 = {"algorithms", "math", "java", "reactjs", "csharp", "aws"};
                List<List<String>> people2 = new ArrayList<>();
                people2.add(Arrays.asList("algorithms", "math", "java"));
                people2.add(Arrays.asList("algorithms", "math", "reactjs"));
                people2.add(Arrays.asList("java", "csharp", "aws"));
                people2.add(Arrays.asList("reactjs", "csharp"));
                people2.add(Arrays.asList("csharp", "math"));
                people2.add(Arrays.asList("aws", "java"));*/


                String[] req_skills2 = {"mmcmnwacnhhdd","vza","mrxyc"};
                List<List<String>> people2 = new ArrayList<>();
                people2.add(Collections.singletonList("mmcmnwacnhhdd"));
                people2.add(new ArrayList<>());
                people2.add(new ArrayList<>());
                people2.add(Arrays.asList("vza", "mrxyc"));

                /**
                 * ["mmcmnwacnhhdd","vza","mrxyc"]
                 * [["mmcmnwacnhhdd"],[],[],["vza","mrxyc"]]
                 */
                int[] ints2 = new Solution1().smallestSufficientTeam(req_skills2, people2);
                Log.d(TAG, "onClick: ");
            }
        });
        setContentView(button);
    }
}
