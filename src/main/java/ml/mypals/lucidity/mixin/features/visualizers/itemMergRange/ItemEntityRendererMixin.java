package ml.mypals.lucidity.mixin.features.visualizers.itemMergRange;

import com.mojang.blaze3d.vertex.*;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.state.CameraRenderState;

import ml.mypals.lucidity.utils.DeferredGeometry;
import fi.dy.masa.malilib.util.data.Color4f;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.rendertype.*;
import net.minecraft.client.renderer.entity.ItemEntityRenderer;
import net.minecraft.client.renderer.entity.state.ItemEntityRenderState;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.phys.AABB;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import static ml.mypals.lucidity.config.VisualizerColors.ITEM_MERG_RANGE_COLOR;
import static ml.mypals.lucidity.config.FeatureToggle.ITEM_MERG_RANGE_VISUALIZE;
import static ml.mypals.lucidity.utils.LucidityRenderUtils.renderBox;

@Mixin(ItemEntityRenderer.class)
public class ItemEntityRendererMixin {
    // 1.21.9 起实体渲染器不再有 render(...)，改成 submit(state, poseStack, collector, camera)
    @Inject(method = "submit(Lnet/minecraft/client/renderer/entity/state/ItemEntityRenderState;Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/SubmitNodeCollector;Lnet/minecraft/client/renderer/state/CameraRenderState;)V",
    at = @At("TAIL"))
    public void render(ItemEntityRenderState itemEntityRenderState, PoseStack poseStack, SubmitNodeCollector submitNodeCollector, CameraRenderState cameraRenderState, CallbackInfo ci) {
        if (ITEM_MERG_RANGE_VISUALIZE.getBooleanValue()) {
            poseStack.pushPose();
            float bbw = itemEntityRenderState.boundingBoxWidth;
            float bbh = itemEntityRenderState.boundingBoxHeight;
            Color4f color = ITEM_MERG_RANGE_COLOR.getColor();
            AABB aabb = new AABB(
                    -bbw / 2, 0, -bbw / 2,
                    bbw / 2, bbh, bbw / 2
            ).inflate(0.5F, 0.0F, 0.5F);

            // submit 阶段不能直接画，得交给提交节点在 draw 阶段执行
            DeferredGeometry.submit(submitNodeCollector, poseStack, RenderTypes.debugQuads(),
                    (ps, consumer) -> renderBox(ps, consumer, aabb, color.r, color.g, color.b, color.a));
            poseStack.popPose();
        }
    }
}
