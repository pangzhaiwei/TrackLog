package cn.pzw.tracklog.util;

import android.graphics.Color;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class LineColorUtil {

    private static List<Integer> colors = new ArrayList<>();

    static {
        colors.add(Color.parseColor("#1d953f"));
        colors.add(Color.parseColor("#ed1941"));
        colors.add(Color.parseColor("#00ae9d"));
        colors.add(Color.parseColor("#f15a22"));
        colors.add(Color.parseColor("#84331f"));
        colors.add(Color.parseColor("#009ad6"));
        colors.add(Color.parseColor("#585eaa"));
        colors.add(Color.parseColor("#8552a1"));
        colors.add(Color.parseColor("#817936"));
    }

    public static int getRandomColor(){
        return colors.get(new Random().nextInt(colors.size()));
    }

}
