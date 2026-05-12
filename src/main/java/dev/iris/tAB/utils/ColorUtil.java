package dev.iris.tAB.utils;

import org.bukkit.ChatColor;

public class ColorUtil {

    public static String color(String text) {
        return ChatColor.translateAlternateColorCodes('&', text);
    }
}