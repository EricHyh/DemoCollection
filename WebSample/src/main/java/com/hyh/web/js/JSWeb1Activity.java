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
public class JSWeb1Activity extends Activity {

    public static void start(Context context) {
        Intent intent = new Intent(context, JSWeb1Activity.class);
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


                String insertImgFun = "function insertImage(tagName, tagClass, id){\n" +
                        "\n" +
                        "        window.offsetYListener.onTest(\"insertImage\");\n" +
                        "\n" +
                        "        var tagElements = document.getElementsByTagName( tagName );\n" +
                        "\n" +
                        "        var newElement = document.createElement(\"img\");\n" +
                        "        newElement.id = id;\n" +
                        "        newElement.src = \"https://publish-pic-cpu.baidu.com/e640b3b6-9295-419b-8565-e322007cdc1d.jpeg@f_webp|q_90\";\n" +
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


                String findElementsFun = "function findElements(tagName, className){\n" +
                        "        if(document.getElementsByClassName){\n" +
                        "            return document.getElementsByClassName(classname);\n" +
                        "        }else{\n" +
                        "            var results = new Array();\n" +
                        "            var elements = document.getElementsByTagName(tagName);\n" +
                        "            if(elements){\n" +
                        "                for(var i = 0; i < elements.length; i++){\n" +
                        "                    if(elements[i].className == className){\n" +
                        "                        results[results.length] = elements[i];\n" +
                        "                    }\n" +
                        "                }\n" +
                        "            }\n" +
                        "            return results;\n" +
                        "        }\n" +
                        "    }";


                String listenerElementFun = "function listenerElement(){\n" +
                        "        window.offsetYListener.onElement(1);\n" +
                        "        var observer = new MutationObserver(function(mutations){\n" +
                        "            window.offsetYListener.onElement(100);\n" +
                        "            mutations.forEach(function(mutation){\n" +
                        "                window.offsetYListener.onElement(101);\n" +
                        "                var elements = findElements(\"div\", \"recommend-layout\");\n" +
                        "                if(elements && elements.length > 0){\n" +
                        "                    window.offsetYListener.onElement(elements.length);\n" +
                        "                }else{" +
                        "                    window.offsetYListener.onElement(0);\n" +
                        "                }\n" +
                        "            });\n" +
                        "        });\n" +
                        "        observer.observe(document.body, {\n" +
                        "            attributes: true, childList: true, subtree: true\n" +
                        "        });\n" +
                        "    }";


                mWebView.loadUrl("javascript:" + insertDivFun);
                mWebView.loadUrl("javascript:" + insertImgFun);
                mWebView.loadUrl("javascript:" + setHeightByIdFun);
                mWebView.loadUrl("javascript:" + getOffsetYFun);
                mWebView.loadUrl("javascript:" + listenerFun);

                mWebView.loadUrl("javascript:" + findElementsFun);
                mWebView.loadUrl("javascript:" + listenerElementFun);




                String script1 = "<script>\n" +
                        "        var style = document.createElement(\"style\");\n" +
                        "        style.type = \"text/css\";\n" +
                        "        try{\n" +
                        "            style.appendChild(document.createTextNode(\"div.news-layout.news-wrapper.recommend-layout, div.module-header-default.container.top-border.bottom-border,div.detail-source.container{display: none!important;}\"));\n" +
                        "        }catch(ex){\n" +
                        "            style.styleSheet.cssText=\"div.news-layout.news-wrapper.recommend-layout, div.module-header-default.container.top-border.bottom-border,div.detail-source.container{display: none!important;}\";//针对IE\n" +
                        "        }\n" +
                        "        var head = document.getElementsByTagName(\"head\")[0];\n" +
                        "        head.appendChild(style);\n" +
                        "</script>";

                //mWebView.loadDataWithBaseURL(null, script, "text/html", "utf-8", null);


                String script2 = "var style = document.createElement(\"style\");\n" +
                        "        style.type = \"text/css\";\n" +
                        "        try{\n" +
                        "            style.appendChild(document.createTextNode(\"div.news-layout.news-wrapper.recommend-layout, div.module-header-default.container.top-border.bottom-border,div.detail-source.container{display: none!important;}\"));\n" +
                        "        }catch(ex){\n" +
                        "            style.styleSheet.cssText=\"div.news-layout.news-wrapper.recommend-layout, div.module-header-default.container.top-border.bottom-border,div.detail-source.container{display: none!important;}\";//针对IE\n" +
                        "        }\n" +
                        "        var head = document.getElementsByTagName(\"head\")[0];\n" +
                        "        head.appendChild(style);";





                /*mWebView.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        String js = "var newscript = document.createElement(\"script\");";
                        js += "window.offsetYListener.onElement(120);\n" +
                                "function isHeadNull(){var head=document.getElementsByTagName(\"head\")[0];if(head&&head.firstChild){creatStyle()}else{setTimeout(function(){isHeadNull()},10)}}function creatStyle(){var style=document.createElement(\"style\");style.type=\"text/css\";style.appendChild(document.createTextNode(\"div.news-layout.news-wrapper.recommend-layout,div.module-header-default.container.top-border.bottom-border,div.detail-source.container{display: none!important;}\"));var head=document.getElementsByTagName(\"head\")[0];head.insertBefore(style,head.firstChild)};setTimeout(function(){isHeadNull()},10);\n" +
                                "window.offsetYListener.onElement(121);";
                        js += "document.body.appendChild(newscript);";



                        mWebView.loadUrl("javascript:" + js);
                    }
                },10);*/







            }

