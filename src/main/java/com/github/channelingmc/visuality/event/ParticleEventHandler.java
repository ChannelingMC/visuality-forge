package com.github.channelingmc.visuality.event;

import com.github.channelingmc.visuality.config.ClientConfig;
import com.github.channelingmc.visuality.particle.type.VisualityParticleTypes;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.PowerableMob;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(value = Dist.CLIENT)
public class ParticleEventHandler {
    
    @SubscribeEvent
    public static void spawnChargeParticles(LivingEvent.LivingTickEvent event) {
        LivingEntity entity = event.getEntity();
        Level level = entity.getLevel();
        RandomSource random = entity.getRandom();
        if (level.isClientSide &&
            random.nextInt(20) == 0 &&
            entity instanceof PowerableMob powerable &&
            powerable.isPowered() &&
            entity.isAlive() &&
            ClientConfig.CHARGE_ENABLED.get())
        {
            AABB aabb = entity.getBoundingBox().inflate(0.5);
            double x = Mth.lerp(random.nextDouble(), aabb.minX, aabb.maxX);
            double y = Mth.lerp(random.nextDouble(), aabb.minY, aabb.maxY);
            double z = Mth.lerp(random.nextDouble(), aabb.minZ, aabb.maxZ);
            level.addParticle(VisualityParticleTypes.CHARGE.get(), x, y, z, 0, 0, 0);
        }
    }
    
}
