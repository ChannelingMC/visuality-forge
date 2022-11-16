package com.github.channelingmc.visuality.mixin;

import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.monster.Creeper;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import com.github.channelingmc.visuality.config.VisualityConfig;
import com.github.channelingmc.visuality.registry.VisualityParticles;

@Mixin(Creeper.class)
public abstract class CreeperMixin extends Monster {
    
    @Shadow public abstract boolean isPowered();
    
    protected CreeperMixin(EntityType<? extends Monster> entityType, Level world) {
        super(entityType, world);
    }

    @Inject(method = "tick", at = @At("TAIL"))
    void tick(CallbackInfo ci) {
        if (this.level.isClientSide &&
            this.isAlive() &&
            this.isPowered() &&
            VisualityConfig.CHARGE_ENABLED.get()) {
            if(this.random.nextInt(20) == 0) {
                double x = random.nextFloat() * 2 - 1;
                double y = random.nextFloat();
                double z = random.nextFloat() * 2 - 1;
                level.addParticle(VisualityParticles.CHARGE.get(), this.getX() + x, this.getY() + y + 1, this.getZ() + z, 0, 0, 0);
            }
        }
    }

}
