package com.hyh.arithmetic;

import android.app.Activity;
import android.os.Bundle;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.nio.charset.Charset;
import java.util.ArrayList;

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

        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
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
                }
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