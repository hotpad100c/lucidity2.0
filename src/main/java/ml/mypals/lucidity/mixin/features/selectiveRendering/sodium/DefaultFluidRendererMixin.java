package ml.mypals.lucidity.mixin.features.selectiveRendering.sodium;


import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import ml.mypals.lucidity.config.SelectiveRenderingConfigs;
import ml.mypals.lucidity.features.selectiveRendering.SelectiveRenderingManager;
import net.caffeinemc.mods.sodium.client.model.color.ColorProvider;
import net.caffeinemc.mods.sodium.client.render.chunk.compile.buffers.ChunkModelBuilder;
import net.caffeinemc.mods.sodium.client.render.chunk.compile.pipeline.DefaultFluidRenderer;
import net.caffeinemc.mods.sodium.client.render.chunk.terrain.DefaultTerrainRenderPasses;
import net.caffeinemc.mods.sodium.client.render.chunk.terrain.material.DefaultMaterials;
import net.caffeinemc.mods.sodium.client.render.chunk.terrain.material.Material;
import net.caffeinemc.mods.sodium.client.render.chunk.translucent_sorting.TranslucentGeometryCollector;
import net.caffeinemc.mods.sodium.client.render.chunk.vertex.builder.ChunkMeshBufferBuilder;
import net.caffeinemc.mods.sodium.client.render.chunk.vertex.format.ChunkVertexEncoder;
import net.caffeinemc.mods.sodium.client.world.LevelSlice;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FluidState;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import static ml.mypals.lucidity.config.LucidityConfigs.Generic.FLUID_TRANSPARENCY;
import static ml.mypals.lucidity.config.FeatureToggle.FLUID_TRANSPARENCY_OVERRIDE;
import static ml.mypals.lucidity.config.SelectiveRenderingConfigs.HIDDEN_BLOCK_TRANSPARENCY;
import static ml.mypals.lucidity.features.selectiveRendering.SelectiveRenderingManager.shouldRenderBlock;

@Mixin(value = DefaultFluidRenderer.class,remap = false)
public class DefaultFluidRendererMixin {
    @Shadow
    @Final
    private int[] quadColors;

    private int alpha;

    @Inject(at = @At("RETURN"), method = "isFullBlockFluidOccluded"
            , cancellable = true)
    private void isFullBlockFluidOccluded(BlockAndTintGetter world, BlockPos pos, Direction dir, BlockState blockState, FluidState fluid, CallbackInfoReturnable<Boolean> cir) {

        boolean renderThis = shouldRenderBlock(world.getBlockState(pos),pos);
        boolean renderNeighbor = shouldRenderBlock(world.getBlockState(pos.relative(dir)), pos.relative(dir));

        if (!SelectiveRenderingConfigs.isBlockFullyHidden()) {
            if (renderThis != renderNeighbor) {
                cir.setReturnValue(false);
            }
        }else {
            if (renderThis != renderNeighbor) {
                cir.setReturnValue(!renderThis);
            }
        }

    }

    @Inject(method = "render", at = @At("HEAD"), cancellable = true)
    private void onRender(LevelSlice level, BlockState state, FluidState fluidState, BlockPos pos, BlockPos offset, TranslucentGeometryCollector collector, ChunkModelBuilder meshBuilder, Material material, ColorProvider<FluidState> colorProvider, TextureAtlasSprite[] sprites, CallbackInfo ci) {
        alpha = SelectiveRenderingManager.shouldRenderBlock(state,pos)?-1: HIDDEN_BLOCK_TRANSPARENCY.getIntegerValue();
        if (alpha == 0) {
            ci.cancel();
        }
    }

    @Inject(at = @At("RETURN"), method = "isSideExposed"
            , cancellable = true)
    private void isSideExposed(BlockAndTintGetter world, int x, int y, int z, Direction dir, float height, CallbackInfoReturnable<Boolean> cir) {
        BlockPos pos = new BlockPos(x, y, z);
        boolean renderThis = shouldRenderBlock(world.getBlockState(pos),pos);
        boolean renderNeighbor = shouldRenderBlock(world.getBlockState(pos.relative(dir)), pos.relative(dir));

        if (!SelectiveRenderingConfigs.isBlockFullyHidden()) {
            if (renderThis != renderNeighbor) {
                cir.setReturnValue(true);
            }
        }else {
            if (renderThis != renderNeighbor) {
                cir.setReturnValue(renderThis);
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
    private void wrapVertex(ChunkMeshBufferBuilder instance, ChunkVertexEncoder.Vertex[] vertices, Material material, Operation<Void> original) {
        int finalAlpha = alpha;

        if (FLUID_TRANSPARENCY_OVERRIDE.getBooleanValue()) {
            finalAlpha = (int)(FLUID_TRANSPARENCY.getFloatValue() * 255.0F);
        }

        if (finalAlpha > -1) {
            int a = finalAlpha << 24;
            for (int i = 0; i < 4; i++) {
                quadColors[i] = (quadColors[i] & 0x00FFFFFF) | a;
                vertices[i].color = quadColors[i];
            }
            original.call(instance, vertices, DefaultMaterials.TRANSLUCENT);
        } else {
            original.call(instance, vertices, material);
        }

    }
}