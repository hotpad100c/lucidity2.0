package ml.mypals.lucidity.mixin.features.visualizers.b36Target;

import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import ml.mypals.lucidity.features.visualizers.b36Target.TransparentVertexConsumer;
import ml.mypals.lucidity.utils.DeferredGeometry;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.*;
import net.minecraft.client.renderer.rendertype.*;
import net.minecraft.client.renderer.block.BlockRenderDispatcher;
import net.minecraft.client.renderer.blockentity.PistonHeadRenderer;
import net.minecraft.client.renderer.block.model.BlockStateModel;
import net.minecraft.client.renderer.block.MovingBlockRenderState;
import net.minecraft.client.renderer.blockentity.state.PistonHeadRenderState;
import net.minecraft.client.renderer.state.CameraRenderState;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.piston.PistonMovingBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import static ml.mypals.lucidity.config.FeatureToggle.B36_TARGET_PREVIEW;

@Mixin(PistonHeadRenderer.class)
public abstract class MovingBlockRenderMixin {
    @Unique
    private boolean renderAdditional = false;
    @Inject(
            method = "submit(Lnet/minecraft/client/renderer/blockentity/state/PistonHeadRenderState;Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/SubmitNodeCollector;Lnet/minecraft/client/renderer/state/CameraRenderState;)V",
            at = @At("TAIL")
    )private void renderPreviewBlock(
            PistonHeadRenderState piston, PoseStack poseStack, SubmitNodeCollector submitNodeCollector, CameraRenderState cameraRenderState, CallbackInfo ci
    )
     {

        if(!B36_TARGET_PREVIEW.getBooleanValue()) return;

         // piston.blockState 继承自 BlockEntityRenderState，指的是 moving_piston 这个方块本身，
         // 它的渲染形状是 INVISIBLE —— 拿它去 tesselate 什么都画不出来。
         // 真正被推动的方块在 piston.block（MovingBlockRenderState）里。
         MovingBlockRenderState moved = piston.block;
         if (moved == null) return;

         // PistonMovingBlockEntity 本身就位于"目的地"，动画期间的偏移由 submit 内部
         // pushPose/popPose 处理、在 TAIL 处已经还原，所以这里不需要任何平移：
         // 直接画在原点就是目的地预览。
         //
         // 绘制必须走提交节点：submit 阶段直接写 bufferSource 的话这一帧不会画出来。
         DeferredGeometry.submit(submitNodeCollector, poseStack, RenderTypes.translucentMovingBlock(),
                 (ps, consumer) -> Minecraft.getInstance().getBlockRenderer().getModelRenderer().tesselateBlock(
                         moved,
                         Minecraft.getInstance().getBlockRenderer().getBlockModel(moved.blockState)
                                 .collectParts(Minecraft.getInstance().level.getRandom()),
                         moved.blockState,
                         moved.blockPos,
                         ps,
                         new TransparentVertexConsumer(consumer),
                         true,
                         LightTexture.FULL_BLOCK
                 ));
    }

    @Unique
    private static Direction getPistonDirection(PistonHeadRenderState s) {
        float ax = Math.abs(s.xOffset);
        float ay = Math.abs(s.yOffset);
        float az = Math.abs(s.zOffset);

        if (ax > ay && ax > az) {
            return s.xOffset > 0 ? Direction.EAST : Direction.WEST;
        }
        if (ay > ax && ay > az) {
            return s.yOffset > 0 ? Direction.UP : Direction.DOWN;
        }
        if (az > ax && az > ay) {
            return s.zOffset > 0 ? Direction.SOUTH : Direction.NORTH;
        }

        return Direction.NORTH;
    }

}
