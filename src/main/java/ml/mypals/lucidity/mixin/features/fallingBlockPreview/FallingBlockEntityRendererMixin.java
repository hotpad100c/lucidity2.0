package ml.mypals.lucidity.mixin.features.fallingBlockPreview;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import ml.mypals.lucidity.features.visualizers.b36Target.TransparentVertexConsumer;
import ml.mypals.lucidity.utils.DeferredGeometry;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.rendertype.*;
import net.minecraft.client.renderer.block.BlockRenderDispatcher;
import net.minecraft.client.renderer.entity.FallingBlockRenderer;
import net.minecraft.client.renderer.entity.state.FallingBlockRenderState;
import net.minecraft.world.level.block.FallingBlock;
import net.minecraft.client.renderer.state.CameraRenderState;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.item.FallingBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import static ml.mypals.lucidity.config.FeatureToggle.FALLING_BLOCK_PREVIEW;
import static ml.mypals.lucidity.features.fallingBlockPreview.FallingBlockPredictor.predictLandingPos;

@Mixin(FallingBlockRenderer.class)
public class FallingBlockEntityRendererMixin {

    @Inject(method = "submit(Lnet/minecraft/client/renderer/entity/state/FallingBlockRenderState;Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/SubmitNodeCollector;Lnet/minecraft/client/renderer/state/CameraRenderState;)V",
            at = @At(target = "Lnet/minecraft/client/renderer/SubmitNodeCollector;submitMovingBlock(Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/block/MovingBlockRenderState;)V",
                    value = "TAIL"))
    public void render(FallingBlockRenderState fallingBlockRenderState, PoseStack poseStack, SubmitNodeCollector submitNodeCollector, CameraRenderState cameraRenderState, CallbackInfo ci) {


        if(Minecraft.getInstance().level == null || !FALLING_BLOCK_PREVIEW.getBooleanValue()) return;
        BlockState blockState = fallingBlockRenderState.movingBlockRenderState.blockState;
        BlockPos predictLandingPos = predictLandingPos(
                Minecraft.getInstance().level,
                fallingBlockRenderState.x,
                fallingBlockRenderState.y,
                fallingBlockRenderState.z,
                blockState);
        if (predictLandingPos != null) {
            poseStack.pushPose();

            double offsetX = predictLandingPos.getX() - fallingBlockRenderState.x;
            double offsetY = predictLandingPos.getY() - fallingBlockRenderState.y;
            double offsetZ = predictLandingPos.getZ() - fallingBlockRenderState.z;
            poseStack.translate(offsetX, offsetY, offsetZ);
            poseStack.scale(1.001f, 1.001f, 1.001f);

            BlockRenderDispatcher blockRenderDispatcher = Minecraft.getInstance().getBlockRenderer();

            // 绘制必须走提交节点：submit 阶段直接写 bufferSource 的话这一帧不会画出来
            DeferredGeometry.submit(submitNodeCollector, poseStack, RenderTypes.translucentMovingBlock(),
                    (ps, consumer) -> blockRenderDispatcher.getModelRenderer().tesselateBlock(
                            Minecraft.getInstance().level,
                            blockRenderDispatcher.getBlockModel(blockState)
                                    .collectParts(Minecraft.getInstance().level.getRandom()),
                            blockState,
                            predictLandingPos,
                            ps,
                            new TransparentVertexConsumer(consumer),
                            true,
                            LightTexture.FULL_BLOCK));
            poseStack.popPose();
        }
    }
}