package plus.dragons.visuality.registry;

import net.minecraft.client.Minecraft;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.core.particles.SimpleParticleType;
import net.neoforged.neoforge.client.event.RegisterParticleProvidersEvent;
import net.neoforged.neoforge.registries.RegistryObject;
import plus.dragons.visuality.particle.*;
import plus.dragons.visuality.particle.type.ColorParticleType;
import plus.dragons.visuality.particle.type.ColorScaleParticleType;

import java.util.function.Supplier;

import static plus.dragons.visuality.registry.VisualityRegistries.Registers.PARTICLE_TYPES;

public class VisualityParticles {
    
    public static final RegistryObject<ColorParticleType> SPARKLE = register("sparkle", ColorParticleType::new);
    public static final RegistryObject<SimpleParticleType> BONE = register("bone");
    public static final RegistryObject<SimpleParticleType> WITHER_BONE = register("wither_bone");
    public static final RegistryObject<SimpleParticleType> FEATHER = register("feather");
    public static final RegistryObject<ColorScaleParticleType> SMALL_SLIME_BLOB = register("small_slime_blob", ColorScaleParticleType::new);
    public static final RegistryObject<ColorScaleParticleType> MEDIUM_SLIME_BLOB = register("medium_slime_blob", ColorScaleParticleType::new);
    public static final RegistryObject<ColorScaleParticleType> BIG_SLIME_BLOB = register("big_slime_blob", ColorScaleParticleType::new);
    public static final RegistryObject<SimpleParticleType> CHARGE = register("charge");
    public static final RegistryObject<ColorParticleType> WATER_CIRCLE = register("water_circle", ColorParticleType::new);
    public static final RegistryObject<SimpleParticleType> EMERALD = register("emerald");
    public static final RegistryObject<SimpleParticleType> SOUL = register("soul");

    private static RegistryObject<SimpleParticleType> register(String name) {
        return PARTICLE_TYPES.register(name, () -> new SimpleParticleType(false));
    }
    
    private static <O extends ParticleOptions, T extends ParticleType<O>> RegistryObject<T> register(String name, Supplier<T> factory) {
        return VisualityRegistries.Registers.PARTICLE_TYPES.register(name, factory);
    }
    
    public static void register() {}
    
    public static void registerProviders(RegisterParticleProvidersEvent event) {
        VisualityParticleEngine engine = ((VisualityParticleEngine)Minecraft.getInstance().particleEngine);
        engine.registerVisuality(SPARKLE, SparkleParticle.Provider::new);
        engine.registerVisuality(BONE, SolidFallingParticle.Provider::new);
        engine.registerVisuality(WITHER_BONE, SolidFallingParticle.Provider::new);
        engine.registerVisuality(FEATHER, FeatherParticle.Provider::new);
        engine.registerVisuality(SMALL_SLIME_BLOB, SlimeParticle.Provider::new);
        engine.registerVisuality(MEDIUM_SLIME_BLOB, SlimeParticle.Provider::new);
        engine.registerVisuality(BIG_SLIME_BLOB, SlimeParticle.Provider::new);
        engine.registerVisuality(CHARGE, ChargeParticle.Provider::new);
        engine.registerVisuality(WATER_CIRCLE, WaterCircleParticle.Provider::new);
        engine.registerVisuality(EMERALD, SolidFallingParticle.Provider::new);
        engine.registerVisuality(SOUL, SoulParticle.Provider::new);
    }
    
}
