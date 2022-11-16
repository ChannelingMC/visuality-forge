package com.github.channelingmc.visuality.registry;

import com.github.channelingmc.visuality.Visuality;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class VisualityParticles {
    public static final DeferredRegister<ParticleType<?>> PARTICLE_TYPES =
        DeferredRegister.create(ForgeRegistries.PARTICLE_TYPES, Visuality.ID);
    
    public static final RegistryObject<SimpleParticleType> SPARKLE = simple("sparkle");
    public static final RegistryObject<SimpleParticleType> BONE = simple("bone");
    public static final RegistryObject<SimpleParticleType> WITHER_BONE = simple("wither_bone");
    public static final RegistryObject<SimpleParticleType> FEATHER = simple("feather");
    public static final RegistryObject<SimpleParticleType> SMALL_SLIME_BLOB = simple("small_slime_blob");
    public static final RegistryObject<SimpleParticleType> MEDIUM_SLIME_BLOB = simple("medium_slime_blob");
    public static final RegistryObject<SimpleParticleType> BIG_SLIME_BLOB = simple("big_slime_blob");
    public static final RegistryObject<SimpleParticleType> CHARGE = simple("charge");
    public static final RegistryObject<SimpleParticleType> WATER_CIRCLE = simple("water_circle");
    public static final RegistryObject<SimpleParticleType> EMERALD = simple("emerald");
    public static final RegistryObject<SimpleParticleType> SOUL = simple("soul");

    private static RegistryObject<SimpleParticleType> simple(String name) {
        return PARTICLE_TYPES.register(name, () -> new SimpleParticleType(false));
    }
    
}