            @Override
            public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view, url);
                //mWebView.loadUrl("javascript:listenerElement()");

                //String imgUrl = "https://goss.veer.com/creative/vcg/veer/1600water/veer-302386254.jpg";
                String imgUrl = "https://publish-pic-cpu.baidu.com/e640b3b6-9295-419b-8565-e322007cdc1d.jpeg@f_webp|q_90";


                //mWebView.loadUrl("javascript:insertDiv(\"div\",\"page-info container\",\"120px\",\"test_div_top\")");

                /*mWebView.evaluateJavascript("javascript:insertImage(\"div\",\"page-info container\",\"test_div_top\")", new ValueCallback<String>() {
                    @Override
                    public void onReceiveValue(String value) {
                        mWebView.evaluateJavascript("javascript:getOffsetY(\"test_div_top\")", new ValueCallback<String>() {


                            @Override
                            public void onReceiveValue(String value) {
                                final float density = getApplicationContext().getResources().getDisplayMetrics().density;
                                Log.d(TAG, "getOffsetY test_div_top: " + value);
                            }
                        });
                    }
                });
*/
                /*mWebView.evaluateJavascript("javascript:insertDiv(\"div\",\"module-header-default container top-border bottom-border\",\"120px\"," +
                        "\"test_div_bottom\")", new ValueCallback<String>() {
                    @Override
                    public void onReceiveValue(String value) {
                        Log.d(TAG, "insertDiv: " + value);

                        mWebView.evaluateJavascript("javascript:getOffsetY(\"test_div_bottom\")", new ValueCallback<String>() {


                            @Override
                            public void onReceiveValue(String value) {
                                final float density = getApplicationContext().getResources().getDisplayMetrics().density;

                                Log.d(TAG, "getOffsetY: " + value);
                                mDiv_view = new View(getApplicationContext()){
                                    @Override
                                    public boolean onTouchEvent(MotionEvent event) {

                                        return super.onTouchEvent(event);
                                    }
                                };
                                mDiv_view.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, (int) (120 * density)));
                                mDiv_view.setBackgroundColor(Color.RED);

                                mDiv_view.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {
                                        Toast.makeText(JSWeb1Activity.this, "被点击了", Toast.LENGTH_SHORT).show();
                                    }
                                });





                                mDiv_view.setTranslationY(Integer.parseInt(value) * density);

                                mWebView.addView(mDiv_view);

                                mWebView.loadUrl("javascript:listenerOffsetY(\"test_div_bottom\")");
                            }
                        });


                    }
                });*/


                String js = "var newscript = document.createElement(\"script\");";
                js += "        var style = document.createElement(\"style\");\n" +
                        "        style.type = \"text/css\";\n" +
                        "        try{\n" +
                        "            style.appendChild(document.createTextNode(\"div.news-layout.news-wrapper.recommend-layout,\n" +
                        "div.module-header-default.container.top-border.bottom-border,\n" +
                        "div.playlist-layout.no-gap.news-wrapper.recommend-layout.video-recommend-layout,\n" +
                        "div.new-video-layout,\n" +
                        "div.page-widget,\n" +
                        "div.detail-source.container{\n" +
                        "    display: none!important;\n" +
                        "}\"));\n" +
                        "        }catch(ex){\n" +
                        "            style.styleSheet.cssText=\"div.news-layout.news-wrapper.recommend-layout,\n" +
                        "div.module-header-default.container.top-border.bottom-border,\n" +
                        "div.playlist-layout.no-gap.news-wrapper.recommend-layout.video-recommend-layout,\n" +
                        "div.new-video-layout,\n" +
                        "div.page-widget,\n" +
                        "div.detail-source.container{\n" +
                        "    display: none!important;\n" +
                        "}\";\n" +
                        "        }\n" +
                        "        var head = document.getElementsByTagName(\"head\")[0];\n" +
                        "        head.appendChild(style);";
                js += "document.body.appendChild(newscript);";



                mWebView.loadUrl("javascript:" + js);

            }
        });

        //mWebClient.loadUrl("https://jumpluna.58.com/i/LZYBeQ6a1luDubj");



        String script1 = "<script>\n" +
                "        window.offsetYListener.onElement(1);\n" +
                "        var style = document.createElement(\"style\");\n" +
                "        style.type = \"text/css\";\n" +
                "        try{\n" +
                "            style.appendChild(document.createTextNode(\"div.news-layout.news-wrapper.recommend-layout, div.module-header-default.container.top-border.bottom-border,div.detail-source.container{display: none!important;}\"));\n" +
                "        }catch(ex){\n" +
                "            style.styleSheet.cssText=\"div.news-layout.news-wrapper.recommend-layout, div.module-header-default.container.top-border.bottom-border,div.detail-source.container{display: none!important;}\";//针对IE\n" +
                "        }\n" +
                "        var head = document.getElementsByTagName(\"head\")[0];\n" +
                "        head.appendChild(style);\n" +
                "</script>";


        String html = "<html><head><script>\n" +
                "        window.offsetYListener.onElement(1);\n" +
               /* "        var style = document.createElement(\"style\");\n" +
                "        style.type = \"text/css\";\n" +
                "        try{\n" +
                "            style.appendChild(document.createTextNode(\"div.news-layout.news-wrapper.recommend-layout, div.module-header-default.container.top-border.bottom-border,div.detail-source.container{display: none!important;}\"));\n" +
                "        }catch(ex){\n" +
                "            style.styleSheet.cssText=\"div.news-layout.news-wrapper.recommend-layout, div.module-header-default.container.top-border.bottom-border,div.detail-source.container{display: none!important;}\";//针对IE\n" +
                "        }\n" +
                "        var head = document.getElementsByTagName(\"head\")[0];\n" +
                "        head.appendChild(style);\n" +*/
                "    </script>\n</head></html>";

        //mWebView.loadData(html,"text/html", "utf-8");


        mWebClient.loadUrl("https://cpu.baidu.com/api/1022/ffa1f96f/detail/42728488720996769/news?aid=uSxcdqPxx93wA9bsZwaJt-A6zmf42iSsAvICnpsUW86DQ9ihHu3AGjJGDQC6EPdBB1UUVUXQe5IN1EwacepypjnvOqQrlHNNJfNx4NNBPPn8BG0OGQeKkooU0JyZVXsyT1bfZC5VBnmYAc_yXzx0sKwUu8VSfHaZk5GOOl2B6AXiO1ui-Cp1aAyYB-Uy0Oy-I8sm63n13o8iVONvbOP0ye5ZlOxHvChggajUyDajhnD1S1MQbo7xKRsNWzyfD5l2GohNQo7w6pB6IQUxL3i6aVTbevao10sqGI5Nph39t8n2P_29apluIJJjOQs4xGwNQ8_uRXBZmaGX2EtADxw-vw&scene=0&log_id=1594634373067a46bc63afbfada03837&exp_infos=20406_20515_20532_20542_20554_20603_20623_20651_20662_20901_20984_21500_21501_22116_22641_25311_26451_29578_29722_20010_8104301_8104903_8800030_7000010_8104204_8300062_8800012_8800022_8200139_8800000_8002400_8190602_8190802_8200142_8104301_8104903_8800030_7000010_8104204_8300062_8800012_8800022_8200139_8800000_8002400_8190602_8190802_8200142_41100_40001_43302_44161_44201_44212_44221_44235_44244_44264_44274_44281_44293_8200139_8190307_8103900_8104301_8104903_8800030_7000010_8104204_8800012_8800022_8200139_8800000_8002400_8190602_8190802_8200142&no_list=1&forward=api&api_version=2&cds_session_id=70da31002836469aba4fa84af8af7faa&last_pv_id=API1594634373067a46bc63afbfada03837&cpu_union_id=ADID_ef98a1ccbb761a257a1bda9f357426b4&uls=&rt=12&rts=4096");





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
        height += 20;
        mWebView.loadUrl("javascript:setHeightById(\"div\",\"test_div_top\",\"" + height + "px\")");
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

        @JavascriptInterface
        public void onTest(String s) {
            Log.d(TAG, "onTest: " + s);
        }

        @JavascriptInterface
        public void onElement(int count) {
            Log.d(TAG, "onElement: " + count);
        }
    }
}