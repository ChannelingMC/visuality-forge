package com.github.channelingmc.visuality.event;

import com.github.channelingmc.visuality.config.VisualityConfig;
import com.github.channelingmc.visuality.registry.VisualityParticles;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.PowerableMob;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.entity.living.LivingAttackEvent;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(value = Dist.CLIENT)
public class EntityParticleEventHandler {
    
    private static final EquipmentSlot[] ARMOR_SLOTS = new EquipmentSlot[]{
        EquipmentSlot.FEET,
        EquipmentSlot.LEGS,
        EquipmentSlot.CHEST,
        EquipmentSlot.HEAD
    };
    
    @SubscribeEvent(priority = EventPriority.LOWEST, receiveCanceled = true)
    public static void hit(LivingAttackEvent event) {
        LivingEntity entity = event.getEntity();
        DamageSource source = event.getSource();
        float amount = event.getAmount();
        if (VisualityConfig.HIT_PARTICLES_ENABLED.get() &&
            amount > 0 &&
            source.getEntity() instanceof Player &&
            !entity.isInvulnerableTo(source) &&
            !entity.isDeadOrDying() &&
            !(source.isFire() && entity.hasEffect(MobEffects.FIRE_RESISTANCE)))
        {
            ParticleOptions particle = VisualityConfig.HIT_PARTICLE_REGISTRY.get(entity.getType());
            if (particle != null) {
                RandomSource random = entity.getRandom();
                int count = Mth.ceil(Math.min(entity.getHealth(), amount) / 2);
                float height = entity.getBbHeight();
                for(int i = 0; i <= count; i++) {
                    entity.level.addParticle(particle,
                        entity.getX(),
                        entity.getY() + random.nextDouble() * height,
                        entity.getZ(),
                        0, 0, 0
                    );
                }
            }
        }
    }
    
    @SubscribeEvent
    public static void armorSparkle(LivingEvent.LivingTickEvent event) {
        LivingEntity entity = event.getEntity();
        Level level = entity.level;
        RandomSource random = entity.getRandom();
        if (!level.isClientSide ||
            !VisualityConfig.SHINY_ARMOR_ENABLED.get() ||
            random.nextInt(32) != 0 ||
            !entity.isAlive())
            return;
        
        Minecraft minecraft = Minecraft.getInstance();
        
        EntityRenderer<?> renderer = minecraft.getEntityRenderDispatcher().getRenderer(entity);
        if (!(renderer instanceof RenderLayerParent<?,?> parent && parent.getModel() instanceof HumanoidModel<?>))
            return;
        
        if (minecraft.cameraEntity == entity && minecraft.options.getCameraType().isFirstPerson())
            return;
        
        boolean swimming = entity.isVisuallySwimming();
        AABB aabb = entity.getBoundingBox();
        for (int index = 0; index < 4; ++index) {
            ItemStack armor = entity.getItemBySlot(ARMOR_SLOTS[index]);
            if (VisualityConfig.SHINY_ARMOR_REGISTRY.contains(armor.getItem())) {
                double x, y, z;
                x = Mth.lerp(random.nextDouble(), aabb.minX, aabb.maxX);
                z = Mth.lerp(random.nextDouble(), aabb.minZ, aabb.maxZ);
                if (swimming) {
                    y = Mth.lerp(random.nextDouble(), aabb.minY, aabb.maxY);
                } else {
                    double sizeY = aabb.getYsize();
                    double minY = aabb.minY + sizeY / 4 * index;
                    double maxY = minY + sizeY / 4;
                    y = Mth.lerp(random.nextDouble(), minY, maxY);
                }
                level.addParticle(VisualityParticles.SPARKLE.get(), x, y, z, 0, 0, 0);
            }
        }
    }
    
    @SubscribeEvent
    public static void charge(LivingEvent.LivingTickEvent event) {
        LivingEntity entity = event.getEntity();
        Level level = entity.getLevel();
        RandomSource random = entity.getRandom();
        if (level.isClientSide &&
            random.nextInt(20) == 0 &&
            entity instanceof PowerableMob powerable &&
            powerable.isPowered() &&
            entity.isAlive() &&
            VisualityConfig.CHARGE_ENABLED.get())
        {
            AABB aabb = entity.getBoundingBox().inflate(0.5);
            double x = Mth.lerp(random.nextDouble(), aabb.minX, aabb.maxX);
            double y = Mth.lerp(random.nextDouble(), aabb.minY, aabb.maxY);
            double z = Mth.lerp(random.nextDouble(), aabb.minZ, aabb.maxZ);
            level.addParticle(VisualityParticles.CHARGE.get(), x, y, z, 0, 0, 0);
        }
    }
    
}
