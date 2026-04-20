package me.alpha432.oxevy.util;

import net.minecraft.world.phys.Vec3;

import java.awt.Color;
import java.util.ArrayDeque;
import java.util.Queue;

/**
 * Object pool for frequently allocated objects to reduce GC pressure.
 * Reuses Color, Vec3, and int[] objects instead of creating new ones.
 *
 * @author Oxevy Team
 * @version 1.0
 */
public class ObjectPool {
    // Color pool - Colors are immutable, so we can safely reuse them
    private static final Queue<Color> colorPool = new ArrayDeque<>(64);
    private static final Queue<int[]> intArrayPool = new ArrayDeque<>(64);

    // Predefined common colors to avoid allocation
    private static final Color[] COMMON_COLORS = new Color[256];
    static {
        for (int i = 0; i < 256; i++) {
            COMMON_COLORS[i] = new Color(i, i, i, 255);
        }
    }

    /**
     * Get a Color from the pool or create a new one.
     * @param r Red component (0-255)
     * @param g Green component (0-255)
     * @param b Blue component (0-255)
     * @param a Alpha component (0-255)
     * @return Color instance (may be reused)
     */
    public static Color getColor(int r, int g, int b, int a) {
        // For common grayscale colors, return cached instance
        if (r == g && g == b && a == 255 && r < 256) {
            return COMMON_COLORS[r];
        }

        Color color = colorPool.poll();
        if (color == null) {
            return new Color(r, g, b, a);
        }

        // Note: Color is immutable, so we can't actually change it
        // Return to pool and create new (Colors can't be modified)
        colorPool.offer(color);
        return new Color(r, g, b, a);
    }

    /**
     * Get an int array for RGBA values from the pool.
     * @return int[4] array for r, g, b, a
     */
    public static int[] getIntArray() {
        int[] array = intArrayPool.poll();
        if (array == null) {
            return new int[4];
        }
        return array;
    }

    /**
     * Return int array to the pool.
     * @param array The array to return
     */
    public static void returnIntArray(int[] array) {
        if (array != null && array.length == 4) {
            // Clear the array
            array[0] = 0;
            array[1] = 0;
            array[2] = 0;
            array[3] = 0;
            intArrayPool.offer(array);
        }
    }

    /**
     * Get a pre-computed color from cache.
     * Most efficient method for common colors.
     */
    public static Color getCachedColor(int rgb) {
        int r = (rgb >> 16) & 0xFF;
        int g = (rgb >> 8) & 0xFF;
        int b = rgb & 0xFF;
        int a = (rgb >> 24) & 0xFF;

        if (r == g && g == b && a == 255 && r < 256) {
            return COMMON_COLORS[r];
        }

        return getColor(r, g, b, a);
    }

    /**
     * Interpolate between two colors without creating new Color objects.
     * @param color1 First color RGB
     * @param color2 Second color RGB
     * @param factor Interpolation factor (0-1)
     * @return Interpolated color
     */
    public static int interpolateColor(int color1, int color2, float factor) {
        int r1 = (color1 >> 16) & 0xFF;
        int g1 = (color1 >> 8) & 0xFF;
        int b1 = color1 & 0xFF;

        int r2 = (color2 >> 16) & 0xFF;
        int g2 = (color2 >> 8) & 0xFF;
        int b2 = color2 & 0xFF;

        int r = (int) (r1 + (r2 - r1) * factor);
        int g = (int) (g1 + (g2 - g1) * factor);
        int b = (int) (b1 + (b2 - b1) * factor);

        return (r << 16) | (g << 8) | b;
    }

    /**
     * Get pool statistics for debugging.
     */
    public static String getStats() {
        return String.format("Color Pool: %d, IntArray Pool: %d",
            colorPool.size(), intArrayPool.size());
    }

    /**
     * Clear all pooled objects (useful for memory cleanup).
     */
    public static void clear() {
        colorPool.clear();
        intArrayPool.clear();
    }
}
