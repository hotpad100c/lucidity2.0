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
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
//? if >=1.21.9 {
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
//?}
//? if >=1.21.3 {
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.util.ARGB;
import net.minecraft.world.entity.Entity;
import org.jetbrains.annotations.Nullable;
//?}
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(LivingEntityRenderer.class)
public abstract class LivingEntityRendererMixin {

    //? if >= 1.21.9 {
    @Shadow protected abstract boolean isBodyVisible(LivingEntityRenderState livingEntityRenderState);

    @Shadow protected abstract int getModelTint(LivingEntityRenderState livingEntityRenderState);

    @Shadow protected abstract boolean shouldRenderLayers(LivingEntityRenderState livingEntityRenderState);

    @WrapOperation(method = "submit(Lnet/minecraft/client/renderer/entity/state/LivingEntityRenderState;Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/SubmitNodeCollector;Lnet/minecraft/client/renderer/state/CameraRenderState;)V",
            at = @At(target = "Lnet/minecraft/client/renderer/SubmitNodeCollector;submitModel(Lnet/minecraft/client/model/Model;Ljava/lang/Object;Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/RenderType;IIILnet/minecraft/client/renderer/texture/TextureAtlasSprite;ILnet/minecraft/client/renderer/feature/ModelFeatureRenderer$CrumblingOverlay;)V",value = "INVOKE"))
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

    //?} else if >=1.21.3 {
    /*@Shadow @Nullable protected abstract RenderType getRenderType(LivingEntityRenderState livingEntityRenderState, boolean bl, boolean bl2, boolean bl3);

    @Shadow protected abstract boolean isBodyVisible(LivingEntityRenderState livingEntityRenderState);

    @WrapOperation(method = "render(Lnet/minecraft/client/renderer/entity/state/LivingEntityRenderState;Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;I)V",
    at = @At(target = "renderToBuffer(Lcom/mojang/blaze3d/vertex/PoseStack;Lcom/mojang/blaze3d/vertex/VertexConsumer;III)V",value = "INVOKE"))
    private void renderToBuffer(EntityModel<?> instance, PoseStack poseStack, VertexConsumer vertexConsumer, int i, int j, int k, Operation<Void> original, @Local(argsOnly = true) LivingEntityRenderState livingEntityRenderState, @Local(argsOnly = true) MultiBufferSource multiBufferSource){
    *///?} else {

    /*@Shadow protected abstract boolean isBodyVisible(LivingEntity par1);

    @Shadow protected abstract RenderType getRenderType(LivingEntity par1, boolean par2, boolean par3, boolean par4);

    @WrapOperation(method = "render(Lnet/minecraft/world/entity/LivingEntity;FFLcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;I)V",
            at = @At(target = "renderToBuffer(Lcom/mojang/blaze3d/vertex/PoseStack;Lcom/mojang/blaze3d/vertex/VertexConsumer;III)V",value = "INVOKE"))
    private void renderToBuffer(EntityModel instance, PoseStack poseStack, VertexConsumer vertexConsumer, int i, int j, int k, Operation<Void> original,@Local(argsOnly = true) LivingEntity livingEntity, @Local(argsOnly = true) MultiBufferSource multiBufferSource){
    *///?}
        if(FeatureToggle.INVISIBLE_ENTITY_OVERRIDE.getBooleanValue()){
            //? if >=1.21.3 {
            boolean bodyVisible = this.isBodyVisible(livingEntityRenderState);
            boolean visibleToPlayer = bodyVisible && !livingEntityRenderState.isInvisibleToPlayer;
            //?} else {
            /*boolean bodyVisible = this.isBodyVisible(livingEntity);
            boolean visibleToPlayer = bodyVisible && !livingEntity.isInvisibleTo(Minecraft.getInstance().player);
            *///?}
            if(visibleToPlayer) {
                //? if >=1.21.9 {
                original.call(instance, model, o, poseStack, renderType, lightCoords, overlay, color, textureAtlasSprite, outlineColor, crumblingOverlay);
                //?} else {
                /*original.call(instance, poseStack, vertexConsumer, i, j, k);
                *///?}
                return;
            }
            //? if >=1.21.9 {

            //?} else if >=1.21.3 {
            /*RenderType renderType = this.getRenderType(livingEntityRenderState, false, true, livingEntityRenderState.appearsGlowing);
            *///?} else {
            /*RenderType renderType = this.getRenderType(livingEntity, false, true, livingEntity.hasGlowingTag());
            *///?}
            float alphaF = LucidityConfigs.Generic.INVISIBLE_ENTITY_ALPHA.getFloatValue();
            int alpha = (int)(alphaF * 255.0f) & 0xFF;
            //? if >=1.21.9 {
            int k = (color & 0x00FFFFFF) | (alpha << 24);
            original.call(instance, model, o, poseStack, renderType, lightCoords, overlay, k, textureAtlasSprite, outlineColor, crumblingOverlay);
            //?} else {
            /*VertexConsumer vertexConsumerT = multiBufferSource.getBuffer(renderType);

            k = (k & 0x00FFFFFF) | (alpha << 24);
            original.call(instance, poseStack, vertexConsumerT, i, j, k);
            *///?}
        }
        else {
            //? if >=1.21.9 {
            original.call(instance, model, o, poseStack, renderType, lightCoords, overlay, color, textureAtlasSprite, outlineColor, crumblingOverlay);
            //?} else {
            /*original.call(instance, poseStack, vertexConsumer, i, j, k);
            *///?}
        }
    }

}
