package ml.mypals.lucidity.config;

import fi.dy.masa.malilib.config.options.*;
import ml.mypals.lucidity.LucidityModInfo;

import java.util.List;

public class ExplosionVisualizerConfigs {
    private static final String EXPLOSION_VISUALIZER_KEY = LucidityModInfo.MOD_ID+".config.explosion_visualizer";
    public static final ConfigBooleanHotkeyed EXPLOSION_TIMER= new ConfigBooleanHotkeyed("explode_timer", false,"").apply(EXPLOSION_VISUALIZER_KEY);
    public static final ConfigBooleanHotkeyed ENABLE_EXPLOSION_VISUALIZER= new ConfigBooleanHotkeyed("main_render", false,"").apply(EXPLOSION_VISUALIZER_KEY);
    public static final ConfigBooleanHotkeyed EXPLOSION_CENTER= new ConfigBooleanHotkeyed("explosion_center", false,"").apply(EXPLOSION_VISUALIZER_KEY);
    public static final ConfigBooleanHotkeyed BLOCK_DESTRUCTION= new ConfigBooleanHotkeyed("block_destruction", false,"").apply(EXPLOSION_VISUALIZER_KEY);
    public static final ConfigBooleanHotkeyed BLOCK_RAY_CAST= new ConfigBooleanHotkeyed("block_ray_cast", false,"").apply(EXPLOSION_VISUALIZER_KEY);
    public static final ConfigBooleanHotkeyed ENTITY_SAMPLE_POINTS= new ConfigBooleanHotkeyed("entity_sample_points", false,"").apply(EXPLOSION_VISUALIZER_KEY);
    public static final ConfigBooleanHotkeyed SAMPLE_POINT_RAY_CAST= new ConfigBooleanHotkeyed("sample_point_ray_cast", false,"").apply(EXPLOSION_VISUALIZER_KEY);
    public static final List<ConfigBase<?>> VALUES = List.of(
            ENABLE_EXPLOSION_VISUALIZER,
            EXPLOSION_CENTER,
            BLOCK_RAY_CAST,
            BLOCK_DESTRUCTION,
            ENTITY_SAMPLE_POINTS,
            SAMPLE_POINT_RAY_CAST
    );

}
