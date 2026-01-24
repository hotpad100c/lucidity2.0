package ml.mypals.lucidity.mixin.features.selectiveRendering.vanilla;


import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.client.renderer.block.ModelBlockRenderer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import static ml.mypals.lucidity.features.selectiveRendering.SelectiveRenderingManager.shouldRenderBlock;

@Mixin(ModelBlockRenderer.class)
public class BlockModelRendererMixin {
    //? if >=1.21.3 {
    @WrapOperation(at = @At(value = "INVOKE",
            target = "Lnet/minecraft/world/level/block/Block;shouldRenderFace(Lnet/minecraft/world/level/block/state/BlockState;Lnet/minecraft/world/level/block/state/BlockState;Lnet/minecraft/core/Direction;)Z"),
            method = {"tesselateWithAO(Lnet/minecraft/world/level/BlockAndTintGetter;Lnet/minecraft/client/resources/model/BakedModel;Lnet/minecraft/world/level/block/state/BlockState;Lnet/minecraft/core/BlockPos;Lcom/mojang/blaze3d/vertex/PoseStack;Lcom/mojang/blaze3d/vertex/VertexConsumer;ZLnet/minecraft/util/RandomSource;JI)V",
                    "tesselateWithoutAO(Lnet/minecraft/world/level/BlockAndTintGetter;Lnet/minecraft/client/resources/model/BakedModel;Lnet/minecraft/world/level/block/state/BlockState;Lnet/minecraft/core/BlockPos;Lcom/mojang/blaze3d/vertex/PoseStack;Lcom/mojang/blaze3d/vertex/VertexConsumer;ZLnet/minecraft/util/RandomSource;JI)V"})
    private boolean onRenderSmoothOrFlat(BlockState state, BlockState otherState,
                                         Direction side, Operation<Boolean> original, @Local(argsOnly = true) BlockAndTintGetter world, @Local(argsOnly = true) BlockPos pos)
    {
        boolean shouldRender = shouldRenderBlock(state,pos);
        boolean shouldRenderNeighbor = shouldRenderBlock(world.getBlockState(pos.relative(side)),pos.relative(side));
        if (shouldRender != shouldRenderNeighbor) {
            return shouldRender;
        }
        return original.call(state, otherState, side);
    }
    //?}
}
