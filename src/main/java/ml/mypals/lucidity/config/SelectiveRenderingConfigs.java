package ml.mypals.lucidity.config;

import com.google.common.collect.ImmutableList;
import fi.dy.masa.malilib.config.options.*;
import ml.mypals.lucidity.LucidityModInfo;
import ml.mypals.lucidity.features.selectiveRendering.SelectiveRenderingManager;
import ml.mypals.lucidity.features.selectiveRendering.WandActionsManager;

import java.util.List;

import static ml.mypals.lucidity.features.selectiveRendering.SelectiveRenderingManager.SelectiveRenderingMode.OFF;
import static ml.mypals.lucidity.features.selectiveRendering.WandActionsManager.WandApplyToMode.APPLY_TO_BLOCKS;

public class SelectiveRenderingConfigs {
    private static final String SELECTIVE_RENDERING_KEY = LucidityModInfo.MOD_ID+".config.selective_rendering";
    public static final ConfigString WAND = new ConfigString("wand","minecraft:breeze_rod").apply(SELECTIVE_RENDERING_KEY);
    public static final ConfigBoolean FORCE_LIGHT_UPDATE= new ConfigBoolean("force_light_update", true).apply(SELECTIVE_RENDERING_KEY);

    public static final ConfigStringList SELECTED_AREAS = new ConfigStringList("selected_areas", ImmutableList.<String>builder().build()).apply(SELECTIVE_RENDERING_KEY);

    public static final ConfigStringList SELECTED_BLOCKS = new ConfigStringList("selected_blocks", ImmutableList.<String>builder().build()).apply(SELECTIVE_RENDERING_KEY);
    public static final ConfigStringList SELECTED_ENTITIES = new ConfigStringList("selected_entities", ImmutableList.<String>builder().build()).apply(SELECTIVE_RENDERING_KEY);
    public static final ConfigStringList SELECTED_PARTICLES = new ConfigStringList("selected_particles", ImmutableList.<String>builder().build()).apply(SELECTIVE_RENDERING_KEY);

    public static final ConfigInteger HIDDEN_BLOCK_TRANSPARENCY = new ConfigInteger("hidden_transparency",0,0,255);

    public static final SelectiveRenderingModeList BLOCK_RENDERING_MODE = new SelectiveRenderingModeList("block_mode",OFF).apply(SELECTIVE_RENDERING_KEY);
    public static final SelectiveRenderingModeList ENTITY_RENDERING_MODE = new SelectiveRenderingModeList("entity_mode",OFF).apply(SELECTIVE_RENDERING_KEY);
    public static final SelectiveRenderingModeList PARTICLE_RENDERING_MODE = new SelectiveRenderingModeList("particle_mod",OFF).apply(SELECTIVE_RENDERING_KEY);
    public static final SelectiveRenderingTargetList APPLY_TARGET_MODE = new SelectiveRenderingTargetList("apply_target",APPLY_TO_BLOCKS).apply(SELECTIVE_RENDERING_KEY);

    public static final List<ConfigBase<?>> VALUES = List.of(
            WAND,
            FORCE_LIGHT_UPDATE,
            HIDDEN_BLOCK_TRANSPARENCY,
            SELECTED_AREAS,
            SELECTED_BLOCKS,
            SELECTED_ENTITIES,
            SELECTED_PARTICLES,
            BLOCK_RENDERING_MODE,
            ENTITY_RENDERING_MODE,
            PARTICLE_RENDERING_MODE,
            APPLY_TARGET_MODE
    );
    public static boolean isBlockFullyHidden(){
        return HIDDEN_BLOCK_TRANSPARENCY.getIntegerValue() <= 0;
    }

    public static class SelectiveRenderingModeList extends ConfigOptionList {
        public SelectiveRenderingModeList(String name, SelectiveRenderingManager.SelectiveRenderingMode defaultValue) {
            super(name, defaultValue);
        }
        @Override
        public SelectiveRenderingModeList apply(String key) {
            super.apply(key);
            return this;
        }
        @Override
        public SelectiveRenderingManager.SelectiveRenderingMode getOptionListValue() {
            return (SelectiveRenderingManager.SelectiveRenderingMode) super.getOptionListValue();
        }
        @Override
        public SelectiveRenderingManager.SelectiveRenderingMode getDefaultOptionListValue() {
            return (SelectiveRenderingManager.SelectiveRenderingMode) super.getDefaultOptionListValue();
        }
    }

    public static class SelectiveRenderingTargetList extends ConfigOptionList {
        public SelectiveRenderingTargetList(String name, WandActionsManager.WandApplyToMode  defaultValue) {
            super(name, defaultValue);
        }
        @Override
        public SelectiveRenderingTargetList apply(String key) {
            super.apply(key);
            return this;
        }
        @Override
        public WandActionsManager.WandApplyToMode getOptionListValue() {
            return (WandActionsManager.WandApplyToMode ) super.getOptionListValue();
        }
        @Override
        public WandActionsManager.WandApplyToMode getDefaultOptionListValue() {
            return (WandActionsManager.WandApplyToMode ) super.getDefaultOptionListValue();
        }
    }
}
