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
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
//? if >=1.21.3 {

import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.minecraft.world.entity.Entity;
import org.jetbrains.annotations.Nullable;
//?}
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(LivingEntityRenderer.class)
public abstract class LivingEntityRendererMixin {

    //? if >=1.21.3 {
    @Shadow @Nullable protected abstract RenderType getRenderType(LivingEntityRenderState livingEntityRenderState, boolean bl, boolean bl2, boolean bl3);

    @Shadow protected abstract boolean isBodyVisible(LivingEntityRenderState livingEntityRenderState);

    @WrapOperation(method = "render(Lnet/minecraft/client/renderer/entity/state/LivingEntityRenderState;Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;I)V",
    at = @At(target = "renderToBuffer(Lcom/mojang/blaze3d/vertex/PoseStack;Lcom/mojang/blaze3d/vertex/VertexConsumer;III)V",value = "INVOKE"))
    private void renderToBuffer(EntityModel<?> instance, PoseStack poseStack, VertexConsumer vertexConsumer, int i, int j, int k, Operation<Void> original, @Local(argsOnly = true) LivingEntityRenderState livingEntityRenderState, @Local(argsOnly = true) MultiBufferSource multiBufferSource){
    //?} else {

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
                original.call(instance, poseStack, vertexConsumer, i, j, k);
                return;
            }
            //? if >=1.21.3 {
            RenderType renderType = this.getRenderType(livingEntityRenderState, false, true, livingEntityRenderState.appearsGlowing);
            //?} else {
            /*RenderType renderType = this.getRenderType(livingEntity, false, true, livingEntity.hasGlowingTag());
            *///?}
            VertexConsumer vertexConsumerT = multiBufferSource.getBuffer(renderType);
            float alphaF = LucidityConfigs.Generic.INVISIBLE_ENTITY_ALPHA.getFloatValue();
            int alpha = (int)(alphaF * 255.0f) & 0xFF;
            k = (k & 0x00FFFFFF) | (alpha << 24);
            original.call(instance, poseStack, vertexConsumerT, i, j, k);
        }
        else {
            original.call(instance, poseStack, vertexConsumer, i, j, k);
        }
    }
}
