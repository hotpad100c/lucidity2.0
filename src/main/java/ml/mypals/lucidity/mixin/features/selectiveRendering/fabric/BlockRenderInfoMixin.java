package ml.mypals.lucidity.mixin.features.selectiveRendering.fabric;

import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import ml.mypals.lucidity.config.SelectiveRenderingConfigs;
import net.fabricmc.fabric.impl.client.indigo.renderer.render.BlockRenderInfo;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

import static ml.mypals.lucidity.features.selectiveRendering.SelectiveRenderingManager.shouldRenderBlock;

@SuppressWarnings("UnstableApiUsage")
@Mixin(value = BlockRenderInfo.class,remap = false)
public class BlockRenderInfoMixin {
    @Shadow public BlockState blockState;

    @Shadow public BlockPos blockPos;

    @Shadow public BlockAndTintGetter blockView;

    @Shadow @Final private BlockPos.MutableBlockPos searchPos;

    //? if >=1.21.3 {

    //? if > 1.21.3 {
    @WrapMethod(method = "shouldDrawSide")
    //?} else {
    /*@WrapMethod(method = "shouldDrawFace")
    *///?}
    boolean shouldDrawSide(Direction side, Operation<Boolean> original) {
        if (side == null) return original.call(side);
        BlockPos neighborPos = this.searchPos.setWithOffset(this.blockPos, side);
        BlockState neighbor = this.blockView.getBlockState(neighborPos);

        boolean renderThis = shouldRenderBlock(this.blockState, this.blockPos);

        boolean renderNeighbor = shouldRenderBlock(neighbor, neighborPos);

        if (!SelectiveRenderingConfigs.isBlockFullyHidden()) {
            if (renderThis != renderNeighbor) {
                return true;
            }
        }

        if (renderThis != renderNeighbor) {
            return renderThis;
        }
        return original.call(side);
    }

    //?}
}
