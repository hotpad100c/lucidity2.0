package ml.mypals.lucidity.mixin.features.selectiveRendering.vanilla;

import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexConsumer;
import ml.mypals.lucidity.config.SelectiveRenderingConfigs;
import ml.mypals.lucidity.features.selectiveRendering.ControllableTransparentBuffersWrapper;
import ml.mypals.lucidity.features.selectiveRendering.ControllableTransparentVertexConsumer;
import ml.mypals.lucidity.features.selectiveRendering.SelectiveRenderingManager;
import ml.mypals.lucidity.features.selectiveRendering.accessor.ParticleAccessor;
import ml.mypals.lucidity.mixin.features.selectiveRendering.ParticlePosAccessor;
import net.minecraft.client.Camera;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleEngine;
import net.minecraft.client.particle.ParticleRenderType;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;

import java.util.Map;
import java.util.Queue;

@Mixin(ParticleEngine.class)
public abstract class ParticleManagerMixin {

    @Shadow @Final private Map<ParticleRenderType, Queue<Particle>> particles;


    @WrapOperation(method = "createParticle",at = @At(target = "Lnet/minecraft/client/particle/ParticleEngine;add(Lnet/minecraft/client/particle/Particle;)V",value = "INVOKE"))
    private void preRenderParticle(ParticleEngine instance, Particle particle, Operation<Void> original, @Local(argsOnly = true) ParticleOptions particleOptions) {
        ((ParticleAccessor)particle).lucidity$setParticleType(particleOptions.getType());
        original.call(instance, particle);
    }

    //? if <=1.21.3 {
    @WrapOperation(method = "render", at = @At(target = "Lnet/minecraft/client/particle/Particle;render(Lcom/mojang/blaze3d/vertex/VertexConsumer;Lnet/minecraft/client/Camera;F)V",value = "INVOKE"))
    private void renderParticle(Particle instance, VertexConsumer vertexConsumer, Camera camera, float v, Operation<Void> original) {
        ParticleType currentParticleType = ((ParticleAccessor)instance).lucidity$getParticleType();
        ParticlePosAccessor particlePosAccessor = (ParticlePosAccessor)instance;
        if(currentParticleType != null && !SelectiveRenderingManager.shouldRenderParticle(currentParticleType,new Vec3(particlePosAccessor.getX(),particlePosAccessor.getY(),particlePosAccessor.getZ()))){
            if(!SelectiveRenderingConfigs.isBlockFullyHidden()){
                RenderSystem.enableBlend();
                RenderSystem.defaultBlendFunc();
                original.call(instance, new ControllableTransparentVertexConsumer(vertexConsumer),camera,v);
            }
        }else {
            original.call(instance, vertexConsumer, camera, v);
        }
    }

    //?} else if <=1.21.6 {
    
    /*@WrapOperation(method = "renderParticleType", at = @At(target = "Lnet/minecraft/client/particle/Particle;render(Lcom/mojang/blaze3d/vertex/VertexConsumer;Lnet/minecraft/client/Camera;F)V",value = "INVOKE"))
    private static void renderParticle(Particle instance, VertexConsumer vertexConsumer, Camera camera, float v, Operation<Void> original, @Local(argsOnly = true) MultiBufferSource.BufferSource bufferSource) {
        ParticleType currentParticleType = ((ParticleAccessor)instance).lucidity$getParticleType();
        ParticlePosAccessor particlePosAccessor = (ParticlePosAccessor)instance;
        if(currentParticleType != null && !SelectiveRenderingManager.shouldRenderParticle(currentParticleType,new Vec3(particlePosAccessor.getX(),particlePosAccessor.getY(),particlePosAccessor.getZ()))){
            if(!SelectiveRenderingConfigs.isBlockFullyHidden()){
                assert ParticleRenderType.PARTICLE_SHEET_TRANSLUCENT.renderType() != null;
                VertexConsumer vertexConsumer1 = bufferSource.getBuffer(ParticleRenderType.PARTICLE_SHEET_TRANSLUCENT.renderType());
                original.call(instance, new ControllableTransparentVertexConsumer(vertexConsumer1),camera,v);
            }
        }else {
            original.call(instance, vertexConsumer, camera, v);
        }
    }
    @WrapOperation(method = "renderCustomParticles", at = @At(target = "Lnet/minecraft/client/particle/Particle;renderCustom(Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;Lnet/minecraft/client/Camera;F)V",value = "INVOKE"))
    private static void renderCustomParticle(Particle instance, PoseStack poseStack, MultiBufferSource multiBufferSource, Camera camera, float v, Operation<Void> original) {
        ParticleType currentRenderingType = ((ParticleAccessor)instance).lucidity$getParticleType();
        ParticlePosAccessor particlePosAccessor = (ParticlePosAccessor)instance;
        if(currentRenderingType != null && !SelectiveRenderingManager.shouldRenderParticle(currentRenderingType,new Vec3(particlePosAccessor.getX(),particlePosAccessor.getY(),particlePosAccessor.getZ()))){
            if(!SelectiveRenderingConfigs.isBlockFullyHidden()){
                original.call(instance, poseStack, new ControllableTransparentBuffersWrapper((MultiBufferSource.BufferSource )multiBufferSource),camera,v);
            }
        }else {
            original.call(instance, poseStack, multiBufferSource, camera, v);
        }
    }
    *///?}
}
