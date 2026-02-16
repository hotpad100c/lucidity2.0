package ml.mypals.lucidity.features.selectiveRendering.accessor;

import net.minecraft.core.particles.ParticleType;

public interface ParticleAccessor {
    void lucidity$setParticleType(ParticleType data);
    ParticleType lucidity$getParticleType();
}
