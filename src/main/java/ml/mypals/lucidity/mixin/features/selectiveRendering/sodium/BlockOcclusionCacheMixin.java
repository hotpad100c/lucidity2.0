package ml.mypals.lucidity.mixin.features.selectiveRendering.sodium;

import net.caffeinemc.mods.sodium.client.render.chunk.compile.pipeline.BlockOcclusionCache;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import static ml.mypals.lucidity.features.selectiveRendering.SelectiveRenderingManager.shouldRenderBlock;

@Mixin(value = BlockOcclusionCache.class,remap = false)
public class BlockOcclusionCacheMixin {
    @Inject(at = @At("HEAD"), method = "shouldDrawSide", remap = false, cancellable = true)
    private void filterShouldDrawSide(BlockState selfBlockState, BlockGetter view, BlockPos selfPos, Direction facing, CallbackInfoReturnable<Boolean> cir) {

        boolean shouldRender = shouldRenderBlock(selfBlockState,selfPos);
        boolean shouldRenderNeighbor = shouldRenderBlock(view.getBlockState(selfPos.relative(facing)),selfPos.relative(facing));

        if (shouldRender && !shouldRenderNeighbor) {
            cir.setReturnValue(true);
        }
        else if (!shouldRender && shouldRenderNeighbor) {
            cir.setReturnValue(false);
        }
    }
}