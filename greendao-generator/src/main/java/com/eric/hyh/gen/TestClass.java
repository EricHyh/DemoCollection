package com.eric.hyh.gen;

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
            for (int index = 0; index < 1000; index++) {
                raf.seek((num - 1) * 8);
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
            RandomAccessFile raf = new RandomAccessFile("test", "rwd");
            for (int index = 0; index < 5; index++) {
                raf.seek(index * 8);
                long readLong = raf.readLong();
                System.out.println(readLong);
            }
            raf.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
