package ml.mypals.lucidity.mixin.features.selectiveRendering.fabric.light;

import ml.mypals.lucidity.config.SelectiveRenderingConfigs;
import net.fabricmc.fabric.impl.client.indigo.renderer.aocalc.AoCalculator;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import static ml.mypals.lucidity.features.selectiveRendering.SelectiveRenderingManager.shouldRenderBlock;

@SuppressWarnings("UnstableApiUsage")
@Mixin(value = AoCalculator.class,remap = false)
public class AoCalculatorMixin {
    @Redirect(
            method = "computeFace(Lnet/fabricmc/fabric/impl/client/indigo/renderer/aocalc/AoFaceData;Lnet/minecraft/core/Direction;ZZ)V",
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
