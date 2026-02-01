package ml.mypals.lucidity.mixin.features.visualizers.entityYaw;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import ml.mypals.lucidity.config.FeatureToggle;
import net.minecraft.client.Minecraft;
//? if >=1.21.5 {
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.client.renderer.entity.state.HitboxesRenderState;
//?}
//? if >=1.21.3 {
import net.minecraft.client.renderer.ShapeRenderer;
//?}
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.server.IntegratedServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;
import org.joml.Vector3f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Map;

import static ml.mypals.lucidity.config.VisualizerColors.BODY_ROT_COLOR;
import static ml.mypals.lucidity.config.VisualizerColors.YROT_COLOR;

@Mixin(EntityRenderDispatcher.class)
public abstract class EntityRendererMixin {



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
        float f = Minecraft.getInstance().getDeltaTracker().getGameTimeDeltaTicks();
    //?} else {
    /*@Inject(at = @At("HEAD"), method = "renderHitbox(Lcom/mojang/blaze3d/vertex/PoseStack;Lcom/mojang/blaze3d/vertex/VertexConsumer;Lnet/minecraft/world/entity/Entity;FFFF)V", cancellable = true)
    private static void renderHitbox(PoseStack poseStack, VertexConsumer vertexConsumer, Entity target, float f, float g, float h, float i, CallbackInfo ci) {
    *///?}
        //? if >=1.21.5 {
        Entity target = targetEntity.get();
        //?}
        if (target != null && FeatureToggle.BODY_YAW.getBooleanValue() && Minecraft.getInstance().getCameraEntity() != null && !target.is(Minecraft.getInstance().getCameraEntity())) {

            Entity serverEntity = getServerSideEntity(target);

            Entity entity = serverEntity != null ? serverEntity : target;

            /*? if >=1.21.3 {*/ShapeRenderer./*?} else {*//*EntityRenderDispatcher.*//*?}*/renderVector(poseStack, vertexConsumer,
                    new Vector3f(0.0F, entity.getBbHeight()/2, 0.0F),
                    Vec3.directionFromRotation(0.0F, getPreciseEntityRotation(entity,f)).scale(2.5F), YROT_COLOR.getIntegerValue());
            if(entity instanceof LivingEntity livingEntity){
                /*? if >=1.21.3 {*/ShapeRenderer./*?} else {*//*EntityRenderDispatcher.*//*?}*/renderVector(poseStack, vertexConsumer,
                        new Vector3f(0.0F, entity.getBbHeight()/2, 0.0F),
                        Vec3.directionFromRotation(0.0F, getPreciseEntityBodyRotation(livingEntity,f)).scale(3.0F), BODY_ROT_COLOR.getIntegerValue());
            }
        }
    }
    @Unique
    private static float getPreciseEntityBodyRotation(LivingEntity entity, float f) {
        return entity.getPreciseBodyRotation(f);
    }
    @Unique
    private static float getPreciseEntityRotation(Entity entity,float f) {
        return entity.getYRot(/*? if >=1.21.3 {*/f/*?}*/);
    }
}
