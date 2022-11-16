package com.github.channelingmc.visuality.mixin;

import com.github.channelingmc.visuality.registry.VisualityParticles;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.WaterFluid;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import static com.github.channelingmc.visuality.config.VisualityConfig.*;

@Mixin(WaterFluid.class)
public class WaterFluidMixin {
    
    @Inject(method = "animateTick", at = @At(value = "HEAD"))
    private void animateTick$addWaterCircles(Level level, BlockPos pos, FluidState state, RandomSource random, CallbackInfo ci) {
        if (!WATER_CIRCLE_ENABLED.get())
            return;
        int density = WATER_CIRCLE_DENSITY.get();
        if (density > 0 && random.nextInt(256) < density) {
            BlockPos above = pos.above();
            if (state.isSource() && level.isRainingAt(above)) {
                Biome biome = level.getBiome(pos).value();
                int color = WATER_CIRCLE_COLORED.get() ? biome.getWaterColor() : 0;
                level.addParticle(VisualityParticles.WATER_CIRCLE.get(),
                    above.getX() + level.random.nextDouble(),
                    above.getY() - 7 / 64D,
                    above.getZ() + level.random.nextDouble(),
                    color, 0, 0);
            }
        }
    }
    
}
