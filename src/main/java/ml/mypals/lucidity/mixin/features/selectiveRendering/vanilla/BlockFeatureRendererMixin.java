package ml.mypals.lucidity.mixin.features.selectiveRendering.vanilla;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import ml.mypals.lucidity.config.SelectiveRenderingConfigs;
import ml.mypals.lucidity.features.selectiveRendering.*;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.ItemBlockRenderTypes;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.ModelBlockRenderer;
//? if >=1.21.9 {
import net.minecraft.client.renderer.feature.BlockFeatureRenderer;
//?}
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Debug;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import java.util.List;

//? if >=1.21.9 {
@Debug(export = true)
@Mixin(BlockFeatureRenderer.class)
public class BlockFeatureRendererMixin {
    @WrapOperation(
            method = "render",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/renderer/block/ModelBlockRenderer;tesselateBlock(Lnet/minecraft/world/level/BlockAndTintGetter;Ljava/util/List;Lnet/minecraft/world/level/block/state/BlockState;Lnet/minecraft/core/BlockPos;Lcom/mojang/blaze3d/vertex/PoseStack;Lcom/mojang/blaze3d/vertex/VertexConsumer;ZI)V"
            )
    )

    private void wrapTesselateBlock(
            ModelBlockRenderer instance,
            BlockAndTintGetter blockAndTintGetter,
            List list,
            BlockState state,
            BlockPos pos,
            PoseStack poseStack,
            VertexConsumer vertexConsumer,
            boolean b, int i,
            Operation<Void> original,
            @Local(argsOnly = true) MultiBufferSource.BufferSource bufferSource
    ) {


        if (!SelectiveRenderingManager.shouldRenderBlock(state, pos) && !SelectiveRenderingConfigs.isBlockFullyHidden()) {

            VertexConsumer wrapped =
                    new ControllableTransparentVertexConsumer(bufferSource.getBuffer(RenderType.translucentMovingBlock()));

            original.call(instance, blockAndTintGetter, list, state, pos, poseStack, wrapped, b, i);

        } else {

            original.call(instance, blockAndTintGetter, list, state, pos, poseStack, vertexConsumer, b, i);
        }
    }
}
//?} else {
/*@Mixin(Minecraft.class)
public class BlockFeatureRendererMixin {
}
*///?}
