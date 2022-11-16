package com.github.channelingmc.visuality;

import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.DiggerItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.SwordItem;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RegisterParticleProvidersEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.config.ModConfigEvent;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.fml.loading.FMLLoader;
import com.github.channelingmc.visuality.config.VisualityConfig;
import com.github.channelingmc.visuality.particle.*;
import com.github.channelingmc.visuality.registry.VisualityParticles;

@Mod(Visuality.ID)
public class Visuality {
    public static final String ID = "visuality";
    private boolean setup = false;
    
    public Visuality() {
        IEventBus modBus = FMLJavaModLoadingContext.get().getModEventBus();
        IEventBus forgeBus = MinecraftForge.EVENT_BUS;
        VisualityParticles.PARTICLE_TYPES.register(modBus);
        if (FMLLoader.getDist() == Dist.CLIENT) {
            ModLoadingContext.get().registerConfig(ModConfig.Type.CLIENT, VisualityConfig.SPEC);
            modBus.addListener(this::registerParticleProviders);
            modBus.addListener(this::onClientSetup);
            modBus.addListener(this::onConfigChange);
            forgeBus.addListener(this::clientTick);
            forgeBus.addListener(this::livingHurt);
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
    
    public void registerParticleProviders(RegisterParticleProvidersEvent event) {
        event.register(VisualityParticles.SPARKLE.get(), SparkleParticle.Factory::new);
        event.register(VisualityParticles.BONE.get(), SolidFallingParticle.Factory::new);
        event.register(VisualityParticles.WITHER_BONE.get(), SolidFallingParticle.Factory::new);
        event.register(VisualityParticles.FEATHER.get(), FeatherParticle.Factory::new);
        event.register(VisualityParticles.SMALL_SLIME_BLOB.get(), SlimeParticle.Factory::new);
        event.register(VisualityParticles.MEDIUM_SLIME_BLOB.get(), SlimeParticle.Factory::new);
        event.register(VisualityParticles.BIG_SLIME_BLOB.get(), SlimeParticle.Factory::new);
        event.register(VisualityParticles.CHARGE.get(), ChargeParticle.Factory::new);
        event.register(VisualityParticles.WATER_CIRCLE.get(), WaterCircleParticle.Factory::new);
        event.register(VisualityParticles.EMERALD.get(), SolidFallingParticle.Factory::new);
        event.register(VisualityParticles.SOUL.get(), SoulParticle.Provider::new);
    }
    
    public void clientTick(TickEvent.ClientTickEvent event) {
        if (event.phase == TickEvent.Phase.END) {
            WaterCircleParticle.generate();
        }
    }
    
    public void livingHurt(LivingHurtEvent event) {
        LivingEntity entity = event.getEntity();
        if (event.getSource().getEntity() instanceof LivingEntity attacker &&
            entity.isAlive() &&
            entity.invulnerableTime == 0 &&
            VisualityConfig.HIT_PARTICLES_ENABLED.get()) {
            ParticleOptions particle = VisualityConfig.HIT_PARTICLE_REGISTRY.get(entity.getType());
            if (particle != null) {
                Item item = attacker.getMainHandItem().getItem();
                RandomSource random = entity.getRandom();
                int count;
                if (item instanceof SwordItem sword)
                    count = (int) sword.getDamage() / 2;
                else if(item instanceof DiggerItem digger)
                    count = (int) digger.getAttackDamage() / 2;
                else
                    count = random.nextInt(2);
                float height = entity.getBbHeight();
                for(int i = 0; i <= count; i++) {
                    double randomHeight = random.nextDouble() * height;
                    entity.level.addParticle(particle, entity.getX(), entity.getY() + 0.2D + randomHeight, entity.getZ(), 0, 0, 0);
                }
            }
        }
    }
    
    public static ResourceLocation loc(String path) {
        return new ResourceLocation(ID, path);
    }

}
