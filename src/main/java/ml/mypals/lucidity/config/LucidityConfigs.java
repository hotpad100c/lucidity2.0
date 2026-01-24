package ml.mypals.lucidity.config;

import com.google.common.collect.ImmutableList;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import fi.dy.masa.malilib.config.ConfigUtils;
import fi.dy.masa.malilib.config.IConfigBase;
import fi.dy.masa.malilib.config.IConfigHandler;
import fi.dy.masa.malilib.config.options.*;
import fi.dy.masa.malilib.hotkeys.IHotkey;
import fi.dy.masa.malilib.hotkeys.KeybindSettings;
import fi.dy.masa.malilib.util.FileUtils;
import fi.dy.masa.malilib.util.JsonUtils;
import ml.mypals.lucidity.Lucidity;
import ml.mypals.lucidity.LucidityModInfo;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

public class LucidityConfigs implements IConfigHandler
{
    private static final String CONFIG_FILE_NAME = LucidityModInfo.MOD_ID + ".json";
    private static final int CONFIG_VERSION = 1;
    private static final String GENERIC_KEY = LucidityModInfo.MOD_ID+".config.generic";

    public static class Generic {
        public static final ConfigHotkey OPEN_CONFIG_GUI= new ConfigHotkey(
                "open_config",
                "X + V",
                KeybindSettings.RELEASE_EXCLUSIVE
        ).apply(GENERIC_KEY);
        public static final ConfigInteger SCULK_DETECT_RANGE= new ConfigInteger(
                "sculk_detect_range",
                18,
                0,
                32
        ).apply(GENERIC_KEY);
        public static final ConfigFloat FLUID_TRANSPARENCY = new ConfigFloat(
                "fluid_transparency",
                0.5f,
                0.0f,
                1.0f
        ).apply(GENERIC_KEY);

        public static final ConfigStringList WORLD_EATER_MINE_HELPER_TARGETS = new ConfigStringList(
                "world_eater_helper_target",
                ImmutableList.of(
                        "#c:clusters",
                        "#c:budding_blocks",
                        "#malilib:immovable_blocks",
                        "#malilib:ore_blocks",
                        "minecraft:kelp",
                        "minecraft:ancient_debris",
                        "%[waterlogged=true]",
                        "minecraft:kelp"
                )
        ).apply(GENERIC_KEY);
        public static final ConfigFloat WORLD_EATER_MINE_HELPER_HEIGHT = new ConfigFloat(
                "world_eater_helper_render_height",
                7f,
                1f,
                7.8f
        ).apply(GENERIC_KEY);

        public static final ConfigFloat INVISIBLE_ENTITY_ALPHA = new ConfigFloat(
                "invisible_entity_alpha",
                0.2f,
                0f,
                1f
        ).apply(GENERIC_KEY);


        public static final ImmutableList<IConfigBase> OPTIONS = ImmutableList.of(
                OPEN_CONFIG_GUI,
                SCULK_DETECT_RANGE,
                FLUID_TRANSPARENCY,
                WORLD_EATER_MINE_HELPER_TARGETS,
                WORLD_EATER_MINE_HELPER_HEIGHT,
                INVISIBLE_ENTITY_ALPHA
        );
        public static final List<IHotkey> HOTKEY_LIST = ImmutableList.of(
                OPEN_CONFIG_GUI
        );
    }

    private static final String OTHER = LucidityModInfo.MOD_ID+".config.other";
    public static class Other {
        public static final ConfigBoolean YACL_STYLE = new ConfigBoolean(
                "yacl_like_config",
                false
        ).apply(OTHER);
        public static final ImmutableList<IConfigBase> OPTIONS = ImmutableList.of(
                YACL_STYLE
        );
    }
    @Override
    public void onConfigsChanged() {
        IConfigHandler.super.onConfigsChanged();
    }

    public static void loadFromFile()
    {
        Path configFile = FileUtils.getConfigDirectoryAsPath().resolve(CONFIG_FILE_NAME);

        if (Files.exists(configFile) && Files.isReadable(configFile))
        {
            JsonElement element = JsonUtils.parseJsonFileAsPath(configFile);

            if (element != null && element.isJsonObject())
            {
                JsonObject root = element.getAsJsonObject();
                ConfigUtils.readConfigBase(root, "ExplosionVisualizer", ExplosionVisualizerConfigs.VALUES);
                ConfigUtils.readConfigBase(root, "Generic", LucidityConfigs.Generic.OPTIONS);
                ConfigUtils.readConfigBase(root, "Colors", VisualizerColors.VALUES);
                ConfigUtils.readHotkeyToggleOptions(root, "FeatureHotKeys","FeatureToggles", FeatureToggle.VALUES);
                ConfigUtils.readConfigBase(root, "SelectiveRendering",SelectiveRenderingConfigs.VALUES);
                ConfigUtils.readConfigBase(root,"ImageRendering",ImageRendererConfigs.VALUES);
                ConfigUtils.readConfigBase(root, "Other", LucidityConfigs.Generic.OPTIONS);
                Lucidity.onConfigLoaded();
            }
        }
    }

    public static void saveToFile()
    {
        Path dir = FileUtils.getConfigDirectoryAsPath();

        if (!Files.exists(dir))
        {
            FileUtils.createDirectoriesIfMissing(dir);
        }

        if (Files.isDirectory(dir))
        {
            JsonObject root = new JsonObject();
            ConfigUtils.writeConfigBase(root, "ExplosionVisualizer", ExplosionVisualizerConfigs.VALUES);
            ConfigUtils.writeConfigBase(root, "Generic", LucidityConfigs.Generic.OPTIONS);
            ConfigUtils.writeConfigBase(root, "Colors", VisualizerColors.VALUES);
            ConfigUtils.writeHotkeyToggleOptions(root, "FeatureHotKeys","FeatureToggles", FeatureToggle.VALUES);
            ConfigUtils.writeConfigBase(root, "SelectiveRendering",SelectiveRenderingConfigs.VALUES);
            ConfigUtils.writeConfigBase(root,"ImageRendering",ImageRendererConfigs.VALUES);
            ConfigUtils.writeConfigBase(root, "Other", LucidityConfigs.Generic.OPTIONS);
            JsonUtils.writeJsonToFileAsPath(root, dir.resolve(CONFIG_FILE_NAME));
        }
    }

    @Override
    public void load()
    {
        loadFromFile();
    }

    @Override
    public void save()
    {
        saveToFile();
    }
}
