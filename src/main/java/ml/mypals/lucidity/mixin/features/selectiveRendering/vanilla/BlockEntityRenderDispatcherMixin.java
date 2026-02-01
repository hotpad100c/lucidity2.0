package ml.mypals.lucidity.mixin.features.selectiveRendering.vanilla;

import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.mojang.blaze3d.vertex.PoseStack;
import ml.mypals.lucidity.config.SelectiveRenderingConfigs;
import ml.mypals.lucidity.features.selectiveRendering.ControllableTransparentBuffersWrapper;
import ml.mypals.lucidity.features.selectiveRendering.SelectiveRenderingManager;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderDispatcher;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;


@Mixin(BlockEntityRenderDispatcher.class)
public class BlockEntityRenderDispatcherMixin {
    @WrapMethod(method = "render")
    private void renderBlockEntity(BlockEntity entity, float f, PoseStack poseStack, MultiBufferSource multiBufferSource, Operation<Void> original) {
        BlockPos pos = entity.getBlockPos();
        BlockState state = entity.getBlockState();
        if(!SelectiveRenderingManager.shouldRenderBlock(state,pos)
                && !SelectiveRenderingConfigs.isBlockFullyHidden()){
            if(multiBufferSource instanceof MultiBufferSource.BufferSource multiBufferSourceBuffer){
                original.call(entity, f, poseStack, new ControllableTransparentBuffersWrapper(multiBufferSourceBuffer));
            }
        }else {
            original.call(entity, f, poseStack, multiBufferSource);
        }
    }
}
