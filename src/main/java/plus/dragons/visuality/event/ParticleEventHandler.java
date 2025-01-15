package plus.dragons.visuality.event;

import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.monster.Creeper;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.tick.EntityTickEvent;
import plus.dragons.visuality.config.Config;
import plus.dragons.visuality.registry.VisualityParticles;

@EventBusSubscriber(value = Dist.CLIENT)
public class ParticleEventHandler {
    
    @SubscribeEvent
    public static void spawnChargeParticles(EntityTickEvent.Post event) {
        Entity entity = event.getEntity();
        if(entity instanceof LivingEntity living){
            Level level = living.level();
            RandomSource random = living.getRandom();
            if (Config.CHARGE_ENABLED.get() &&
                    random.nextInt(20) == 0 &&
                    living instanceof Creeper creeper &&
                    creeper.isPowered() &&
                    living.isAlive())
            {
                AABB aabb = living.getBoundingBox().inflate(0.5);
                double x = Mth.lerp(random.nextDouble(), aabb.minX, aabb.maxX);
                double y = Mth.lerp(random.nextDouble(), aabb.minY, aabb.maxY);
                double z = Mth.lerp(random.nextDouble(), aabb.minZ, aabb.maxZ);
                level.addParticle(VisualityParticles.CHARGE.get(), x, y, z, 0, 0, 0);
            }
        }
    }
    
}
