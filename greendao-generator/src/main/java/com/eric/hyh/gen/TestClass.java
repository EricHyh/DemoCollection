package com.eric.hyh.gen;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.RandomAccessFile;

/**
 * Created by Eric_He on 2018/12/24.
 */

public class TestClass {

    public static void main(String[] args) {
        /*for (int index = 0; index < 5; index++) {
            final int finalIndex = index;
            new Thread(new Runnable() {
                @Override
                public void run() {
                    write(finalIndex + 1);
                }
            }).start();
        }*/
        //writeBuffer();
        simpleWrite();
        simpleRead();
    }


    private static void simpleWrite() {
        try {
            RandomAccessFile raf = new RandomAccessFile("test", "rwd");
            raf.seek(16);
            raf.writeLong(1);
            raf.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void simpleRead() {
        try {
            RandomAccessFile raf = new RandomAccessFile("test", "rwd");
            raf.seek(8);
            long l = raf.readLong();
            System.out.println("simpleRead = " + l);
            raf.seek(16);
            long l1 = raf.readLong();
            System.out.println("simpleRead = " + l1);
            raf.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    private static void write(int num) {
        System.out.println(num + "-start");
        try {
            RandomAccessFile raf = new RandomAccessFile("test", "rwd");
            raf.seek((num - 1) * 8 * 1000);
            for (int index = 0; index < 1000; index++) {
                raf.writeLong(index * num);
            }
            raf.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        System.out.println(num + "-end");
    }


    private static void writeBuffer() {
        try {

            BufferedInputStream bis = new BufferedInputStream(new FileInputStream("changyong.txt"));

            RandomAccessFile raf = new RandomAccessFile("test.txt", "rw");
            BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(raf.getFD()));

            raf.seek(5000);
            byte[] buffer = new byte[1024];
            int len;
            while ((len = bis.read(buffer)) != -1) {
                bos.write(buffer, 0, len);
            }
            bos.close();
            try {
                raf.close();
            } catch (Exception e) {
                //
            }
            bis.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    private static void read() {
        long start = System.currentTimeMillis();
        try {
            File testFile = new File("test");
            System.out.println("testFile length = " + testFile.length());

            System.out.println("read start");

            RandomAccessFile raf = new RandomAccessFile("test", "rwd");
            for (int index = 0; index < 5; index++) {
                raf.seek(index * 8 * 1000);
                long readLong1 = raf.readLong();
                long readLong2 = raf.readLong();
                long readLong3 = raf.readLong();
                System.out.println(index + "-readLong1-" + readLong1);
                System.out.println(index + "-readLong2-" + readLong2);
                System.out.println(index + "-readLong3-" + readLong3);
            }
            raf.close();
            System.out.println("read end");
        } catch (Exception e) {
            e.printStackTrace();
        }
        long end = System.currentTimeMillis();
        System.out.println("use time = " + (end - start));
    }
}
