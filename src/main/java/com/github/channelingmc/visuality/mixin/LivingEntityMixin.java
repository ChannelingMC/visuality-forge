package com.github.channelingmc.visuality.mixin;

import net.minecraft.client.Minecraft;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.DiggerItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.SwordItem;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import com.github.channelingmc.visuality.config.VisualityConfig;
import com.github.channelingmc.visuality.registry.VisualityParticles;

@Mixin(LivingEntity.class)
public abstract class LivingEntityMixin extends Entity {

    public LivingEntityMixin(EntityType<?> type, Level world) {
        super(type, world);
    }

    @Inject(method = "tick", at = @At("TAIL"))
    private void tick(CallbackInfo ci) {
        if (level.isClientSide) {
            var client = Minecraft.getInstance();
            if (this.isAlive() && client.player != null && VisualityConfig.SHINY_ARMOR_ENABLED.get()) {
                int shiny = 0;
                for(ItemStack stack : LivingEntity.class.cast(this).getArmorSlots()) {
                    if(VisualityConfig.SHINY_ARMOR_REGISTRY.contains(stack.getItem())) {
                        shiny++;
                    }
                }
                if (shiny == 0) return;
                if (client.player != (Object)this || !client.options.getCameraType().isFirstPerson()) {
                    if(this.random.nextInt(20 - shiny) == 0) {
                        double x = random.nextFloat() * 2 - 1;
                        double y = random.nextFloat();
                        double z = random.nextFloat() * 2 - 1;
                        level.addParticle(VisualityParticles.SPARKLE.get(), this.getX() + x, this.getY() + y + 1, this.getZ() + z, 0, 0, 0);
                    }
                }
            }
        }
    }

    @Inject(method = "hurt", at = @At("HEAD"))
    void damage(DamageSource source, float amount, CallbackInfoReturnable<Boolean> cir) {
        if (level.isClientSide &&
            source.getEntity() instanceof LivingEntity attacker &&
            this.isAlive() &&
            invulnerableTime == 0 &&
            VisualityConfig.HIT_PARTICLES_ENABLED.get()) {
            ParticleOptions particle = VisualityConfig.HIT_PARTICLE_REGISTRY.get(this.getType());
            if (particle != null) {
                Item item = attacker.getMainHandItem().getItem();
                int count;
                if (item instanceof SwordItem sword)
                    count = (int) sword.getDamage() / 2;
                else if(item instanceof DiggerItem digger)
                    count = (int) digger.getAttackDamage() / 2;
                else
                    count = this.random.nextInt(2);
                float height = this.getBbHeight();
                for(int i = 0; i <= count; i++) {
                    double randomHeight = this.random.nextDouble() * height;
                    level.addParticle(particle, this.getX(), this.getY() + 0.2D + randomHeight, this.getZ(), 0, 0, 0);
                }
            }
        }
    }
    
}
