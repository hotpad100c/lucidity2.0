package ml.mypals.lucidity.mixin.features.selectiveRendering.vanilla;

import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.mojang.blaze3d.vertex.VertexConsumer;
import ml.mypals.lucidity.config.SelectiveRenderingConfigs;
import ml.mypals.lucidity.features.selectiveRendering.ControllableTransparentVertexConsumer;
import ml.mypals.lucidity.features.selectiveRendering.SelectiveRenderingManager;
import ml.mypals.lucidity.features.selectiveRendering.accessor.ParticleAccessor;
import net.minecraft.client.Camera;
import net.minecraft.client.particle.Particle;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;

@Mixin(Particle.class)
public abstract class ParticleMixin implements ParticleAccessor {
    @Shadow protected double x;
    @Shadow protected double y;
    @Shadow protected double z;
    @Unique
    private ParticleType currentRenderingType = null;
    @Unique
    public void lucidity$setParticleType(ParticleType data) {
        this.currentRenderingType = data;
    }
    @Unique
    public ParticleType lucidity$getParticleType() {
        return this.currentRenderingType;
    }
}
