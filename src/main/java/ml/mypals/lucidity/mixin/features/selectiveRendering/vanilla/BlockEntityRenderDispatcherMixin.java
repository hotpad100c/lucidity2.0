package ml.mypals.lucidity.mixin.features.selectiveRendering.vanilla;

import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.mojang.blaze3d.vertex.PoseStack;
import ml.mypals.lucidity.config.SelectiveRenderingConfigs;
import ml.mypals.lucidity.features.selectiveRendering.ControllableTransparentBuffersWrapper;
import ml.mypals.lucidity.features.selectiveRendering.SelectiveRenderingManager;
import ml.mypals.lucidity.features.selectiveRendering.SelectiveRenderingSubmitNodeStorage;
import net.minecraft.client.renderer.MultiBufferSource;
//? if >=1.21.9 {
/*import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.client.renderer.state.CameraRenderState;
*///?}
import net.minecraft.client.renderer.blockentity.BlockEntityRenderDispatcher;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(value = BlockEntityRenderDispatcher.class)
public class BlockEntityRenderDispatcherMixin {


    //? if >=1.21.9 {

    /*@WrapMethod(method = "submit")
    private void renderBlockEntity(BlockEntityRenderState blockEntityRenderState, PoseStack poseStack, SubmitNodeCollector submitNodeCollector, CameraRenderState cameraRenderState, Operation<Void> original) {
        if (!SelectiveRenderingManager.shouldRenderBlock(blockEntityRenderState.blockState,blockEntityRenderState.blockPos)
                && !SelectiveRenderingConfigs.isBlockFullyHidden()){
            original.call(blockEntityRenderState, poseStack,new SelectiveRenderingSubmitNodeStorage(submitNodeCollector), cameraRenderState);
        }else {
            original.call(blockEntityRenderState, poseStack, submitNodeCollector, cameraRenderState);
        }
    }
    *///?} else {
    @WrapMethod(method = "render(Lnet/minecraft/world/level/block/entity/BlockEntity;FLcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;)V")
    private void renderBlockEntity(BlockEntity entity, float f, PoseStack poseStack, MultiBufferSource multiBufferSource, Operation<Void> original) {
        BlockPos pos = entity.getBlockPos();
        BlockState state = entity.getBlockState();
        if(!SelectiveRenderingManager.shouldRenderBlock(state,pos)
                && !SelectiveRenderingConfigs.isBlockFullyHidden()){
            original.call(entity, f, poseStack, new ControllableTransparentBuffersWrapper((MultiBufferSource.BufferSource) multiBufferSource));
        }else {
            original.call(entity, f, poseStack, multiBufferSource);
        }
    }
    //?}
}
