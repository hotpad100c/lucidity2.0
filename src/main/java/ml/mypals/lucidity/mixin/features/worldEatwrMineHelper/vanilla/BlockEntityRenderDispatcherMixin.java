package ml.mypals.lucidity.mixin.features.worldEatwrMineHelper.vanilla;

import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import ml.mypals.lucidity.features.worldEaterHelper.WorldEaterHelperManager;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderDispatcher;
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
    @WrapMethod(method = "render(Lnet/minecraft/world/level/block/entity/BlockEntity;FLcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;)V")
    public void tesselate(BlockEntity blockEntity, float f, PoseStack poseStack, MultiBufferSource multiBufferSource, Operation<Void> original) {
        original.call(blockEntity, f, poseStack, multiBufferSource);

        BlockState blockState = blockEntity.getBlockState();
        BlockPos blockPos = blockEntity.getBlockPos();
        if (WORLD_EATER_MINE_HELPER.getBooleanValue() && WorldEaterHelperManager.shouldRender(blockState, blockPos)) {
            float height = WORLD_EATER_MINE_HELPER_HEIGHT.getFloatValue();
            poseStack.pushPose();

            poseStack.translate(0.5, height+0.5, 0.5);
            Quaternionf rotation = new Quaternionf().rotationTo(new Vector3f(1, 1, 1).normalize(), new Vector3f(0, 1, 0));
            poseStack.mulPose(rotation);
            poseStack.mulPose(Axis.YP.rotationDegrees(45f));

            poseStack.translate(-0.5, -0.5, -0.5);

            original.call(blockEntity, f, poseStack, multiBufferSource);

            poseStack.popPose();
        }

    }
}
