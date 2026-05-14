package dev.iris.tAB.utils;

import org.bukkit.Bukkit;

import java.util.Locale;

public final class TPSUtil {

    private static String cachedTPS = "20.00";

    private TPSUtil() {
    }

    public static String getTPS() {
        try {
            double[] tps = Bukkit.getTPS();

            if (tps.length > 0) {
                cachedTPS = String.format(Locale.ROOT, "%.2f", Math.min(tps[0], 20.0));
            }
        } catch (UnsupportedOperationException | LinkageError ignored) {
            /* Folia fallback */
        }

        return cachedTPS;
    }
}
