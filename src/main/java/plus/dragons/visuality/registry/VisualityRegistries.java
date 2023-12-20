package plus.dragons.visuality.registry;

import net.minecraft.core.Registry;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.resources.ResourceKey;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NewRegistryEvent;
import plus.dragons.visuality.Visuality;

public class VisualityRegistries {
    public static final ResourceKey<Registry<ParticleType<?>>> PARTICLE_TYPES_KEY = ResourceKey.createRegistryKey(Visuality.location("particle_type"));
    public static final DeferredRegister<ParticleType<?>> PARTICLE_TYPES = DeferredRegister.create(PARTICLE_TYPES_KEY, Visuality.ID);
    public static Registry<ParticleType<?>> PARTICLE_TYPES_REGISTRY;
    
    public static class Registers {
        public static void register(IEventBus modBus) {
            PARTICLE_TYPES_REGISTRY = PARTICLE_TYPES.makeRegistry(builder -> builder.sync(false));
            VisualityParticles.register();
            PARTICLE_TYPES.register(modBus);
        }
    }
    
}
