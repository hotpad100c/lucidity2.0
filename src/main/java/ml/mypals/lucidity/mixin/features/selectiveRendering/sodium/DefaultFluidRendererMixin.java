package ml.mypals.lucidity.mixin.features.selectiveRendering.sodium;


import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.caffeinemc.mods.sodium.client.render.chunk.compile.pipeline.DefaultFluidRenderer;
import net.caffeinemc.mods.sodium.client.render.chunk.terrain.DefaultTerrainRenderPasses;
import net.caffeinemc.mods.sodium.client.render.chunk.terrain.material.Material;
import net.caffeinemc.mods.sodium.client.render.chunk.vertex.builder.ChunkMeshBufferBuilder;
import net.caffeinemc.mods.sodium.client.render.chunk.vertex.format.ChunkVertexEncoder;
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
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import static ml.mypals.lucidity.config.LucidityConfigs.Generic.FLUID_TRANSPARENCY;
import static ml.mypals.lucidity.config.FeatureToggle.FLUID_TRANSPARENCY_OVERRIDE;
import static ml.mypals.lucidity.features.selectiveRendering.SelectiveRenderingManager.shouldRenderBlock;

@Mixin(value = DefaultFluidRenderer.class,remap = false)
public class DefaultFluidRendererMixin {
    @Shadow
    @Final
    private int[] quadColors;

    @Inject(at = @At("RETURN"), method = "isFullBlockFluidOccluded"
            , cancellable = true)
    private void isFullBlockFluidOccluded(BlockAndTintGetter world, BlockPos pos, Direction dir, BlockState blockState, FluidState fluid, CallbackInfoReturnable<Boolean> cir) {
        if (!shouldRenderBlock(world.getBlockState(pos.relative(dir)), pos.relative(dir)))
            cir.setReturnValue(false);
    }

    @Inject(at = @At("RETURN"), method = "isSideExposed"
            , cancellable = true)
    private void isSideExposed(BlockAndTintGetter world, int x, int y, int z, Direction dir, float height, CallbackInfoReturnable<Boolean> cir) {
        BlockPos pos = new BlockPos(x, y, z);
        if (!shouldRenderBlock(world.getBlockState(pos.relative(dir)), pos.relative(dir)))
            cir.setReturnValue(true);
    }

    @WrapOperation(
            method = "writeQuad",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/caffeinemc/mods/sodium/client/render/chunk/vertex/builder/ChunkMeshBufferBuilder;push([Lnet/caffeinemc/mods/sodium/client/render/chunk/vertex/format/ChunkVertexEncoder$Vertex;Lnet/caffeinemc/mods/sodium/client/render/chunk/terrain/material/Material;)V"
            )
    )
    private void wrapVertex(ChunkMeshBufferBuilder instance, ChunkVertexEncoder.Vertex[] vertices, Material material, Operation<Void> original) {
        if (!FLUID_TRANSPARENCY_OVERRIDE.getBooleanValue()) {
            original.call(instance, vertices, material);
        } else {
            Material material1 = new Material(DefaultTerrainRenderPasses.TRANSLUCENT,material.alphaCutoff,material.mipped);
            for (int i = 0; i < 4; i++) {
                int color = this.quadColors[i];
                int r = color & 0xFF;
                int g = (color >> 8) & 0xFF;
                int b = (color >> 16) & 0xFF;
                int a = (int) (FLUID_TRANSPARENCY.getFloatValue() * 255.0F) & 0xFF;
                this.quadColors[i] = (a << 24) | (b << 16) | (g << 8) | r;
                vertices[i].color = this.quadColors[i];
            }
            original.call(instance, vertices, material1);
        }
    }
}