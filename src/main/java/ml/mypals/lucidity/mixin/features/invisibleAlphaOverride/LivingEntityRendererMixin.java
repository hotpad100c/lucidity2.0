package ml.mypals.lucidity.mixin.features.invisibleAlphaOverride;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import ml.mypals.lucidity.config.FeatureToggle;
import ml.mypals.lucidity.config.LucidityConfigs;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.Model;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.rendertype.*;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.util.ARGB;
import net.minecraft.world.entity.Entity;
import org.jetbrains.annotations.Nullable;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(LivingEntityRenderer.class)
public abstract class LivingEntityRendererMixin {

    @Shadow protected abstract boolean isBodyVisible(LivingEntityRenderState livingEntityRenderState);

    @Shadow protected abstract int getModelTint(LivingEntityRenderState livingEntityRenderState);

    @Shadow protected abstract boolean shouldRenderLayers(LivingEntityRenderState livingEntityRenderState);

    @WrapOperation(method = "submit(Lnet/minecraft/client/renderer/entity/state/LivingEntityRenderState;Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/SubmitNodeCollector;Lnet/minecraft/client/renderer/state/CameraRenderState;)V",
            // 1.21.11 把 RenderType 搬进了 rendertype 子包。注入点描述符是字符串字面量、
            // 用的是斜杠形式，替换规则只改点号形式的 FQN，所以这里改不到，注入会静默失效。
            at = @At(target = "Lnet/minecraft/client/renderer/SubmitNodeCollector;submitModel(Lnet/minecraft/client/model/Model;Ljava/lang/Object;Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/rendertype/RenderType;IIILnet/minecraft/client/renderer/texture/TextureAtlasSprite;ILnet/minecraft/client/renderer/feature/ModelFeatureRenderer$CrumblingOverlay;)V",value = "INVOKE"))
    private void renderToBuffer(
            SubmitNodeCollector instance,
            Model<LivingEntity> model,
            Object o, PoseStack poseStack,
            RenderType renderType,
            int lightCoords, int overlay, int color,
            TextureAtlasSprite textureAtlasSprite,
            int outlineColor,
            ModelFeatureRenderer.CrumblingOverlay crumblingOverlay,
            Operation<Void> original,
            @Local(argsOnly = true) LivingEntityRenderState livingEntityRenderState){

        if(FeatureToggle.INVISIBLE_ENTITY_OVERRIDE.getBooleanValue()){
            boolean bodyVisible = this.isBodyVisible(livingEntityRenderState);
            boolean visibleToPlayer = bodyVisible && !livingEntityRenderState.isInvisibleToPlayer;
            if(visibleToPlayer) {
                original.call(instance, model, o, poseStack, renderType, lightCoords, overlay, color, textureAtlasSprite, outlineColor, crumblingOverlay);
                return;
            }

            float alphaF = LucidityConfigs.Generic.INVISIBLE_ENTITY_ALPHA.getFloatValue();
            int alpha = (int)(alphaF * 255.0f) & 0xFF;
            int k = (color & 0x00FFFFFF) | (alpha << 24);
            original.call(instance, model, o, poseStack, renderType, lightCoords, overlay, k, textureAtlasSprite, outlineColor, crumblingOverlay);
        }
        else {
            original.call(instance, model, o, poseStack, renderType, lightCoords, overlay, color, textureAtlasSprite, outlineColor, crumblingOverlay);
        }
    }

}
