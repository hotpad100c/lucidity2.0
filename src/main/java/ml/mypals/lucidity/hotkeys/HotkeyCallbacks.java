package ml.mypals.lucidity.hotkeys;

import com.mojang.blaze3d.platform.InputConstants;
import fi.dy.masa.malilib.gui.GuiBase;
import fi.dy.masa.malilib.hotkeys.IHotkeyCallback;
import fi.dy.masa.malilib.hotkeys.IKeybind;
import fi.dy.masa.malilib.hotkeys.KeyAction;
import ml.mypals.lucidity.config.ImageRendererConfigs;
import ml.mypals.lucidity.config.LucidityConfigs;
import ml.mypals.lucidity.config.SelectiveRenderingConfigs;
import ml.mypals.lucidity.config.FeatureToggle;
import ml.mypals.lucidity.features.imageRender.ImageRenderManager;
import ml.mypals.lucidity.features.worldEaterHelper.WorldEaterHelperManager;
import ml.mypals.lucidity.gui.LucidityGuiConfigs;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;

import static ml.mypals.lucidity.LucidityModInfo.MOD_ID;
import static ml.mypals.lucidity.features.selectiveRendering.SelectiveRenderingManager.*;
import static org.lwjgl.glfw.GLFW.*;

public class HotkeyCallbacks {
    //? if >=1.21.9 {
    static KeyMapping.Category SELECTIVE_RENDERING = KeyMapping.Category.register(
            ResourceLocation.fromNamespaceAndPath(MOD_ID,"category.selective_renderings")
    );
    //?}
    public static KeyMapping addArea;
    public static KeyMapping switchRenderMode;
    public static KeyMapping deleteArea;
    public static void init ()
    {
        Callbacks callback = new Callbacks();
        LucidityConfigs.Generic.OPEN_CONFIG_GUI.getKeybind().setCallback(callback);
        LucidityConfigs.Generic.WORLD_EATER_MINE_HELPER_TARGETS.setValueChangeCallback(configStringList -> WorldEaterHelperManager.refresh());
        LucidityConfigs.Generic.WORLD_EATER_MINE_HELPER_HEIGHT.setValueChangeCallback(configFloat -> WorldEaterHelperManager.refresh());
        FeatureToggle.FLUID_TRANSPARENCY_OVERRIDE.setValueChangeCallback(iConfigBoolean -> Minecraft.getInstance().levelRenderer.allChanged());
        FeatureToggle.WORLD_EATER_MINE_HELPER.setValueChangeCallback(iConfigBoolean -> WorldEaterHelperManager.refresh());
        FeatureToggle.FLUID_SOURCE_HIGHLIGHT.setValueChangeCallback(iConfigBoolean -> Minecraft.getInstance().levelRenderer.allChanged());
        FeatureToggle.BLOCK_NO_RANDOM_OFFSET.setValueChangeCallback(iConfigBoolean -> Minecraft.getInstance().levelRenderer.allChanged());
        ImageRendererConfigs.IMAGES.setValueChangeCallback(configStringList -> ImageRenderManager.prepareImages());
        SelectiveRenderingConfigs.APPLY_TARGET_MODE.setValueChangeCallback((string)->scheduleChunkRebuild());
        SelectiveRenderingConfigs.HIDDEN_BLOCK_TRANSPARENCY.setValueChangeCallback((value)->Minecraft.getInstance().levelRenderer.allChanged());
        SelectiveRenderingConfigs.SELECTED_AREAS.setValueChangeCallback((string)->resolveSelectedAreasFromString(string.getStrings()));
        SelectiveRenderingConfigs.SELECTED_BLOCKS.setValueChangeCallback((string)->resolveSelectedBlockStatesFromString(string.getStrings()));
        SelectiveRenderingConfigs.SELECTED_ENTITIES.setValueChangeCallback((string)->resolveSelectedEntityTypesFromString(string.getStrings()));
        SelectiveRenderingConfigs.SELECTED_PARTICLES.setValueChangeCallback((string)->resolveSelectedParticleTypesFromString(string.getStrings()));
        SelectiveRenderingConfigs.WAND.setValueChangeCallback((string)->resolveSelectedWandFromString(string.getStringValue()));

    }
    public static void registerVanillaKeyBindings()
    {
        addArea = KeyBindingHelper.registerKeyBinding(new KeyMapping(
            "key.selective_renderings.addSelection",
            InputConstants.Type.KEYSYM,
            GLFW_KEY_EQUAL,
            //? if >=1.21.9 {
            SELECTIVE_RENDERING
            //?} else {
            /*"category.selective_renderings"
            *///?}
        ));
        switchRenderMode = KeyBindingHelper.registerKeyBinding(new KeyMapping(
                "key.selective_renderings.renderingMode",
                InputConstants.Type.KEYSYM,
                GLFW_KEY_LEFT_ALT,
                //? if >=1.21.9 {
                SELECTIVE_RENDERING
                //?} else {
                /*"category.selective_renderings"
                 *///?}
        ));
        deleteArea = KeyBindingHelper.registerKeyBinding(new KeyMapping(
                "key.selective_renderings.removeSelection",
                InputConstants.Type.KEYSYM,
                GLFW_KEY_MINUS,
                //? if >=1.21.9 {
                SELECTIVE_RENDERING
                //?} else {
                /*"category.selective_renderings"
                 *///?}
        ));
    }
    public static class Callbacks implements IHotkeyCallback {
        @Override
        public boolean onKeyAction(KeyAction action, IKeybind key) {
            Minecraft mc = Minecraft.getInstance();

            if (mc.player == null) {
                return false;
            }

            if (key == LucidityConfigs.Generic.OPEN_CONFIG_GUI.getKeybind()) {
                GuiBase.openGui(new LucidityGuiConfigs());
            }
            return true;
        }
    }
}
