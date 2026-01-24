package ml.mypals.lucidity.mixin.features.selectiveRendering.vanilla;

import ml.mypals.lucidity.features.selectiveRendering.SelectiveRenderingManager;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleEngine;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ParticleEngine.class)
public class ParticleManagerMixin {
    @Inject(method = "createParticle", at = @At("HEAD"), cancellable = true)
    private void disableAllParticles(ParticleOptions particleOptions, double x, double y, double z, double g, double h, double i, CallbackInfoReturnable<Particle> cir) {
        if(!SelectiveRenderingManager.shouldRenderParticle(particleOptions.getType(),new Vec3(x,y,z))){
            cir.setReturnValue(null);
            cir.cancel();
        }
    }
}
