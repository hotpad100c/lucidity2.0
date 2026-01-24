package ml.mypals.lucidity.mixin.features.selectiveRendering.vanilla.light;

import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import ml.mypals.lucidity.features.selectiveRendering.SelectiveRenderingManager;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.lighting.LightEngine;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(LightEngine.class)
public abstract class LightEngineMixin {

    @WrapMethod(method = "getState")
    private BlockState lucidity$fakeAirForHiddenBlocks(
            BlockPos blockPos, Operation<BlockState> original
    ) {
        BlockState state = original.call(blockPos);

        if (!SelectiveRenderingManager.shouldRenderBlock(state, blockPos)) {
            return Blocks.AIR.defaultBlockState();
        }
        return state;
    }
}