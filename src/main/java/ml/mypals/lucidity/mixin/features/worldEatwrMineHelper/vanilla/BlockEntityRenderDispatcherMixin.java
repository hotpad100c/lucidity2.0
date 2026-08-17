package ml.mypals.lucidity.mixin.features.worldEatwrMineHelper.vanilla;

import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import ml.mypals.lucidity.features.worldEaterHelper.WorldEaterHelperManager;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderDispatcher;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.client.renderer.state.CameraRenderState;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.joml.Quaternionf;
import org.joml.Vector3f;
import org.spongepowered.asm.mixin.Mixin;

import static ml.mypals.lucidity.config.LucidityConfigs.Generic.WORLD_EATER_MINE_HELPER_HEIGHT;
import static ml.mypals.lucidity.config.FeatureToggle.WORLD_EATER_MINE_HELPER;

@Mixin(BlockEntityRenderDispatcher.class)
public class BlockEntityRenderDispatcherMixin {
    // 1.21.9 起 BlockEntityRenderDispatcher 上已经没有 render(...) 了，
    // 方块实体走 submit(state, poseStack, collector, camera)。原来的 @WrapMethod
    // 目标不存在，注入静默失败 —— 这就是额外的方块实体渲染整个不见的原因。
    @WrapMethod(method = "submit")
    public void tesselate(BlockEntityRenderState blockEntityRenderState, PoseStack poseStack, SubmitNodeCollector submitNodeCollector, CameraRenderState cameraRenderState, Operation<Void> original) {
        original.call(blockEntityRenderState, poseStack, submitNodeCollector, cameraRenderState);

        BlockState blockState = blockEntityRenderState.blockState;
        BlockPos blockPos = blockEntityRenderState.blockPos;
        if (WORLD_EATER_MINE_HELPER.getBooleanValue() && WorldEaterHelperManager.shouldRender(blockState, blockPos)) {
            float height = WORLD_EATER_MINE_HELPER_HEIGHT.getFloatValue();
            poseStack.pushPose();

            poseStack.translate(0.5, height+0.5, 0.5);
            Quaternionf rotation = new Quaternionf().rotationTo(new Vector3f(1, 1, 1).normalize(), new Vector3f(0, 1, 0));
            poseStack.mulPose(rotation);
            poseStack.mulPose(Axis.YP.rotationDegrees(45f));

            poseStack.translate(-0.5, -0.5, -0.5);

            original.call(blockEntityRenderState, poseStack, submitNodeCollector, cameraRenderState);

            poseStack.popPose();
        }

    }
}
