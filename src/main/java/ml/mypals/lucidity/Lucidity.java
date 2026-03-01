package ml.mypals.lucidity;

import fi.dy.masa.malilib.event.InitializationHandler;
import ml.mypals.lucidity.features.imageRender.ImageRenderManager;
import ml.mypals.lucidity.features.selectiveRendering.SelectiveRenderingManager;
import ml.mypals.lucidity.features.selectiveRendering.WandActionsManager;
import ml.mypals.lucidity.features.selectiveRendering.WandTooltipRenderer;
import ml.mypals.lucidity.features.tisMicroTiminghighlight.MicrotimingKeyWorldTracker;
import ml.mypals.lucidity.features.visualizers.VisualizerManager;
import ml.mypals.lucidity.features.visualizers.explosion.ExplosionSimulatorManager;
import ml.mypals.lucidity.features.visualizers.fluidSource.FluidSourceResourceLoader;
import ml.mypals.lucidity.features.visualizers.soundVisualize.SoundListener;
import ml.mypals.lucidity.features.worldEaterHelper.WorldEaterHelperManager;
import ml.mypals.lucidity.init.LucidityInit;
import ml.mypals.lucidity.features.netherPosCaculator.NetherPosCaculatorManager;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
//? if >=1.21.6 {

//?} else if >= 1.21.4 {
import net.fabricmc.fabric.api.client.rendering.v1.HudLayerRegistrationCallback;
import net.fabricmc.fabric.api.client.rendering.v1.IdentifiedLayer;
//?} else {
/*import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
*///?}
//? if >=1.21.6 {
/*import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElement;
import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElementRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.hud.VanillaHudElements;
import net.fabricmc.fabric.impl.client.rendering.hud.HudElementRegistryImpl;
*///?}
import net.fabricmc.fabric.api.event.player.AttackBlockCallback;
import net.fabricmc.fabric.api.event.player.UseBlockCallback;
import net.fabricmc.fabric.api.resource.ResourceManagerHelper;
import net.fabricmc.fabric.api.resource.ResourcePackActivationType;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionResult;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static ml.mypals.lucidity.LucidityModInfo.MOD_ID;
import static ml.mypals.lucidity.config.LucidityConfigs.Generic.WORLD_EATER_MINE_HELPER_TARGETS;
import static ml.mypals.lucidity.config.SelectiveRenderingConfigs.*;
import static ml.mypals.lucidity.features.selectiveRendering.SelectiveRenderingManager.*;
import static ml.mypals.lucidity.hotkeys.HotkeyCallbacks.registerVanillaKeyBindings;

public class Lucidity implements ModInitializer {

    public static final Logger LOGGER = LoggerFactory.getLogger("lucidity");
    private static boolean hasWorldLoaded = false;
    @Override
    public void onInitialize()
    {
        InitializationHandler.getInstance().registerInitializationHandler(new LucidityInit());
        registerVanillaKeyBindings();
        //? if >=1.21.6 {
        /*HudElementRegistry.attachElementBefore(VanillaHudElements.CHAT,
                ResourceLocation.fromNamespaceAndPath(MOD_ID,"selective_rendering_hud"),
                (guiGraphics,deltaTracker)->{
                    WandTooltipRenderer.renderWandTooltip(guiGraphics);
                });
        *///?} else if >= 1.21.4 {
        HudLayerRegistrationCallback.EVENT.register((wrapper) -> {
            wrapper.addLayer(new IdentifiedLayer() {
                @Override
                public ResourceLocation id() {
                    return ResourceLocation.fromNamespaceAndPath(MOD_ID,"selective_rendering_hud");
                }

                @Override
                public void render(@NotNull GuiGraphics guiGraphics, @NotNull DeltaTracker deltaTracker) {
                    WandTooltipRenderer.renderWandTooltip(guiGraphics);
                }
            });
        });
        //?} else {
        /*HudRenderCallback.EVENT.register((guiGraphics, deltaTracker) -> {
            WandTooltipRenderer.renderWandTooltip(guiGraphics);
        });
        *///?}
        FabricLoader.getInstance().getModContainer(MOD_ID).ifPresent(container -> {
            ResourceManagerHelper.registerBuiltinResourcePack(
                    ResourceLocation.fromNamespaceAndPath(MOD_ID, "lavahighlight"),
                    container,
                    ResourcePackActivationType.NORMAL
            );
        });
        ClientTickEvents.END_CLIENT_TICK.register(client->{
            WandActionsManager.wandActions(client);
            VisualizerManager.tick();
            ExplosionSimulatorManager.tick();
            ImageRenderManager.onClientTick();
            if(!hasWorldLoaded && client.level != null){
                onEnterLevel(client);
                hasWorldLoaded = true;
            }
            if(hasWorldLoaded && client.level == null){
                onLeaveLevel(client);
                hasWorldLoaded = false;
            }
            if(!SoundListener.registered){
                Minecraft.getInstance().getSoundManager().addListener(SoundListener.INSTANCE);
                SoundListener.registered = true;
            }
        });
        UseBlockCallback.EVENT.register((player, world, hand, pos) -> {
            if (world.isClientSide() && player.getItemInHand(hand).getItem() == wand) {
                return InteractionResult.FAIL;
            }
            NetherPosCaculatorManager.onPlayerUse(player, world, hand, pos);
            return InteractionResult.PASS;
        });
        AttackBlockCallback.EVENT.register((player, world, hand, pos, dir) -> {
            if (world.isClientSide() && player.getItemInHand(hand).getItem() == wand) {
                return InteractionResult.FAIL;
            }
            return InteractionResult.PASS;
        });

        FluidSourceResourceLoader.init();
    }
    public static void onConfigLoaded(){
        SelectiveRenderingManager.resolveSelectedAreasFromString(SELECTED_AREAS.getStrings());
        SelectiveRenderingManager.resolveSelectedEntityTypesFromString(SELECTED_ENTITIES.getStrings());
        SelectiveRenderingManager.resolveSelectedBlockStatesFromString(SELECTED_BLOCKS.getStrings());
        SelectiveRenderingManager.resolveSelectedParticleTypesFromString(SELECTED_PARTICLES.getStrings());
        SelectiveRenderingManager.resolveSelectedWandFromString(WAND.getStringValue());
        WorldEaterHelperManager.resolveTargetBlocksFromString(WORLD_EATER_MINE_HELPER_TARGETS.getStrings());
    }
    public static void lateInit() {
        onConfigLoaded();
    }
    public static void onEnterLevel(Minecraft minecraft){
        ImageRenderManager.prepareImages();
        MicrotimingKeyWorldTracker.start();
    }
    public static void onLeaveLevel(Minecraft minecraft){
        MicrotimingKeyWorldTracker.stop();
    }
}