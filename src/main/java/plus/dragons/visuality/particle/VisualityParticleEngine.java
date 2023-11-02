package plus.dragons.visuality.particle;

import net.minecraft.client.particle.ParticleEngine;
import net.minecraft.client.particle.ParticleProvider;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleType;
import net.neoforged.neoforge.registries.RegistryObject;

public interface VisualityParticleEngine {
    
    <O extends ParticleOptions, T extends ParticleType<O>> void registerVisuality(RegistryObject<T> type, ParticleProvider<O> provider);
    
    <O extends ParticleOptions, T extends ParticleType<O>> void registerVisuality(RegistryObject<T> type, ParticleEngine.SpriteParticleRegistration<O> registration);
    
}
