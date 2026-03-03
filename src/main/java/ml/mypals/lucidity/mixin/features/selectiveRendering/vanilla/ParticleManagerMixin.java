package ml.mypals.lucidity.mixin.features.selectiveRendering.vanilla;
//? if <1.21.9 {
/*import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import ml.mypals.lucidity.config.SelectiveRenderingConfigs;
import ml.mypals.lucidity.features.selectiveRendering.ControllableTransparentBuffersWrapper;
import ml.mypals.lucidity.features.selectiveRendering.ControllableTransparentVertexConsumer;
import net.minecraft.client.Camera;
import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.core.particles.ParticleType;
*///?}
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;

import ml.mypals.lucidity.features.selectiveRendering.SelectiveRenderingManager;
import ml.mypals.lucidity.features.selectiveRendering.accessor.ParticleAccessor;
import ml.mypals.lucidity.mixin.features.selectiveRendering.accessor.ParticlePosAccessor;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleEngine;
import net.minecraft.client.particle.ParticleRenderType;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;

import java.util.List;
import java.util.Map;
import java.util.Queue;

import static ml.mypals.lucidity.features.selectiveRendering.SelectiveRenderingManager.SelectiveRenderingMode.OFF;
//? if <=1.21.6 {

//?}
@Mixin(ParticleEngine.class)
public abstract class ParticleManagerMixin {

    @Shadow @Final private Map<ParticleRenderType, Queue<Particle>> particles;

    //? if >=1.21.4 && <=1.21.6 {
    /*@Shadow
    protected static void renderCustomParticles(Camera camera, float f, MultiBufferSource.BufferSource bufferSource, Queue<Particle> queue) {
    }

    @Shadow
    protected static void renderParticleType(Camera camera, float f, MultiBufferSource.BufferSource bufferSource, ParticleRenderType particleRenderType, Queue<Particle> queue) {
    }
    *///?}

    @Shadow @Final private static List<ParticleRenderType> RENDER_ORDER;


    @WrapOperation(method = "createParticle",at = @At(target = "Lnet/minecraft/client/particle/ParticleEngine;add(Lnet/minecraft/client/particle/Particle;)V",value = "INVOKE"))
    private void preRenderParticle(ParticleEngine instance, Particle particle, Operation<Void> original, @Local(argsOnly = true) ParticleOptions particleOptions) {

        //? if >=1.21.9 {
        ParticlePosAccessor particlePosAccessor = (ParticlePosAccessor)particle;
        if(!SelectiveRenderingManager.shouldRenderParticle(particleOptions.getType(),new Vec3(particlePosAccessor.getX(),particlePosAccessor.getY(),particlePosAccessor.getZ())))
        {
            return;
        }
        //?}

        ((ParticleAccessor)particle).lucidity$setParticleType(particleOptions.getType());
        original.call(instance, particle);
    }

    //? if <=1.21.3 {
    /*@WrapOperation(method = "render", at = @At(target = "Lnet/minecraft/client/particle/Particle;render(Lcom/mojang/blaze3d/vertex/VertexConsumer;Lnet/minecraft/client/Camera;F)V",value = "INVOKE"))
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

    *///?} else if <=1.21.6 {
    /*@WrapMethod(method = "render")
    public void render(Camera camera, float f, MultiBufferSource.BufferSource bufferSource, Operation<Void> original) {

        if(SelectiveRenderingConfigs.PARTICLE_RENDERING_MODE.getOptionListValue() == OFF){
            original.call(camera, f, bufferSource);
            return;
        }

        for (ParticleRenderType particleRenderType : RENDER_ORDER) {
            ParticleRenderType actualType = particleRenderType;
            if (particleRenderType == ParticleRenderType.PARTICLE_SHEET_OPAQUE) {
                actualType = ParticleRenderType.PARTICLE_SHEET_TRANSLUCENT;
            }
            Queue<Particle> queue = this.particles.get(particleRenderType);
            if (queue != null && !queue.isEmpty()) {
                renderParticleType(camera, f, bufferSource, actualType, queue);
            }
        }
        Queue<Particle> queue2 = (Queue)this.particles.get(ParticleRenderType.CUSTOM);
        if (queue2 != null && !queue2.isEmpty()) {
            renderCustomParticles(camera, f, bufferSource, queue2);
        }
        bufferSource.endBatch();
    }
    @WrapOperation(method = "renderParticleType", at = @At(target = "Lnet/minecraft/client/particle/Particle;render(Lcom/mojang/blaze3d/vertex/VertexConsumer;Lnet/minecraft/client/Camera;F)V",value = "INVOKE"))
    private static void renderParticle(Particle instance, VertexConsumer vertexConsumer, Camera camera, float v, Operation<Void> original, @Local(argsOnly = true) MultiBufferSource.BufferSource bufferSource, @Local(argsOnly = true) ParticleRenderType particleRenderType) {
        ParticleType currentParticleType = ((ParticleAccessor)instance).lucidity$getParticleType();
        ParticlePosAccessor particlePosAccessor = (ParticlePosAccessor)instance;
        if(currentParticleType != null && !SelectiveRenderingManager.shouldRenderParticle(currentParticleType,new Vec3(particlePosAccessor.getX(),particlePosAccessor.getY(),particlePosAccessor.getZ()))){
            if(!SelectiveRenderingConfigs.isBlockFullyHidden()){
                original.call(instance, new ControllableTransparentVertexConsumer(vertexConsumer),camera,v);
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
