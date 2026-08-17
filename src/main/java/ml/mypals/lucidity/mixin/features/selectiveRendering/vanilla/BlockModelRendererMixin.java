package ml.mypals.lucidity.mixin.features.selectiveRendering.vanilla;


import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import ml.mypals.lucidity.config.SelectiveRenderingConfigs;
import ml.mypals.lucidity.features.selectiveRendering.ControllableTransparentVertexConsumer;
import ml.mypals.lucidity.features.selectiveRendering.SelectiveRenderingManager;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.rendertype.*;
import net.minecraft.client.renderer.block.ModelBlockRenderer;
import net.minecraft.client.renderer.block.model.BlockModelPart;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import java.util.List;

import static ml.mypals.lucidity.features.selectiveRendering.SelectiveRenderingManager.shouldRenderBlock;

@Mixin(ModelBlockRenderer.class)
public class BlockModelRendererMixin {
    @WrapOperation(at = @At(value = "INVOKE",
            target = "Lnet/minecraft/client/renderer/block/ModelBlockRenderer;shouldRenderFace(Lnet/minecraft/world/level/BlockAndTintGetter;Lnet/minecraft/world/level/block/state/BlockState;ZLnet/minecraft/core/Direction;Lnet/minecraft/core/BlockPos;)Z"),
            method = {"tesselateWithAO(Lnet/minecraft/world/level/BlockAndTintGetter;Ljava/util/List;Lnet/minecraft/world/level/block/state/BlockState;Lnet/minecraft/core/BlockPos;Lcom/mojang/blaze3d/vertex/PoseStack;Lcom/mojang/blaze3d/vertex/VertexConsumer;ZI)V",
                    "tesselateWithoutAO(Lnet/minecraft/world/level/BlockAndTintGetter;Ljava/util/List;Lnet/minecraft/world/level/block/state/BlockState;Lnet/minecraft/core/BlockPos;Lcom/mojang/blaze3d/vertex/PoseStack;Lcom/mojang/blaze3d/vertex/VertexConsumer;ZI)V"})
    private boolean onRenderSmoothOrFlat(BlockAndTintGetter blockAndTintGetter, BlockState state, boolean b, Direction side, BlockPos blockPos, Operation<Boolean> original, @Local(argsOnly = true) BlockAndTintGetter world, @Local(argsOnly = true) BlockPos pos)
    {
        boolean shouldRender = shouldRenderBlock(state,pos);
        boolean shouldRenderNeighbor = shouldRenderBlock(world.getBlockState(pos.relative(side)),pos.relative(side));
        if (shouldRender != shouldRenderNeighbor) {
            return shouldRender;
        }
        return original.call(blockAndTintGetter, state, b, side, blockPos);
    }
    @WrapMethod(method = "tesselateBlock")
    private void tesselateBlock(BlockAndTintGetter blockAndTintGetter, List<BlockModelPart> list, BlockState blockState, BlockPos blockPos, PoseStack poseStack, VertexConsumer vertexConsumer, boolean bl, int i, Operation<Void> original){
        if(!SelectiveRenderingManager.shouldRenderBlock(blockState,blockPos)){
            VertexConsumer consumer = Minecraft.getInstance().renderBuffers().bufferSource().getBuffer(RenderTypes.translucentMovingBlock());
            original.call(blockAndTintGetter, list, blockState, blockPos, poseStack, new ControllableTransparentVertexConsumer(vertexConsumer), bl, i);
            return;
        }
        original.call(blockAndTintGetter, list, blockState, blockPos, poseStack, vertexConsumer, bl, i);
    }
}
