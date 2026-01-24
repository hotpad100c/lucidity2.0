package ml.mypals.lucidity.config;

import com.google.common.collect.ImmutableList;
import fi.dy.masa.malilib.config.options.*;
import ml.mypals.lucidity.LucidityModInfo;

import java.util.List;

public class ImageRendererConfigs {
    private static final String IMAGER_RENDERING_KEY = LucidityModInfo.MOD_ID+".config.image_rendering";
    public static final ConfigBooleanHotkeyed IMAGER_RENDERING= new ConfigBooleanHotkeyed("image_rendering", false,"").apply(IMAGER_RENDERING_KEY);
    public static final ConfigStringList IMAGES= new ConfigStringList("images", ImmutableList.of()).apply(IMAGER_RENDERING_KEY);
    public static final ConfigFloat PIXELS_PER_BLOCK= new ConfigFloat("pixels_per_block", 64).apply(IMAGER_RENDERING_KEY);
    public static final List<ConfigBase<?>> VALUES = List.of(
            IMAGER_RENDERING,
            IMAGES,
            PIXELS_PER_BLOCK
    );

}
