package me.alpha432.oxevy.util.render;

import me.alpha432.oxevy.Oxevy;

public class AnimationUtil {

    public enum Easing {
        LINEAR,
        EASE_IN,
        EASE_OUT,
        EASE_IN_OUT,
        EASE_OUT_BACK,
        EASE_OUT_ELASTIC,
        EASE_OUT_BOUNCE
    }

    /**
     * Smoothly animate a value from start to end using easing
     */
    public static float animate(float current, float target, float speed, Easing easing) {
        float delta = target - current;
        float factor = calculateEasing(speed, easing);
        return current + delta * factor;
    }

    /**
     * Animate with custom factor (0-1)
     */
    public static float animate(float current, float target, float factor) {
        return current + (target - current) * Math.max(0, Math.min(1, factor));
    }

    /**
     * Calculate easing factor based on progress
     */
    private static float calculateEasing(float progress, Easing easing) {
        progress = Math.max(0, Math.min(1, progress));

        switch (easing) {
            case LINEAR:
                return progress;
            case EASE_IN:
                return progress * progress;
            case EASE_OUT:
                return 1 - (1 - progress) * (1 - progress);
            case EASE_IN_OUT:
                return progress < 0.5 ? 2 * progress * progress : 1 - (float) Math.pow(-2 * progress + 2, 2) / 2;
            case EASE_OUT_BACK:
                float c1 = 1.70158f;
                float c3 = c1 + 1;
                return 1 + c3 * (float) Math.pow(progress - 1, 3) + c1 * (float) Math.pow(progress - 1, 2);
            case EASE_OUT_ELASTIC:
                float c4 = (2 * (float) Math.PI) / 3;
                return progress == 0 ? 0 : progress == 1 ? 1 :
                    (float) Math.pow(2, -10 * progress) * (float) Math.sin((progress * 10 - 0.75f) * c4) + 1;
            case EASE_OUT_BOUNCE:
                float n1 = 7.5625f;
                float d1 = 2.75f;
                if (progress < 1 / d1) {
                    return n1 * progress * progress;
                } else if (progress < 2 / d1) {
                    return n1 * (progress -= 1.5f / d1) * progress + 0.75f;
                } else if (progress < 2.5f / d1) {
                    return n1 * (progress -= 2.25f / d1) * progress + 0.9375f;
                } else {
                    return n1 * (progress -= 2.625f / d1) * progress + 0.984375f;
                }
            default:
                return progress;
        }
    }

    /**
     * Smoothly interpolate between two colors
     */
    public static int interpolateColor(int startColor, int endColor, float factor) {
        int startA = (startColor >> 24) & 0xFF;
        int startR = (startColor >> 16) & 0xFF;
        int startG = (startColor >> 8) & 0xFF;
        int startB = startColor & 0xFF;

        int endA = (endColor >> 24) & 0xFF;
        int endR = (endColor >> 16) & 0xFF;
        int endG = (endColor >> 8) & 0xFF;
        int endB = endColor & 0xFF;

        int a = (int) (startA + (endA - startA) * factor);
        int r = (int) (startR + (endR - startR) * factor);
        int g = (int) (startG + (endG - startG) * factor);
        int b = (int) (startB + (endB - startB) * factor);

        return (a << 24) | (r << 16) | (g << 8) | b;
    }

    /**
     * Clamp a value between min and max
     */
    public static float clamp(float value, float min, float max) {
        return Math.max(min, Math.min(max, value));
    }

    /**
     * Linear interpolation
     */
    public static float lerp(float start, float end, float factor) {
        return start + (end - start) * factor;
    }

    /**
     * Smooth step function
     */
    public static float smoothStep(float edge0, float edge1, float x) {
        x = clamp((x - edge0) / (edge1 - edge0), 0, 1);
        return x * x * (3 - 2 * x);
    }

    /**
     * Get the game time in milliseconds for animation timing
     */
    public static long getTime() {
        return System.currentTimeMillis();
    }
}
