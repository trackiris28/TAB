package dev.iris.tAB.utils;

import org.bukkit.Bukkit;

public class TPSUtil {

    private static String cachedTPS = "20.00";

    public static String getTPS() {

        try {

            double[] tps = Bukkit.getTPS();

            if (tps.length > 0) {

                double value = tps[0];

                if (value > 20.0) {
                    value = 20.0;
                }

                cachedTPS = String.format("%.2f", value);
            }

        } catch (Throwable ignored) {

            /*
             * Folia fallback
             */

        }

        return cachedTPS;
    }
}