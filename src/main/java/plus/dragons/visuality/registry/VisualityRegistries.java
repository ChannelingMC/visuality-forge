package plus.dragons.visuality.registry;

import net.minecraft.core.Registry;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.resources.ResourceKey;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.IForgeRegistry;
import net.minecraftforge.registries.RegistryBuilder;
import plus.dragons.visuality.Visuality;

import java.util.function.Supplier;

public class VisualityRegistries {
    public static Supplier<IForgeRegistry<ParticleType<?>>> PARTICLE_TYPES;
    
    public static class Keys {
        
        public static final ResourceKey<Registry<ParticleType<?>>> PARTICLE_TYPES =
            ResourceKey.createRegistryKey(Visuality.location("particle_type"));
        
    }
    
    public static class Registers {
        
        public static final DeferredRegister<ParticleType<?>> PARTICLE_TYPES =
            DeferredRegister.create(Keys.PARTICLE_TYPES, Visuality.ID);
        
        public static void register(IEventBus modBus) {
            VisualityRegistries.PARTICLE_TYPES = PARTICLE_TYPES.makeRegistry(() ->
                new RegistryBuilder<ParticleType<?>>().disableSaving().disableSync());
            PARTICLE_TYPES.register(modBus);
        }
        
    }
    
}
