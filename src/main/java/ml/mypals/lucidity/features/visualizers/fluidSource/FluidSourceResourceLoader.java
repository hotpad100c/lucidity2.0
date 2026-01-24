package ml.mypals.lucidity.features.visualizers.fluidSource;

import net.fabricmc.fabric.api.client.render.fluid.v1.FluidRenderHandler;
import net.fabricmc.fabric.api.client.render.fluid.v1.FluidRenderHandlerRegistry;
import net.fabricmc.fabric.api.resource.ResourceManagerHelper;
import net.fabricmc.fabric.api.resource.SimpleSynchronousResourceReloadListener;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BiomeColors;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.ModelBakery;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LiquidBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.function.Function;

import static ml.mypals.lucidity.LucidityModInfo.MOD_ID;
import static ml.mypals.lucidity.config.FeatureToggle.FLUID_SOURCE_HIGHLIGHT;
import static net.minecraft.world.level.block.state.properties.BlockStateProperties.DRAG;

public class FluidSourceResourceLoader implements SimpleSynchronousResourceReloadListener {
    public static final TextureAtlasSprite[] lavaSourceSpites = new TextureAtlasSprite[2];
    public static final TextureAtlasSprite[] defaultLavaSourceSpites = new TextureAtlasSprite[2];

    public static final TextureAtlasSprite[] waterSourceSpites = new TextureAtlasSprite[3];
    public static final TextureAtlasSprite[] bubbleWaterSpitesUp = new TextureAtlasSprite[3];
    public static final TextureAtlasSprite[] bubbleWaterSpitesDown = new TextureAtlasSprite[3];

    public static final TextureAtlasSprite[] defaultWaterSourceSpites = new TextureAtlasSprite[3];
    private static final ResourceLocation LISTENER_ID = ResourceLocation.fromNamespaceAndPath(MOD_ID,"reload_listener");
    private static final ResourceLocation FLOWING_LAVA_SPRITE_ID = ResourceLocation.fromNamespaceAndPath(MOD_ID,"block/lava_flow");
    private static final ResourceLocation STILL_LAVA_SPRITE_ID = ResourceLocation.fromNamespaceAndPath(MOD_ID,"block/lava_still");
    private static final ResourceLocation FLOWING_WATER_SPRITE_ID = ResourceLocation.fromNamespaceAndPath(MOD_ID,"block/water_flow");
    private static final ResourceLocation STILL_WATER_SPRITE_ID = ResourceLocation.fromNamespaceAndPath(MOD_ID,"block/water_still");

    private static final ResourceLocation UP_BUBBLE_SPRITE_ID = ResourceLocation.fromNamespaceAndPath(MOD_ID,"block/bubble_up");
    private static final ResourceLocation DOWN_BUBBLE_SPRITE_ID = ResourceLocation.fromNamespaceAndPath(MOD_ID,"block/bubble_down");
    public static TextureAtlasSprite lavaSourceFlowSprite;
    public static TextureAtlasSprite lavaSourceStillSprite;
    public static TextureAtlasSprite defaultLavaSourceFlowSprite;
    public static TextureAtlasSprite defaultLavaSourceStillSprite;

    public static TextureAtlasSprite waterSourceFlowSprite;
    public static TextureAtlasSprite waterSourceStillSprite;
    public static TextureAtlasSprite defaultWaterSourceFlowSprite;
    public static TextureAtlasSprite defaultWaterOverlaySprite;
    public static TextureAtlasSprite defaultWaterSourceStillSprite;

    public static TextureAtlasSprite bubbleUpSprite;
    public static TextureAtlasSprite bubbleDownSprite;
    public static void init() {
        ResourceManagerHelper.get(PackType.CLIENT_RESOURCES).registerReloadListener(new FluidSourceResourceLoader());
    }

    @Override
    public ResourceLocation getFabricId() {
        return LISTENER_ID;
    }

