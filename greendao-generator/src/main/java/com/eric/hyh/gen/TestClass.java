package com.eric.hyh.gen;

import java.io.File;
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

        read();
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

    private static void read() {
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
    }
}
