package com.github.channelingmc.visuality.mixin;

import com.github.channelingmc.visuality.config.VisualityConfig;
import com.github.channelingmc.visuality.registry.VisualityParticles;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.monster.Slime;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@MethodsReturnNonnullByDefault
@Mixin(Slime.class)
public abstract class SlimeMixin extends Mob {
    
    @Shadow public abstract int getSize();
    
    @Shadow public abstract EntityType<? extends Slime> getType();
    
    private SlimeMixin(EntityType<? extends Mob> pEntityType, Level pLevel) {
        super(pEntityType, pLevel);
    }
    
    @Inject(method = "spawnCustomParticles", at = @At("RETURN"), cancellable = true, remap = false)
    private void getParticleType$modify(CallbackInfoReturnable<Boolean> cir) {
        if (VisualityConfig.SLIME_ENABLED.get() && this.getType() == EntityType.SLIME) {
            int size = getSize();
            SimpleParticleType particleType = switch (size) {
                case 1 -> VisualityParticles.SMALL_SLIME_BLOB.get();
                case 2 -> VisualityParticles.MEDIUM_SLIME_BLOB.get();
                default -> VisualityParticles.BIG_SLIME_BLOB.get();
            };
            int i = getSize();
            for(int j = 0; j < i * 8; ++j) {
                float f = this.random.nextFloat() * ((float)Math.PI * 2F);
                float f1 = this.random.nextFloat() * 0.5F + 0.5F;
                float f2 = Mth.sin(f) * (float)i * 0.5F * f1;
                float f3 = Mth.cos(f) * (float)i * 0.5F * f1;
                this.level.addParticle(particleType,
                    this.getX() + f2, this.getY(), this.getZ() + f3,
                    8978297, size > 2 ? 2 : 1, 0);
            }
            cir.setReturnValue(true);
        }
    }

}
