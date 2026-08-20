package ml.mypals.lucidity.mixin.features.selectiveRendering.vanilla;

import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.mojang.blaze3d.vertex.PoseStack;
import ml.mypals.lucidity.features.selectiveRendering.SelectiveRenderingManager;
import ml.mypals.lucidity.features.selectiveRendering.SelectiveRenderingSubmitNodeStorage;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.client.renderer.state.CameraRenderState;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderDispatcher;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(value = BlockEntityRenderDispatcher.class)
public class BlockEntityRenderDispatcherMixin {



    @WrapMethod(method = "submit")
    private void renderBlockEntity(BlockEntityRenderState blockEntityRenderState, PoseStack poseStack, SubmitNodeCollector submitNodeCollector, CameraRenderState cameraRenderState, Operation<Void> original) {
        if (!SelectiveRenderingManager.shouldRenderBlock(blockEntityRenderState.blockState,blockEntityRenderState.blockPos)){
            if (SelectiveRenderingManager.isFullyTransparentAt(blockEntityRenderState.blockPos)) {
                return;
            }
            int source = SelectiveRenderingManager.transparencySourceAt(
                    Vec3.atLowerCornerOf(blockEntityRenderState.blockPos), true);
            original.call(blockEntityRenderState, poseStack,new SelectiveRenderingSubmitNodeStorage(submitNodeCollector, source), cameraRenderState);
        }else {
            original.call(blockEntityRenderState, poseStack, submitNodeCollector, cameraRenderState);
        }
    }

}
