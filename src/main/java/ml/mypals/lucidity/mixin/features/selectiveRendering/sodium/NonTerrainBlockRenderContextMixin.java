package ml.mypals.lucidity.mixin.features.selectiveRendering.sodium;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import ml.mypals.lucidity.config.SelectiveRenderingConfigs;
import ml.mypals.lucidity.features.selectiveRendering.SelectiveRenderingManager;
import net.caffeinemc.mods.sodium.client.render.frapi.mesh.MutableQuadViewImpl;
import net.caffeinemc.mods.sodium.client.render.frapi.render.NonTerrainBlockRenderContext;
//? if >=1.21.6 {
import net.fabricmc.fabric.api.renderer.v1.render.BlockVertexConsumerProvider;
import net.minecraft.client.renderer.chunk.ChunkSectionLayer;
//?} else {
/*import net.fabricmc.fabric.api.renderer.v1.material.BlendMode;
*///?}
import net.minecraft.client.color.block.BlockColors;
import net.minecraft.client.renderer.MultiBufferSource;
//? if >=1.21.5 {
import net.minecraft.client.renderer.block.model.BlockStateModel;
//?}
//? if <1.21.5 {
/*import net.minecraft.client.resources.model.BakedModel;
*///?}
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = NonTerrainBlockRenderContext.class, remap = false)
public abstract class NonTerrainBlockRenderContextMixin {
    //? if >=1.21.6 {
    @Shadow protected abstract VertexConsumer getVertexConsumer(ChunkSectionLayer par1);
    //?} else if >= 1.21.5 {
    /*@Shadow protected abstract VertexConsumer getVertexConsumer(BlendMode par1);
    *///?} else {
    //?}
    @Unique
    private int alpha;

    @Inject(method = "renderModel", at = @At("HEAD"), cancellable = true)
    //? if >=1.21.6 {
    
    private void onRenderModel(BlockAndTintGetter blockView, BlockColors blockColors, BlockStateModel model, BlockState state, BlockPos pos, PoseStack poseStack, BlockVertexConsumerProvider buffer, boolean cull, long seed, int overlay, CallbackInfo ci) {
    //?} else if >= 1.21.5 {
    /*private void onRenderModel(BlockAndTintGetter blockView, BlockColors blockColors, BlockStateModel model, BlockState state, BlockPos pos, PoseStack poseStack, MultiBufferSource buffer, boolean cull, long seed, int overlay, CallbackInfo ci) {
    *///?} else {
    /*private void onRenderModel(BlockAndTintGetter blockView, BakedModel model, BlockState state, BlockPos pos, PoseStack poseStack, VertexConsumer buffer, boolean cull, RandomSource random, long seed, int overlay, CallbackInfo ci) {
    *///?}
        alpha = SelectiveRenderingManager.shouldRenderBlock(state,pos)?-1: SelectiveRenderingConfigs.HIDDEN_BLOCK_TRANSPARENCY.getIntegerValue();
        if (alpha == 0) {
            ci.cancel();
        }
    }

    @Inject(method = "bufferQuad", at = @At("HEAD"))
    //? if >=1.21.5 {
    private void onBufferQuad(MutableQuadViewImpl quad, VertexConsumer vertexConsumer, CallbackInfo ci) {
    //?} else {
    /*private void onBufferQuad(MutableQuadViewImpl quad, CallbackInfo ci) {
    *///?}
        if (alpha > -1) {
            for (int i = 0; i < 4; i++) {
                int color = quad.getColor(i);
                quad.color(i, ((alpha & 0xFF) << 24) | (color & 0x00FFFFFF));
            }
        }
    }
    //? if >=1.21.5 {
    @ModifyArg(method = "processQuad", at = @At(value = "INVOKE", target = "Lnet/caffeinemc/mods/sodium/client/render/frapi/render/NonTerrainBlockRenderContext;bufferQuad(Lnet/caffeinemc/mods/sodium/client/render/frapi/mesh/MutableQuadViewImpl;Lcom/mojang/blaze3d/vertex/VertexConsumer;)V"), index = 1)
    private VertexConsumer modifyMaterial(VertexConsumer par2) {
        if (alpha > -1) {
            //? if >=1.21.6 {
            return this.getVertexConsumer(ChunkSectionLayer.TRANSLUCENT);
            //?} else {
            /*return this.getVertexConsumer(BlendMode.TRANSLUCENT);
            *///?}
        }
        return par2;
    }
    //?}
}
