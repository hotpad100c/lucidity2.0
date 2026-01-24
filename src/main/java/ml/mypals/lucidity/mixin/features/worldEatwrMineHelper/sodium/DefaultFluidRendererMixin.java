package ml.mypals.lucidity.mixin.features.worldEatwrMineHelper.sodium;


import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import ml.mypals.lucidity.features.worldEaterHelper.WorldEaterHelperManager;
import net.caffeinemc.mods.sodium.client.model.color.ColorProvider;
import net.caffeinemc.mods.sodium.client.model.light.LightMode;
import net.caffeinemc.mods.sodium.client.model.light.LightPipeline;
import net.caffeinemc.mods.sodium.client.model.light.data.QuadLightData;
import net.caffeinemc.mods.sodium.client.model.quad.ModelQuadViewMutable;
import net.caffeinemc.mods.sodium.client.model.quad.properties.ModelQuadFacing;
import net.caffeinemc.mods.sodium.client.render.chunk.compile.buffers.ChunkModelBuilder;
import net.caffeinemc.mods.sodium.client.render.chunk.compile.pipeline.DefaultFluidRenderer;
import net.caffeinemc.mods.sodium.client.render.chunk.terrain.material.Material;
import net.caffeinemc.mods.sodium.client.render.chunk.translucent_sorting.TranslucentGeometryCollector;
import net.caffeinemc.mods.sodium.client.render.chunk.vertex.builder.ChunkMeshBufferBuilder;
import net.caffeinemc.mods.sodium.client.render.chunk.vertex.format.ChunkVertexEncoder;
import net.caffeinemc.mods.sodium.client.world.LevelSlice;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FluidState;
import org.joml.Matrix4f;
import org.joml.Quaternionf;
import org.joml.Vector3f;
import org.joml.Vector4f;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import static ml.mypals.lucidity.config.LucidityConfigs.Generic.WORLD_EATER_MINE_HELPER_HEIGHT;
import static ml.mypals.lucidity.config.FeatureToggle.WORLD_EATER_MINE_HELPER;

@Mixin(value = DefaultFluidRenderer.class,remap = false)
public class DefaultFluidRendererMixin {

    @Shadow @Final private float[] brightness;
    @Shadow @Final private QuadLightData quadLightData;
    @Unique
    private static final ThreadLocal<Boolean> isRenderingTransformed = ThreadLocal.withInitial(() -> false);

    @Unique
    private static final ThreadLocal<Matrix4f> transformMatrix = new ThreadLocal<>();

    @Unique
    private static final ThreadLocal<BlockPos> currentBlockPos = new ThreadLocal<>();

    @Inject(
            method = "updateQuad",
            at = @At(
                    value = "FIELD",
                    target = "Lnet/caffeinemc/mods/sodium/client/render/chunk/compile/pipeline/DefaultFluidRenderer;brightness:[F",
                    ordinal = 0,
                    shift = At.Shift.AFTER
            ),
            remap = false
    )
    private void redirectBrightness(ModelQuadViewMutable quad, LevelSlice level, BlockPos pos, LightPipeline lighter, Direction dir, ModelQuadFacing facing, float brightness, ColorProvider<FluidState> colorProvider, FluidState fluidState, CallbackInfo ci) {
        if (isRenderingTransformed.get()) {
            float[] brightnessArray = this.brightness;
            for (int i = 0; i < 4; i++) {
                brightnessArray[i] = 1.0f;
            }
        }
    }

    @Inject(method = "writeQuad", at = @At("HEAD"), remap = false)
    private void boostLightmap(CallbackInfo ci) {
        if (isRenderingTransformed.get()) {
            QuadLightData lightData = this.quadLightData;
            for (int i = 0; i < 4; i++) {
                lightData.lm[i] = LightTexture.FULL_BRIGHT;
            }
        }
    }

    @WrapMethod(
            method = "render"
    )
    private void onRenderStart(
            LevelSlice level, BlockState blockState, FluidState fluidState, BlockPos blockPos, BlockPos offset, TranslucentGeometryCollector collector, ChunkModelBuilder meshBuilder, Material material, ColorProvider<FluidState> colorProvider, TextureAtlasSprite[] sprites, Operation<Void> original
    ) {
        original.call(level, blockState, fluidState, blockPos, offset, collector, meshBuilder, material, colorProvider, sprites);
        currentBlockPos.set(blockPos);

        if (!isRenderingTransformed.get() &&
                WORLD_EATER_MINE_HELPER.getBooleanValue() &&
                WorldEaterHelperManager.shouldRender(blockState, blockPos)) {

            isRenderingTransformed.set(true);

            PoseStack poseStack = new PoseStack();
            float relX = (float)(blockPos.getX() & 15);
            float relY = (float)(blockPos.getY() & 15);
            float relZ = (float)(blockPos.getZ() & 15);

            poseStack.translate(relX + 0.5, relY + 0.5 + WORLD_EATER_MINE_HELPER_HEIGHT.getFloatValue(), relZ + 0.5);

            Quaternionf rotation = new Quaternionf().rotationTo(
                    new Vector3f(1, 1, 1).normalize(),
                    new Vector3f(0, 1, 0)
            );
            poseStack.mulPose(rotation);
            poseStack.mulPose(Axis.YP.rotationDegrees(45f));

            poseStack.translate(-(relX + 0.5), -(relY + 0.5), -(relZ + 0.5));

            transformMatrix.set(poseStack.last().pose());

            original.call(level, blockState, fluidState, blockPos, offset, collector, meshBuilder, material, colorProvider, sprites);

            if (isRenderingTransformed.get()) {
                isRenderingTransformed.set(false);
                transformMatrix.remove();
                currentBlockPos.remove();
            }
        }
    }

    @WrapOperation(
            method = "writeQuad",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/caffeinemc/mods/sodium/client/render/chunk/vertex/builder/ChunkMeshBufferBuilder;push([Lnet/caffeinemc/mods/sodium/client/render/chunk/vertex/format/ChunkVertexEncoder$Vertex;Lnet/caffeinemc/mods/sodium/client/render/chunk/terrain/material/Material;)V"
            )
    )
    private void transformVertices(ChunkMeshBufferBuilder instance, ChunkVertexEncoder.Vertex[] vertices, Material material, Operation<Void> original) {
        if(!isRenderingTransformed.get()){
            original.call(instance, vertices, material);
        }else {
            Matrix4f matrix = transformMatrix.get();
            if (matrix != null) {
                for (ChunkVertexEncoder.Vertex vertex : vertices) {
                    Vector4f pos = new Vector4f(vertex.x, vertex.y, vertex.z, 1.0f);
                    pos.mul(matrix);
                    vertex.x = pos.x;
                    vertex.y = pos.y;
                    vertex.z = pos.z;
                }
                original.call(instance, vertices, material);
            }
        }
    }

    @WrapMethod(method = "isSideExposed")
    private boolean redirectShouldRenderFace(
            BlockAndTintGetter world, int x, int y, int z, Direction dir, float height, Operation<Boolean> original
    ) {
        return isRenderingTransformed.get() || original.call(world, x, y, z, dir, height);
    }

    @WrapMethod(method = "isFullBlockFluidOccluded")
    private boolean redirectIsFaceOccluded(
            BlockAndTintGetter world, BlockPos pos, Direction dir, BlockState blockState, FluidState fluid, Operation<Boolean> original
    ) {
        return !isRenderingTransformed.get() && original.call(world, pos, dir, blockState, fluid);
    }
}