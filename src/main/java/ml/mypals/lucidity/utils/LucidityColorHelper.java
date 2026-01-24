package ml.mypals.lucidity.utils;

import fi.dy.masa.malilib.util.data.Color4f;

import java.awt.*;

public class LucidityColorHelper {
    public static Color c4f2C(Color4f color4f) {
        float r = color4f.r;
        float g = color4f.g;
        float b = color4f.b;
        float a = color4f.a;
        return new Color(r, g, b, a);
    }
    public static Color invertColor(int argb) {
        int a = (argb >>> 24) & 0xFF;
        int r = (argb >>> 16) & 0xFF;
        int g = (argb >>> 8) & 0xFF;
        int b = argb & 0xFF;

        r = 255 - r;
        g = 255 - g;
        b = 255 - b;

        return new Color(r,g,b,a);
    }
    public static Color pulseColor(Color color, float t, float minBrightness) {
        float brightness = minBrightness
                + (1.0f - minBrightness) * (0.5f + 0.5f * (float) Math.cos(t));

        int r = color.getRed();
        int g = color.getRed();
        int b = color.getBlue();
        int a = color.getAlpha();

        r = (int) (r * brightness);
        g = (int) (g * brightness);
        b = (int) (b * brightness);
        return new Color(r,g,b,a);
    }

}
