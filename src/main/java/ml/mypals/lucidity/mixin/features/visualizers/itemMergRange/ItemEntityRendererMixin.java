package ml.mypals.lucidity.mixin.features.visualizers.itemMergRange;

import com.mojang.blaze3d.vertex.*;
import fi.dy.masa.malilib.util.Color4f;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.ItemEntityRenderer;
//? if >=1.21.3 {
import net.minecraft.client.renderer.entity.state.ItemEntityRenderState;
//?}
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
    //? if >=1.21.3 {
    @Inject(method = "render(Lnet/minecraft/client/renderer/entity/state/ItemEntityRenderState;Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;I)V",
    at = @At("TAIL"))

    public void render(ItemEntityRenderState itemEntityRenderState, PoseStack poseStack, MultiBufferSource multiBufferSource, int i, CallbackInfo ci) {
    //?} else {
    /*@Inject(method = "Lnet/minecraft/client/renderer/entity/ItemEntityRenderer;render(Lnet/minecraft/world/entity/item/ItemEntity;FFLcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;I)V",
            at = @At("TAIL"))

    public void render(ItemEntity itemEntity, float f, float g, PoseStack poseStack, MultiBufferSource multiBufferSource, int i, CallbackInfo ci) {
    *///?}
        if (ITEM_MERG_RANGE_VISUALIZE.getBooleanValue()) {
            poseStack.pushPose();
            //? if >=1.21.3 {
            float bbw = itemEntityRenderState.boundingBoxWidth;
            float bbh = itemEntityRenderState.boundingBoxHeight;
            //?} else {
            /*float bbw = itemEntity.getBbWidth();
            float bbh = itemEntity.getBbHeight();
            *///?}
            Color4f color = ITEM_MERG_RANGE_COLOR.getColor();
            AABB aabb = new AABB(
                    -bbw / 2, 0, -bbw / 2,
                    bbw / 2, bbh, bbw / 2
            ).inflate(0.5F, 0.0F, 0.5F);

            renderBox(poseStack,multiBufferSource.getBuffer(RenderType.debugQuads()), aabb,color.r,color.g,color.b,color.a);
            poseStack.popPose();
        }
    }
}