    @Override
    public void onResourceManagerReload(@NotNull ResourceManager resourceManager) {
        final Function<ResourceLocation, TextureAtlasSprite> atlas = Minecraft.getInstance().getTextureAtlas(TextureAtlas.LOCATION_BLOCKS);
        lavaSourceStillSprite = atlas.apply(STILL_LAVA_SPRITE_ID);
        lavaSourceFlowSprite = atlas.apply(FLOWING_LAVA_SPRITE_ID);
        lavaSourceSpites[0] = lavaSourceStillSprite;
        lavaSourceSpites[1] = lavaSourceFlowSprite;
        //? if >=1.21.5 {
        defaultLavaSourceStillSprite = Minecraft.getInstance().getModelManager().getBlockModelShaper().getBlockModel(Blocks.LAVA.defaultBlockState()).particleIcon();
        //?} else {
        /*defaultLavaSourceStillSprite = Minecraft.getInstance().getModelManager().getBlockModelShaper().getBlockModel(Blocks.LAVA.defaultBlockState()).getParticleIcon();
        *///?}
        defaultLavaSourceFlowSprite = ModelBakery.LAVA_FLOW.sprite();
        defaultLavaSourceSpites[0] = defaultLavaSourceStillSprite;
        defaultLavaSourceSpites[1] = defaultLavaSourceFlowSprite;
        FluidRenderHandler lavaSourceRenderHandler = (view, pos, state) -> {
            if (view != null && pos != null && FLUID_SOURCE_HIGHLIGHT.getBooleanValue()) {
                BlockState blockState = view.getBlockState(pos);
                if (blockState.hasProperty(LiquidBlock.LEVEL) && blockState.getValue(LiquidBlock.LEVEL) == 0) {
                    return lavaSourceSpites;
                }
            }
            return defaultLavaSourceSpites;
        };
        FluidRenderHandlerRegistry.INSTANCE.register(Fluids.LAVA, lavaSourceRenderHandler);
        FluidRenderHandlerRegistry.INSTANCE.register(Fluids.FLOWING_LAVA, lavaSourceRenderHandler);
        defaultWaterOverlaySprite = ModelBakery.WATER_OVERLAY.sprite();

        waterSourceStillSprite = atlas.apply(STILL_WATER_SPRITE_ID);
        waterSourceFlowSprite = atlas.apply(FLOWING_WATER_SPRITE_ID);
        waterSourceSpites[0] = waterSourceStillSprite;
        waterSourceSpites[1] = waterSourceFlowSprite;
        waterSourceSpites[2] = defaultWaterOverlaySprite;

        bubbleDownSprite = atlas.apply(DOWN_BUBBLE_SPRITE_ID);
        bubbleUpSprite = atlas.apply(UP_BUBBLE_SPRITE_ID);
        bubbleWaterSpitesDown[0] = waterSourceStillSprite;
        bubbleWaterSpitesDown[1] = bubbleDownSprite;
        bubbleWaterSpitesDown[2] = defaultWaterOverlaySprite;

        bubbleWaterSpitesUp[0] = waterSourceStillSprite;
        bubbleWaterSpitesUp[1] = bubbleUpSprite;
        bubbleWaterSpitesUp[2] = defaultWaterOverlaySprite;

        //? if >=1.21.5 {
        defaultWaterSourceStillSprite = Minecraft.getInstance().getModelManager().getBlockModelShaper().getBlockModel(Blocks.WATER.defaultBlockState()).particleIcon();
        //?} else {
        /*defaultWaterSourceStillSprite = Minecraft.getInstance().getModelManager().getBlockModelShaper().getBlockModel(Blocks.WATER.defaultBlockState()).getParticleIcon();
        *///?}
        defaultWaterSourceFlowSprite = ModelBakery.WATER_FLOW.sprite();

        defaultWaterSourceSpites[0] = defaultWaterSourceStillSprite;
        defaultWaterSourceSpites[1] = defaultWaterSourceFlowSprite;
        defaultWaterSourceSpites[2] = defaultWaterOverlaySprite;

        FluidRenderHandler waterSourceRenderHandler = new FluidRenderHandler() {
            @Override
            public TextureAtlasSprite[] getFluidSprites(@Nullable BlockAndTintGetter view, @Nullable BlockPos pos, FluidState state) {
                if (view != null && pos != null &&  FLUID_SOURCE_HIGHLIGHT.getBooleanValue()) {
                    BlockState blockState = view.getBlockState(pos);
                    if (blockState.is(Blocks.BUBBLE_COLUMN) && blockState.getValue(DRAG)  && view.getFluidState(pos).isSource()) {
                        return bubbleWaterSpitesDown;
                    }
                    if (blockState.is(Blocks.BUBBLE_COLUMN) && !blockState.getValue(DRAG)  && view.getFluidState(pos).isSource()) {
                        return bubbleWaterSpitesUp;
                    }
                    if (view.getFluidState(pos).isSource()) {
                        return waterSourceSpites;
                    }
                }
                return defaultWaterSourceSpites;
            }
            @Override
            public int getFluidColor(@Nullable BlockAndTintGetter view, @Nullable BlockPos pos, FluidState state) {
                return BiomeColors.getAverageWaterColor(view == null? Minecraft.getInstance().level : view, pos == null?new BlockPos(0,0,0):pos);
            }
        };
        FluidRenderHandlerRegistry.INSTANCE.register(Fluids.WATER, waterSourceRenderHandler);
        FluidRenderHandlerRegistry.INSTANCE.register(Fluids.FLOWING_WATER, waterSourceRenderHandler);
    }
}
