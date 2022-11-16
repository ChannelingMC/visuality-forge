package com.github.channelingmc.visuality;

import com.github.channelingmc.visuality.config.VisualityConfig;
import com.github.channelingmc.visuality.particle.*;
import com.github.channelingmc.visuality.registry.VisualityParticles;
import net.minecraft.client.Minecraft;
import net.minecraft.client.particle.ParticleEngine;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.ParticleFactoryRegisterEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.config.ModConfigEvent;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.fml.loading.FMLLoader;

@Mod(Visuality.ID)
public class Visuality {
    public static final String ID = "visuality";
    private boolean setup = false;
    
    public Visuality() {
        IEventBus modBus = FMLJavaModLoadingContext.get().getModEventBus();
        VisualityParticles.PARTICLE_TYPES.register(modBus);
        if (FMLLoader.getDist() == Dist.CLIENT) {
            ModLoadingContext.get().registerConfig(ModConfig.Type.CLIENT, VisualityConfig.SPEC);
            modBus.addListener(this::registerParticleProviders);
            modBus.addListener(this::onClientSetup);
            modBus.addListener(this::onConfigChange);
        }
    }
    
    public void onClientSetup(FMLClientSetupEvent event) {
        setup = true;
        VisualityConfig.reload();
    }
    
    public void onConfigChange(ModConfigEvent event) {
        if (setup && event.getConfig().getType() == ModConfig.Type.CLIENT)
            VisualityConfig.reload();
    }
    
    public void registerParticleProviders(ParticleFactoryRegisterEvent event) {
        ParticleEngine engine = Minecraft.getInstance().particleEngine;
        engine.register(VisualityParticles.SPARKLE.get(), SparkleParticle.Factory::new);
        engine.register(VisualityParticles.BONE.get(), SolidFallingParticle.Factory::new);
        engine.register(VisualityParticles.WITHER_BONE.get(), SolidFallingParticle.Factory::new);
        engine.register(VisualityParticles.FEATHER.get(), FeatherParticle.Factory::new);
        engine.register(VisualityParticles.SMALL_SLIME_BLOB.get(), SlimeParticle.Factory::new);
        engine.register(VisualityParticles.MEDIUM_SLIME_BLOB.get(), SlimeParticle.Factory::new);
        engine.register(VisualityParticles.BIG_SLIME_BLOB.get(), SlimeParticle.Factory::new);
        engine.register(VisualityParticles.CHARGE.get(), ChargeParticle.Factory::new);
        engine.register(VisualityParticles.WATER_CIRCLE.get(), WaterCircleParticle.Factory::new);
        engine.register(VisualityParticles.EMERALD.get(), SolidFallingParticle.Factory::new);
        engine.register(VisualityParticles.SOUL.get(), SoulParticle.Provider::new);
    }
    
    public static ResourceLocation loc(String path) {
        return new ResourceLocation(ID, path);
    }

}
