package ml.mypals.lucidity.mixin.features.selectiveRendering.vanilla;

import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import static ml.mypals.lucidity.features.selectiveRendering.SelectiveRenderingManager.shouldRenderBlock;

@Mixin(Block.class)
public class BlockMixin {
    //? if <=1.21.1 {
    /*@WrapMethod(method = "shouldRenderFace(Lnet/minecraft/world/level/block/state/BlockState;Lnet/minecraft/world/level/BlockGetter;Lnet/minecraft/core/BlockPos;Lnet/minecraft/core/Direction;Lnet/minecraft/core/BlockPos;)Z")
    private static boolean onRenderSmoothOrFlat(BlockState state, BlockGetter blockGetter, BlockPos pos, Direction side, BlockPos blockPos2, Operation<Boolean> original)
    {
        boolean shouldRender = shouldRenderBlock(state,pos);
        boolean shouldRenderNeighbor = shouldRenderBlock(blockGetter.getBlockState(pos.relative(side)),pos.relative(side));
        if (shouldRender != shouldRenderNeighbor) {
            return shouldRender;
        }
        return original.call(state, blockGetter, pos, side, blockPos2);
    }
    *///?}
}
