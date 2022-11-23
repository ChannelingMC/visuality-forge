package com.github.channelingmc.visuality;

import com.github.channelingmc.visuality.config.ClientConfig;
import com.github.channelingmc.visuality.config.block.BlockAmbientParticles;
import com.github.channelingmc.visuality.config.block.BlockStepParticles;
import com.github.channelingmc.visuality.config.entity.EntityArmorParticles;
import com.github.channelingmc.visuality.config.entity.EntityHitParticles;
import com.github.channelingmc.visuality.particle.*;
import com.github.channelingmc.visuality.particle.type.VisualityParticleTypes;
import net.minecraft.client.Minecraft;
import net.minecraft.client.particle.ParticleEngine;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.ParticleFactoryRegisterEvent;
import net.minecraftforge.client.event.RegisterClientReloadListenersEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.fml.loading.FMLLoader;

@Mod(Visuality.ID)
public class Visuality {
    public static final String ID = "visuality";
    public static EntityHitParticles ENTITY_HIT_PARTICLES;
    public static EntityArmorParticles ENTITY_ARMOR_PARTICLES;
    public static BlockAmbientParticles BLOCK_AMBIENT_PARTICLES;
    public static BlockStepParticles BLOCK_STEP_PARTICLES;
    
    public Visuality() {
        IEventBus modBus = FMLJavaModLoadingContext.get().getModEventBus();
        VisualityParticleTypes.PARTICLE_TYPES.register(modBus);
        if (FMLLoader.getDist() == Dist.CLIENT) {
            ModLoadingContext.get().registerConfig(
                ModConfig.Type.CLIENT,
                ClientConfig.SPEC,
                ID + "/client.toml"
            );
            modBus.addListener(this::registerClientResourceListener);
            modBus.addListener(this::registerParticleProviders);
        }
    }
    
    public void registerClientResourceListener(RegisterClientReloadListenersEvent event) {
        event.registerReloadListener(ENTITY_HIT_PARTICLES = new EntityHitParticles());
        event.registerReloadListener(ENTITY_ARMOR_PARTICLES = new EntityArmorParticles());
        event.registerReloadListener(BLOCK_AMBIENT_PARTICLES = new BlockAmbientParticles());
        event.registerReloadListener(BLOCK_STEP_PARTICLES = new BlockStepParticles());
    }
    
    public void registerParticleProviders(ParticleFactoryRegisterEvent event) {
        ParticleEngine engine = Minecraft.getInstance().particleEngine;
        engine.register(VisualityParticleTypes.SPARKLE.get(), SparkleParticle.Provider::new);
        engine.register(VisualityParticleTypes.BONE.get(), SolidFallingParticle.Provider::new);
        engine.register(VisualityParticleTypes.WITHER_BONE.get(), SolidFallingParticle.Provider::new);
        engine.register(VisualityParticleTypes.FEATHER.get(), FeatherParticle.Provider::new);
        engine.register(VisualityParticleTypes.SMALL_SLIME_BLOB.get(), SlimeParticle.Provider::new);
        engine.register(VisualityParticleTypes.MEDIUM_SLIME_BLOB.get(), SlimeParticle.Provider::new);
        engine.register(VisualityParticleTypes.BIG_SLIME_BLOB.get(), SlimeParticle.Provider::new);
        engine.register(VisualityParticleTypes.CHARGE.get(), ChargeParticle.Provider::new);
        engine.register(VisualityParticleTypes.WATER_CIRCLE.get(), WaterCircleParticle.Provider::new);
        engine.register(VisualityParticleTypes.EMERALD.get(), SolidFallingParticle.Provider::new);
        engine.register(VisualityParticleTypes.SOUL.get(), SoulParticle.Provider::new);
    }
    
    public static ResourceLocation loc(String path) {
        return new ResourceLocation(ID, path);
    }

}
