package ml.mypals.lucidity.config;

import fi.dy.masa.malilib.config.options.ConfigColor;
import ml.mypals.lucidity.LucidityModInfo;
import org.jetbrains.annotations.NotNull;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class VisualizerColors{

    private static final String COLORS_KEY = LucidityModInfo.MOD_ID+".config.colors";
    public static final List<@NotNull ConfigColor> VALUES = new ArrayList<>();
    public static final ConfigColor SCULK_SENSOR_RANGE_COLOR
            = register("sculk_sensor_color", new Color( 84, 154, 172,100));

    public static final ConfigColor YROT_COLOR
            = register("y_rot_color", new Color(0, 255, 0,100));

    public static final ConfigColor BODY_ROT_COLOR
            = register("body_rot_color", new Color(255, 0, 0,100));
    //new
    public static final ConfigColor CREAKING_HEART_INDICATOR_COLOR
            = register("creaking_heart_indicator_color",new Color(255, 129, 0,100));

    public static final ConfigColor ITEM_MERG_RANGE_COLOR =
            register("item_merg_range_visualize_color",new Color(255,255,0,100));


    public static final ConfigColor WITHER_DESTRUCTION_RANGE_COLOR =
            register("wither_destruction_range_color",new Color(255, 0, 0,100));


    public static final ConfigColor ENDER_DRAGON_DESTRUCTION_RANGE_COLOR =
            register("ender_dragon_destruction_color",new Color(137, 101, 255,100));


    public static final ConfigColor ENDER_DRAGON_WAYPOINT_COLOR =
            register("ender_dragon_waypoint_color",new Color(255, 0, 118, 240));

    //new
    public static final ConfigColor EXPLOSION_DESTRUCTION_COLOR =
            register("explosion_destruction_color",new Color(255, 122, 30, 70));
    public static final ConfigColor EXPLOSION_CENTER_COLOR =
            register("explosion_center_color",new Color(220, 255, 0, 255));

    public static final ConfigColor SAMPLE_POINT_SAFE_COLOR =
            register("sample_point_safe_color",new Color(0, 255, 30, 165));

    public static final ConfigColor SAMPLE_POINT_EXPOSED_COLOR =
            register("sample_point_exposed_color",new Color(255, 0, 0, 240));

    public static final ConfigColor BLOCK_EVENT_COLOR =
            register("block_event_color",new Color(81, 211, 138, 255));

    public static final ConfigColor SOUND_COLOR =
            register("sound_color",new Color(151, 73, 228, 255));

    public static ConfigColor register(String name, Color defaultValue) {
        return register(new ConfigColor(name, toHexRGBA(defaultValue)).apply(COLORS_KEY));
    }
    public static ConfigColor register(ConfigColor color) {
        VALUES.add(color);
        return color;
    }
    public static String toHexRGBA(int r,int g,int b,int a) {
        return String.format(
            "#%02X%02X%02X%02X",
            a,r,g,b
        );
    }
    public static String toHexRGBA(Color color) {
        return toHexRGBA(color.getRed(),color.getGreen(),color.getBlue(),color.getAlpha());
    }
}


