package com.gmail.game.yadokari1130.MinecraftLogBot.Data;

import com.dajudge.colordiff.ColorDiff;
import org.checkerframework.checker.units.qual.C;
import org.checkerframework.checker.units.qual.Substance;

import java.awt.*;
import java.util.ArrayList;

public enum MinecraftColor {
    BLACK("0", new Color(0x000000)),
    DARK_BLUE("1", new Color(0x0000AA)),
    DARK_GREEN("2", new Color(0x00AA00)),
    DARK_AQUA("3", new Color(0x00AAAA)),
    DARK_RED("4", new Color(0xAA0000)),
    DARK_PURPLE("5", new Color(0xAA00AA)),
    GOLD("6", new Color(0xFFAA00)),
    GRAY("7", new Color(0xAAAAAA)),
    DARK_GARY("8", new Color(0x555555)),
    BLUE("9", new Color(0x5555FF)),
    GREEN("a", new Color(0x55FF55)),
    AQUA("b", new Color(0x55FFFF)),
    RED("c", new Color(0xFF5555)),
    LIGHT_PURPLE("d", new Color(0xFF55FF)),
    YELLOW("e", new Color(0xFFFF55)),
    WHITE("f", new Color(0xFFFFFF));

    private final String code;
    private final Color color;

    private MinecraftColor(String code, Color color) {
        this.code = code;
        this.color = color;
    }

    public static MinecraftColor getNearestColor(Color color) {
        MinecraftColor result = null;
        double min = Double.MAX_VALUE;

        for (MinecraftColor mc : MinecraftColor.values()) {
            double diff = ColorDiff.diff(ColorDiff.rgb_to_lab(mc.getColor()), ColorDiff.rgb_to_lab(color));
            if (min >= diff) {
                min = diff;
                result = mc;
            }
        }

        return result;
    }

    public String getCode() {
        return "§" + code;
    }

    public Color getColor() {
        return color;
    }

    public static String getResetCode() {
        return "§r";
    }
}
