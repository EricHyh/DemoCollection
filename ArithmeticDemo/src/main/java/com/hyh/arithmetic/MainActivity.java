package com.hyh.arithmetic;

import android.app.Activity;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.provider.CalendarContract;
import android.text.Html;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.hyh.arithmetic.utils.ListNode;

/**
 * @author Administrator
 * @description
 * @data 2019/11/28
 */
public class MainActivity extends Activity {

    private static final String TAG = "MainActivity_";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        TextView textView1 = findViewById(R.id.text1);
        TextView textView2 = findViewById(R.id.text2);
        TextView textView3 = findViewById(R.id.text3);

        textView1.setText(Html.fromHtml("<p style=\"margin-top: 0px; margin-bottom: 0px; padding: 0px; max-width: 100%; clear: both; min-height: 1em; color: rgb(51, 51, 51); font-family: -apple-system-font, BlinkMacSystemFont, &quot;Helvetica Neue&quot;, &quot;PingFang SC&quot;, &quot;Hiragino Sans GB&quot;, &quot;Microsoft YaHei UI&quot;, &quot;Microsoft YaHei&quot;, Arial, sans-serif; font-size: 17px; letter-spacing: 0.544px; text-align: justify; white-space: normal; background-color: rgb(255, 255, 255); box-sizing: border-box !important; overflow-wrap: break-word !important;\"><span style=\"margin: 0px; padding: 0px; max-width: 100%; box-sizing: border-box !important; word-wrap: break-word !important; color: rgb(216, 40, 33);\"><strong style=\"margin: 0px; padding: 0px; max-width: 100%; box-sizing: border-box !important; word-wrap: break-word !important;\"><span style=\"margin: 0px; padding: 0px; max-width: 100%; box-sizing: border-box !important; word-wrap: break-word !important; font-size: 19px;\">近日，一张洪金宝的近照在网络上被疯传，只因为他的变化实在是太大了，68岁的他竟然“瘦到脱相”！</span></strong></span></p>"));
        textView1.setBackground(new ColorDrawable(0xff00ff00));

        textView2.setText("近日，一张洪金宝的近照在网络上被疯传，只因为他的变化实在是太大了，68岁的他竟然“瘦到脱相");


        new Thread(new Runnable() {
            @Override
            public void run() {

                /*try {
                    ArrayList<ZJ> zjs = new ArrayList<>();
                    {
                        InputStream inputStream = getAssets().open("mycgrt.txt");
                        BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));

                        String str = null;
                        long length = 0;
                        ZJ lastZj = null;
                        while ((str = reader.readLine()) != null) {
                            //第82章 将军夫人今天偏要闯
                            int bytesLength = str.getBytes().length;
                            if (str.matches("^第.{1,3}章.+$")) {
                                ZJ zj = new ZJ();
                                zjs.add(zj);
                                zj.title = new String(str.getBytes("UTF-8"), "UTF-8");
                                zj.titleLength = bytesLength;
                                zj.contentStart = length + bytesLength;
                                if (lastZj != null) {
                                    lastZj.contentEnd = length;
                                    lastZj.contentLength = lastZj.contentEnd - lastZj.contentStart + 1;
                                }
                                lastZj = zj;
                            }
                            length += bytesLength;
                        }

                        if (lastZj != null) {
                            lastZj.contentEnd = length;
                            lastZj.contentLength = lastZj.contentEnd - lastZj.contentStart + 1;
                        }

                        reader.close();
                        inputStream.close();
                    }


                    {
                        InputStream inputStream = getAssets().open("mycgrt.txt");


                        BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, Charset.forName("GB2312")));

                        BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(
                                new FileOutputStream(new File(getExternalFilesDir(null), "mycgrt.txt"), false)));

                        String str = null;
                        long length = 0;

                        ZJ remove = zjs.remove(0);
                        while ((str = reader.readLine()) != null) {
                            int str_bytes_length = str.getBytes("GB2312").length;
                            if (str_bytes_length == remove.titleLength) {
                                writer.write(remove.title);
                                writer.newLine();
                                remove = zjs.remove(0);
                            } else {
                                writer.write(str);
                                writer.newLine();
                            }
                        }
                        writer.close();
                    }

                } catch (Exception e) {
                    e.printStackTrace();
                }*/
            }
        }).run();


        /*new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    {
                        InputStream inputStream = getAssets().open("mycgrt.txt");
                        BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, "GB2312"));

                        BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(
                                new FileOutputStream(new File(getExternalFilesDir(null), "mycgrt.txt"), false),
                                Charset.forName("UTF-8")));


                        String str = null;
                        long length = 0;
                        while ((str = reader.readLine()) != null) {
                            //第82章 将军夫人今天偏要闯
                            int bytesLength = str.getBytes().length;
                            if (str.matches("^第.{1,3}章.+$")) {

                            }
                            length += bytesLength;
                        }

                        writer.close();
                        reader.close();
                    }

                    *//*{
                        for (ZJ zj : zjs) {
                            Log.d(TAG, "run: " + zj);
                        }
                    }*//*


                    {
                        InputStream inputStream = getAssets().open("mycgrt.txt");

                        InputStreamReader isr = new InputStreamReader(inputStream, Charset.forName("GB2312"));

                        BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(
                                new FileOutputStream(new File(getExternalFilesDir(null), "mycgrt.txt"), false), Charset.forName("UTF-8")));


                        for (ZJ zj : zjs) {
                            Log.d(TAG, "run: " + zj);
                            writer.write(zj.title);
                            writer.newLine();

                            isr.skip(zj.titleLength);
                            long contentLength = zj.contentLength;
                            for (int i = 0; i < contentLength; i++) {
                                writer.write(isr.read());
                            }
                        }

                        writer.close();
                        isr.close();
                    }

                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }).run();*/

    }


    public void test(View v) {
        //[[1,4,5],[1,3,4],[2,6]]
        ListNode node1 = new ListNode(1);
        node1.next = new ListNode(4);
        node1.next.next = new ListNode(5);

        ListNode node2 = new ListNode(1);
        node2.next = new ListNode(3);
        node2.next.next = new ListNode(4);
        //ListNode merge = TestUtil.merge(node1, node2);
        Log.d(TAG, "run: ");
    }


    private static class ZJ {

        String title;

        int titleLength;

        long contentStart;

        long contentEnd;

        long contentLength;

        @Override
        public String toString() {
            return "ZJ{" +
                    "title='" + title + '\'' +
                    ", titleLength=" + titleLength +
                    ", contentStart=" + contentStart +
                    ", contentEnd=" + contentEnd +
                    ", contentLength=" + contentLength +
                    '}';
        }
    }
}