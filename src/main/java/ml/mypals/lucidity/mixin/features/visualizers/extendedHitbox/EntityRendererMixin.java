package ml.mypals.lucidity.mixin.features.visualizers.extendedHitbox;

import com.llamalad7.mixinextras.sugar.Share;
import com.llamalad7.mixinextras.sugar.ref.LocalRef;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import ml.mypals.lucidity.config.FeatureToggle;
import net.minecraft.client.Minecraft;
//? if >=1.21.3 {
import net.minecraft.client.renderer.ShapeRenderer;
//?}
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
//? if >=1.21.5 {

import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.client.renderer.entity.state.HitboxesRenderState;
//?}
import net.minecraft.client.server.IntegratedServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.projectile.*;
import net.minecraft.world.phys.AABB;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(EntityRenderDispatcher.class)
public abstract class EntityRendererMixin {

    //? if >=1.21.5 {
    @Unique
    public ThreadLocal<Entity> targetEntity = ThreadLocal.withInitial(() -> null);

    @Inject(at = @At("HEAD"), method = "render(Lnet/minecraft/world/entity/Entity;DDDFLcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;ILnet/minecraft/client/renderer/entity/EntityRenderer;)V")
    private void render(Entity entity, double d, double e, double f, float g, PoseStack poseStack, MultiBufferSource multiBufferSource, int i, EntityRenderer<? super Entity, EntityRenderState> entityRenderer, CallbackInfo ci) {
        targetEntity.set(entity);
    }
    @Inject(at = @At("HEAD"), method = "renderHitboxes(Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/entity/state/EntityRenderState;Lnet/minecraft/client/renderer/entity/state/HitboxesRenderState;Lnet/minecraft/client/renderer/MultiBufferSource;)V")
    private void renderHitbox(PoseStack poseStack, EntityRenderState entityRenderState, HitboxesRenderState hitboxesRenderState, MultiBufferSource multiBufferSource, CallbackInfo ci) {
        VertexConsumer vertexConsumer = multiBufferSource.getBuffer(RenderType.lines());
    //?} else {
    /*@Inject(at = @At("HEAD"), method = "renderHitbox(Lcom/mojang/blaze3d/vertex/PoseStack;Lcom/mojang/blaze3d/vertex/VertexConsumer;Lnet/minecraft/world/entity/Entity;FFFF)V", cancellable = true)
    private static void renderHitbox(PoseStack poseStack, VertexConsumer vertexConsumer, Entity target, float f, float g, float h, float i, CallbackInfo ci) {
    *///?}
        //? if >=1.21.5 {
        Entity target = targetEntity.get();
        //?}
        if (target != null && FeatureToggle.EXTENDED_HITBOX.getBooleanValue()) {
            Entity serverEntity = getServerSideEntity(target);
            Entity entity = serverEntity != null ? serverEntity : target;
            if(entity instanceof Projectile projectile && projectile.getPickRadius()>0){
                float targetingMargin = projectile.getPickRadius();
                AABB aABB = entity.getBoundingBox().inflate(targetingMargin).move(-entity.getX(), -entity.getY(), -entity.getZ());
                //? if >=1.21.3 {
                ShapeRenderer.renderLineBox(poseStack, vertexConsumer, aABB,1.0F, 1.0F, 0.0F, 0.6F);
                //?} else {
                /*LevelRenderer.renderLineBox(poseStack, vertexConsumer, aABB,1.0F, 1.0F, 0.0F, 0.6F);
                *///?}
            }
        }
    }
    @Unique
    private static Entity getServerSideEntity(Entity entity) {
        IntegratedServer integratedServer = Minecraft.getInstance().getSingleplayerServer();
        if (integratedServer != null) {
            ServerLevel serverLevel = integratedServer.getLevel(entity.level().dimension());
            if (serverLevel != null) {
                return serverLevel.getEntity(entity.getId());
            }
        }

        return null;
    }
}
