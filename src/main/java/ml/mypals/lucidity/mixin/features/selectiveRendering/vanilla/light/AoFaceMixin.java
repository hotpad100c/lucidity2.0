package ml.mypals.lucidity.mixin.features.selectiveRendering.vanilla.light;

import net.minecraft.client.renderer.block.ModelBlockRenderer;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import static ml.mypals.lucidity.features.selectiveRendering.SelectiveRenderingManager.shouldRenderBlock;

//? if >=1.21.5 {
/*@Mixin(ModelBlockRenderer.AmbientOcclusionRenderStorage.class)
*///?} else {
@Mixin(ModelBlockRenderer.AmbientOcclusionFace.class)
//?}
public class AoFaceMixin{
    @Redirect(
            //? if >=1.21.5 {
            /*method = "calculate(Lnet/minecraft/world/level/BlockAndTintGetter;Lnet/minecraft/world/level/block/state/BlockState;Lnet/minecraft/core/BlockPos;Lnet/minecraft/core/Direction;Z)V",
            *///?} else {
            method = "calculate(Lnet/minecraft/world/level/BlockAndTintGetter;Lnet/minecraft/world/level/block/state/BlockState;Lnet/minecraft/core/BlockPos;Lnet/minecraft/core/Direction;[FLjava/util/BitSet;Z)V",
            //?}
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/level/BlockAndTintGetter;getBlockState(Lnet/minecraft/core/BlockPos;)Lnet/minecraft/world/level/block/state/BlockState;"
            )
    )
    private BlockState redirectGetBlockState(BlockAndTintGetter world, BlockPos pos) {
        BlockState state = world.getBlockState(pos);
        if (!shouldRenderBlock(state, pos)) {
            return Blocks.AIR.defaultBlockState();
        }
        return state;
    }
}