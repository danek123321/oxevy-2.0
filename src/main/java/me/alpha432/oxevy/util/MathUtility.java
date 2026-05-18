package me.alpha432.oxevy.util;

public class MathUtility {
    public static double lerp(double start, double end, double t) {
        return start + (end - start) * t;
    }
    
    public static float lerp(float start, float end, float t) {
        return start + (end - start) * t;
    }
}