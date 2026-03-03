package ml.mypals.lucidity.mixin.features.selectiveRendering.sodium;

import ml.mypals.lucidity.config.SelectiveRenderingConfigs;
import ml.mypals.lucidity.features.selectiveRendering.SelectiveRenderingManager;
import net.caffeinemc.mods.sodium.client.render.chunk.compile.pipeline.BlockRenderer;
import net.caffeinemc.mods.sodium.client.render.frapi.mesh.MutableQuadViewImpl;
//? if <=1.21.4 {
/*import net.minecraft.client.resources.model.BakedModel;
*///?} else {
import net.minecraft.client.renderer.block.model.BlockStateModel;
//?}
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import net.caffeinemc.mods.sodium.client.render.chunk.compile.pipeline.BlockRenderer;
import net.caffeinemc.mods.sodium.client.render.chunk.terrain.material.DefaultMaterials;
import net.caffeinemc.mods.sodium.client.render.chunk.terrain.material.Material;


import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = BlockRenderer.class,remap = false)
public class BlockRendererMixin {
    @Unique
    private int alpha;

    @Inject(method = "renderModel", at = @At("HEAD"), cancellable = true)

    //? if <=1.21.4 {
    /*private void onRenderModel(BakedModel model, BlockState state, BlockPos pos, BlockPos origin, CallbackInfo ci) {
    *///?} else {
    private void onRenderModel(BlockStateModel model, BlockState state, BlockPos pos, BlockPos origin, CallbackInfo ci) {
    //?}
        alpha = SelectiveRenderingManager.shouldRenderBlock(state,pos)?-1:SelectiveRenderingConfigs.HIDDEN_BLOCK_TRANSPARENCY.getIntegerValue();
        if (alpha == 0) {
            ci.cancel();
        }
    }

    @Inject(method = "bufferQuad", at = @At("HEAD"))
    private void onBufferQuad(MutableQuadViewImpl quad, float[] brightnesses, Material material, CallbackInfo ci) {
        if (alpha > -1) {
            for (int i = 0; i < 4; i++) {
                int color = quad.getColor(i);
                quad.color(i, ((alpha & 0xFF) << 24) | (color & 0x00FFFFFF));
            }
        }
    }

    @ModifyArg(method = "processQuad", at = @At(value = "INVOKE", target = "Lnet/caffeinemc/mods/sodium/client/render/chunk/compile/pipeline/BlockRenderer;bufferQuad(Lnet/caffeinemc/mods/sodium/client/render/frapi/mesh/MutableQuadViewImpl;[FLnet/caffeinemc/mods/sodium/client/render/chunk/terrain/material/Material;)V"), index = 2)
    private Material modifyMaterial(Material material) {
        if (alpha > -1) {
            return DefaultMaterials.TRANSLUCENT;
        }
        return material;
    }
}
