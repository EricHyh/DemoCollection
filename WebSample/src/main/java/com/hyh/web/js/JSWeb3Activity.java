package com.hyh.web.js;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.View;
import android.webkit.JavascriptInterface;
import android.webkit.WebView;

import com.hyh.web.R;
import com.hyh.web.widget.web.BaseWebViewClient;
import com.hyh.web.widget.web.CustomWebView;
import com.hyh.web.widget.web.WebClient;

/**
 * @author Administrator
 * @description
 * @data 2020/7/13
 */
public class JSWeb3Activity extends Activity {

    public static void start(Context context) {
        Intent intent = new Intent(context, JSWeb3Activity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(intent);
    }

    private static final String TAG = "JSWeb_";

    private WebClient mWebClient;
    private CustomWebView mWebView;

    private View mDiv_view;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_js_web);
        mWebView = findViewById(R.id.web_view);
        mWebClient = new WebClient(getApplicationContext(), mWebView, null, null, null);

        mWebView.addJavascriptInterface(new ListenerHeight(), "offsetYListener");

        mWebClient.setWebViewClient(new BaseWebViewClient() {

            @Override
            public void onPageStarted(WebView view, String url, Bitmap favicon) {
                super.onPageStarted(view, url, favicon);

                String insertDivFun = "function insertDiv(tagName, tagClass, height, id){\n" +
                        "\n" +
                        "        var tagElements = document.getElementsByTagName( tagName );\n" +
                        "\n" +
                        "        var newElement = document.createElement(\"div\");\n" +
                        "        newElement.id = id;\n" +
                        "        newElement.style.height = height;\n" +
                        "\n" +
                        "        for( var m = 0 ; m < tagElements.length ; m++ ){\n" +
                        "            if( tagElements[m].className == tagClass ){\n" +
                        "                var targetElement = tagElements[m];\n" +
                        "\n" +
                        "                var parent = targetElement.parentNode;\n" +
                        "\n" +
                        "                if( parent.lastChild == targetElement ){\n" +
                        "                    parent.appendChild( newElement, targetElement );\n" +
                        "                }else{\n" +
                        "                    parent.insertBefore( newElement, targetElement.nextSibling );\n" +
                        "                };\n" +
                        "\n" +
                        "               break;\n" +
                        "            }\n" +
                        "        }\n" +
                        "    }";


                String setHeightByIdFun = "function setHeightById(tagName, tagId, height){\n" +
                        "        var element = document.querySelector(\"#\" + tagId);\n" +
                        "        element.style.height = height;\n" +
                        "    }";


                String getOffsetYFun = "function getOffsetY(tagId){\n" +
                        "        var element = document.querySelector(\"#\" + tagId);\n" +
                        "        \n" +
                        "        var actualTop = element.offsetTop;\n" +
                        "        var current = element.offsetParent;\n" +
                        "        while (current !== null){\n" +
                        "            actualTop += (current.offsetTop+current.clientTop);\n" +
                        "            current = current.offsetParent;\n" +
                        "        }\n" +
                        "\n" +
                        "        return actualTop;\n" +
                        "    }";


                String listenerFun = "function listenerOffsetY(tagId){\n" +
                        "        window.offsetYListener.onOffsetYChanged(getOffsetY(tagId));\n" +
                        "        var element = document.querySelector(\"#\" + tagId);\n" +
                        "        var observer = new MutationObserver(function(mutations){\n" +
                        "            mutations.forEach(function(mutation){\n" +
                        "                window.offsetYListener.onOffsetYChanged(getOffsetY(tagId));\n" +
                        "            });\n" +
                        "        });\n" +
                        "        observer.observe(document.body, {\n" +
                        "            attributes: true, childList: true, subtree: true\n" +
                        "        });\n" +
                        "    }";


                String removeTags = "function removeTags(tagName,tagClass){\n" +
                        "            var tagElements = document.getElementsByTagName( tagName );\n" +
                        "            for( var m = 0 ; m < tagElements.length ; m++ ){\n" +
                        "                if( tagElements[m].className == tagClass ){\n" +
                        "                    tagElements[m].parentNode.removeChild( tagElements[m] );\n" +
                        "                }\n" +
                        "            }\n" +
                        "        }";


                mWebView.loadUrl("javascript:" + insertDivFun);
                mWebView.loadUrl("javascript:" + setHeightByIdFun);
                mWebView.loadUrl("javascript:" + getOffsetYFun);
                mWebView.loadUrl("javascript:" + listenerFun);
                mWebView.loadUrl("javascript:" + removeTags);
            }

            @Override
            public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view, url);


                /*mWebView.loadUrl("javascript:insertDiv(\"div\",\"page-info container\",\"120px\",\"test_div_top\")");

                mWebView.evaluateJavascript("javascript:insertDiv(\"div\",\"module-header-default container top-border bottom-border\",\"120px\"," +
                        "\"test_div_bottom\")", new ValueCallback<String>() {
                    @Override
                    public void onReceiveValue(String value) {
                        Log.d(TAG, "insertDiv: " + value);

                        mWebView.evaluateJavascript("javascript:getOffsetY(\"test_div_bottom\")", new ValueCallback<String>() {


                            @Override
                            public void onReceiveValue(String value) {
                                final float density = getApplicationContext().getResources().getDisplayMetrics().density;

                                Log.d(TAG, "getOffsetY: " + value);
                                mDiv_view = new View(getApplicationContext());
                                mDiv_view.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, (int) (120 * density)));
                                mDiv_view.setBackgroundColor(Color.RED);

                                mDiv_view.setTranslationY(Integer.parseInt(value) * density);

                                mWebView.addView(mDiv_view);

                                mWebView.loadUrl("javascript:listenerOffsetY(\"test_div_bottom\")");
                            }
                        });


                    }
                });*/
            }
        });

        //mWebClient.loadUrl("https://jumpluna.58.com/i/LZYBeQ6a1luDubj");
        mWebClient.loadUrl("https://cpu.baidu.com/api/1022/ffa1f96f/detail/73185020146/video?aid=uSxcdqPxx93wA9bsZwaJt-A6zmf42iSsAvICnpsUW86DQ9ihHu3AGjJGDQC6EPdBB1UUVUXQe5IN1EwacepypjnvOqQrlHNNJfNx4NNBPPn8BG0OGQeKkooU0JyZVXsyT1bfZC5VBnmYAc_yXzx0sKwUu8VSfHaZk5GOOl2B6AXiO1ui-Cp1aAyYB-Uy0Oy-I8sm63n13o8iVONvbOP0ye5ZlOxHvChggajUyDajhnD1S1MQbo7xKRsNWzyfD5l2GohNQo7w6pB6IQUxL3i6aVTbevao10sqGI5Nph39t8n2P_29apluIJJjOQs4xGwNQ8_uRXBZmaGX2EtADxw-vw&scene=0&log_id=1594634373067a46bc63afbfada03837&exp_infos=20406_20515_20532_20542_20554_20603_20623_20651_20662_20901_20984_21500_21501_22116_22641_25311_26451_29578_29722_20010_8104301_8104903_8800030_7000010_8104204_8300062_8800012_8800022_8200139_8800000_8002400_8190602_8190802_8200142_8104301_8104903_8800030_7000010_8104204_8300062_8800012_8800022_8200139_8800000_8002400_8190602_8190802_8200142_41100_40001_43302_44161_44201_44212_44221_44235_44244_44264_44274_44281_44293_8200139_8190307_8103900_8104301_8104903_8800030_7000010_8104204_8800012_8800022_8200139_8800000_8002400_8190602_8190802_8200142&no_list=1&forward=api&api_version=2&cds_session_id=70da31002836469aba4fa84af8af7faa&last_pv_id=API1594634373067a46bc63afbfada03837&cpu_union_id=ADID_ef98a1ccbb761a257a1bda9f357426b4&uls=&rt=107&rts=65536");
    }

    @Override
    public void onBackPressed() {
        if (mWebClient == null || !mWebClient.onBackPressed()) {
            super.onBackPressed();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mWebClient != null) {
            mWebClient.destroy();
        }
    }

    private int height = 120;

    public void test(View view) {
        height += 5;
        //mWebView.loadUrl("javascript:setHeightById(\"div\",\"test_div_top\",\"" + height + "px\")");

        mWebView.loadUrl("javascript:removeTags(\"div\",\"video-layout news-wrapper recommend-layout video-ad-style\")");
    }


    private class ListenerHeight {

        @JavascriptInterface
        public void onHeightChanged(String s) {
            Log.d(TAG, "onHeightChanged: " + s);
        }

        @JavascriptInterface
        public void onOffsetYChanged(String height) {
            Log.d(TAG, "onOffsetYChanged: " + height);
            if (mDiv_view != null) {
                final float density = getApplicationContext().getResources().getDisplayMetrics().density;
                mDiv_view.setTranslationY(Integer.parseInt(height) * density);
            }
        }
    }
}