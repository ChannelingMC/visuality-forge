package com.github.channelingmc.visuality.particle.type;

import com.github.channelingmc.visuality.Visuality;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

import java.util.function.Supplier;

public class VisualityParticleTypes {
    public static final DeferredRegister<ParticleType<?>> PARTICLE_TYPES =
        DeferredRegister.create(ForgeRegistries.PARTICLE_TYPES, Visuality.ID);
    
    public static final RegistryObject<ColorParticleType> SPARKLE =
        register("sparkle", ColorParticleType::new);
    public static final RegistryObject<SimpleParticleType> BONE =
        register("bone");
    public static final RegistryObject<SimpleParticleType> WITHER_BONE =
        register("wither_bone");
    public static final RegistryObject<SimpleParticleType> FEATHER =
        register("feather");
    public static final RegistryObject<ColorScaleParticleType> SMALL_SLIME_BLOB =
        register("small_slime_blob", ColorScaleParticleType::new);
    public static final RegistryObject<ColorScaleParticleType> MEDIUM_SLIME_BLOB =
        register("medium_slime_blob", ColorScaleParticleType::new);
    public static final RegistryObject<ColorScaleParticleType> BIG_SLIME_BLOB =
        register("big_slime_blob", ColorScaleParticleType::new);
    public static final RegistryObject<SimpleParticleType> CHARGE =
        register("charge");
    public static final RegistryObject<ColorParticleType> WATER_CIRCLE =
        register("water_circle", ColorParticleType::new);
    public static final RegistryObject<SimpleParticleType> EMERALD =
        register("emerald");
    public static final RegistryObject<SimpleParticleType> SOUL =
        register("soul");

    private static RegistryObject<SimpleParticleType> register(String name) {
        return PARTICLE_TYPES.register(name, () -> new SimpleParticleType(false));
    }
    
    private static <O extends ParticleOptions, T extends ParticleType<O>>
    RegistryObject<T> register(String name, Supplier<T> factory) {
        return PARTICLE_TYPES.register(name, factory);
    }
    
}
