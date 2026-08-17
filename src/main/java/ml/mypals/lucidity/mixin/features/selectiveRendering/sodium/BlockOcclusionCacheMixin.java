package ml.mypals.lucidity.mixin.features.selectiveRendering.sodium;

import ml.mypals.lucidity.config.SelectiveRenderingConfigs;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import static ml.mypals.lucidity.features.selectiveRendering.SelectiveRenderingManager.shouldRenderBlock;

// sodium 0.8 删掉了 BlockOcclusionCache，shouldDrawSide 挪到了 AbstractBlockRenderContext，
// 并且从四个参数收成了 shouldDrawSide(Direction)——方块位置和状态改从实例字段读。
// （类名沿用旧的，避免改动 mixin 配置。）
import net.caffeinemc.mods.sodium.client.render.model.AbstractBlockRenderContext;
import net.minecraft.world.level.BlockAndTintGetter;

@Mixin(value = AbstractBlockRenderContext.class, remap = false)
public class BlockOcclusionCacheMixin {

    @Shadow protected BlockState state;
    @Shadow protected BlockPos pos;
    @Shadow protected BlockAndTintGetter level;

    @Inject(at = @At("HEAD"), method = "shouldDrawSide", remap = false, cancellable = true)
    private void filterShouldDrawSide(Direction facing, CallbackInfoReturnable<Boolean> cir) {
        BlockPos neighborPos = this.pos.relative(facing);
        boolean renderThis = shouldRenderBlock(this.state, this.pos);
        boolean renderNeighbor = shouldRenderBlock(this.level.getBlockState(neighborPos), neighborPos);

        if (renderThis == renderNeighbor) {
            return;
        }
        cir.setReturnValue(true);
    }
}
