package ml.mypals.lucidity.mixin.features.selectiveRendering.sodium;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import ml.mypals.lucidity.config.SelectiveRenderingConfigs;
import ml.mypals.lucidity.features.selectiveRendering.SelectiveRenderingManager;
import net.caffeinemc.mods.sodium.client.render.model.MutableQuadViewImpl;
import net.caffeinemc.mods.sodium.client.render.frapi.render.NonTerrainBlockRenderContext;
import net.fabricmc.fabric.api.renderer.v1.render.BlockVertexConsumerProvider;
import net.minecraft.client.renderer.chunk.ChunkSectionLayer;
import net.minecraft.client.color.block.BlockColors;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.block.model.BlockStateModel;
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
    @Shadow protected abstract VertexConsumer getVertexConsumer(ChunkSectionLayer par1);
    @Unique
    private int alpha;

    @Inject(method = "renderModel", at = @At("HEAD"), cancellable = true)
    
    private void onRenderModel(BlockAndTintGetter blockView, BlockColors blockColors, BlockStateModel model, BlockState state, BlockPos pos, PoseStack poseStack, BlockVertexConsumerProvider buffer, boolean cull, long seed, int overlay, CallbackInfo ci) {
        alpha = SelectiveRenderingManager.shouldRenderBlock(state,pos)?-1: SelectiveRenderingConfigs.HIDDEN_BLOCK_TRANSPARENCY.getIntegerValue();
    }

    @Inject(method = "bufferQuad", at = @At("HEAD"))
    private void onBufferQuad(MutableQuadViewImpl quad, VertexConsumer vertexConsumer, CallbackInfo ci) {
        if (alpha > -1) {
            for (int i = 0; i < 4; i++) {
                int color = quad.getColor(i);
                quad.setColor(i, ((alpha & 0xFF) << 24) | (color & 0x00FFFFFF));
            }
        }
    }
    @ModifyArg(method = "processQuad", at = @At(value = "INVOKE", target = "Lnet/caffeinemc/mods/sodium/client/render/frapi/render/NonTerrainBlockRenderContext;bufferQuad(Lnet/caffeinemc/mods/sodium/client/render/model/MutableQuadViewImpl;Lcom/mojang/blaze3d/vertex/VertexConsumer;)V"), index = 1)
    private VertexConsumer modifyMaterial(VertexConsumer par2) {
        if (alpha > -1) {
            return this.getVertexConsumer(ChunkSectionLayer.TRANSLUCENT);
        }
        return par2;
    }

}
