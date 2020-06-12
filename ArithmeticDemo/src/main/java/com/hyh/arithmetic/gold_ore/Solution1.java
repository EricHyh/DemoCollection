package com.hyh.arithmetic.gold_ore;

import java.util.List;

public class Solution1 {


    /**
     * 10
     * <p>
     * <p>
     * 400/5
     * 500/5
     * 200/3
     * 300/4
     * 350/3
     */
    public int[] text(List<GoldOre> goldOres, int peopleNum) {
        //p(x) =
        //g(x) =
        //f(x, y) = 0           (x <= 1, y < p(0))
        //f(x, y) = g(0)        (x == 1, y >= p(0))
        //f(x, y) = f(x-1, y)    (x > 1, y < p(x-1))
        //f(x, y) = max(f(x-1, y), f(x-1, (y - p(x-1))) + g(x-1))
        return null;
    }

    public static class GoldOre {

        int gold;

        int peopleNum;

    }

}




