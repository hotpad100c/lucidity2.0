package ml.mypals.lucidity.mixin.features.selectiveRendering.vanilla;
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

@Mixin(ParticleEngine.class)
public abstract class ParticleManagerMixin {

    @Shadow @Final private Map<ParticleRenderType, Queue<Particle>> particles;


    @Shadow @Final private static List<ParticleRenderType> RENDER_ORDER;


    @WrapOperation(method = "createParticle",at = @At(target = "Lnet/minecraft/client/particle/ParticleEngine;add(Lnet/minecraft/client/particle/Particle;)V",value = "INVOKE"))
    private void preRenderParticle(ParticleEngine instance, Particle particle, Operation<Void> original, @Local(argsOnly = true) ParticleOptions particleOptions) {

        ParticlePosAccessor particlePosAccessor = (ParticlePosAccessor)particle;
        if(!SelectiveRenderingManager.shouldRenderParticle(particleOptions.getType(),new Vec3(particlePosAccessor.getX(),particlePosAccessor.getY(),particlePosAccessor.getZ())))
        {
            return;
        }

        ((ParticleAccessor)particle).lucidity$setParticleType(particleOptions.getType());
        original.call(instance, particle);
    }

}
