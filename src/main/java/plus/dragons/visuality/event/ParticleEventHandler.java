package plus.dragons.visuality.event;

import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.PowerableMob;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.event.entity.living.LivingEvent;
import plus.dragons.visuality.config.Config;
import plus.dragons.visuality.registry.VisualityParticles;

@Mod.EventBusSubscriber(value = Dist.CLIENT)
public class ParticleEventHandler {
    
    @SubscribeEvent
    public static void spawnChargeParticles(LivingEvent.LivingTickEvent event) {
        LivingEntity entity = event.getEntity();
        Level level = entity.level();
        RandomSource random = entity.getRandom();
        if (Config.CHARGE_ENABLED.get() &&
            random.nextInt(20) == 0 &&
            entity instanceof PowerableMob powerable &&
            powerable.isPowered() &&
            entity.isAlive())
        {
            AABB aabb = entity.getBoundingBox().inflate(0.5);
            double x = Mth.lerp(random.nextDouble(), aabb.minX, aabb.maxX);
            double y = Mth.lerp(random.nextDouble(), aabb.minY, aabb.maxY);
            double z = Mth.lerp(random.nextDouble(), aabb.minZ, aabb.maxZ);
            level.addParticle(VisualityParticles.CHARGE.get(), x, y, z, 0, 0, 0);
        }
    }
    
}
