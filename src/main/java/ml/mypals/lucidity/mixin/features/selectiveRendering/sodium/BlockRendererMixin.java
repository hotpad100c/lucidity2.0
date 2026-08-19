package ml.mypals.lucidity.mixin.features.selectiveRendering.sodium;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import ml.mypals.lucidity.features.selectiveRendering.SelectiveRenderingManager;
import net.caffeinemc.mods.sodium.client.render.chunk.compile.pipeline.BlockRenderer;
import net.caffeinemc.mods.sodium.client.render.model.MutableQuadViewImpl;
import net.minecraft.client.renderer.block.model.BlockStateModel;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import net.caffeinemc.mods.sodium.client.render.chunk.terrain.material.DefaultMaterials;
import net.caffeinemc.mods.sodium.client.render.chunk.terrain.material.Material;


import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = BlockRenderer.class,remap = false)
public class BlockRendererMixin {
    @Unique
    private int alpha;

    @Inject(method = "renderModel", at = @At("HEAD"), cancellable = true)

    private void onRenderModel(BlockStateModel model, BlockState state, BlockPos pos, BlockPos origin, CallbackInfo ci) {
        alpha = SelectiveRenderingManager.shouldRenderBlock(state,pos)?-1:SelectiveRenderingManager.hiddenTransparencyAt(pos);
    }

    @Inject(method = "bufferQuad", at = @At("HEAD"))
    private void onBufferQuad(MutableQuadViewImpl quad, float[] brightnesses, Material material, CallbackInfo ci) {
        if (alpha > -1) {
            for (int i = 0; i < 4; i++) {
                int color = quad.getColor(i);
                quad.setColor(i, ((alpha & 0xFF) << 24) | (color & 0x00FFFFFF));
            }
        }
    }

    @WrapOperation(method = "processQuad", at = @At(value = "INVOKE", target = "Lnet/caffeinemc/mods/sodium/client/render/chunk/compile/pipeline/BlockRenderer;bufferQuad(Lnet/caffeinemc/mods/sodium/client/render/model/MutableQuadViewImpl;[FLnet/caffeinemc/mods/sodium/client/render/chunk/terrain/material/Material;)V"))
    private void modifyMaterial(BlockRenderer instance, MutableQuadViewImpl quad, float[] brightnesses, Material material, Operation<Void> original) {
        if (alpha > -1) {
            original.call(instance, quad, brightnesses, DefaultMaterials.TRANSLUCENT);
            return;
        }
        original.call(instance, quad, brightnesses, material);
    }
}
