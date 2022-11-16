package com.github.channelingmc.visuality.mixin;

import com.github.channelingmc.visuality.config.VisualityConfig;
import com.github.channelingmc.visuality.registry.VisualityParticles;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Random;

@Mixin(Block.class)
public abstract class BlockMixin extends BlockBehaviour implements ItemLike {

    public BlockMixin(Properties properties) {
        super(properties);
    }

    @Inject(method = "fallOn", at = @At("TAIL"))
    void fallOn(Level level, BlockState state, BlockPos pos, Entity entity, float f, CallbackInfo ci) {
        if(VisualityConfig.SOUL_ENABLED.get() && state.is(BlockTags.WITHER_SUMMON_BASE_BLOCKS)) {
            double x = entity.getX();
            double y = entity.getY() + 0.125;
            double z = entity.getZ();
            for(int i = 0; i <= level.random.nextInt(5) + 1; i++) {
                level.addParticle(VisualityParticles.SOUL.get(), x, y, z, 0, 0, 0);
            }
        }
    }

    @Inject(method = "animateTick", at = @At("TAIL"))
    void animateTick(BlockState state, Level level, BlockPos pos, Random random, CallbackInfo ci) {
        if (VisualityConfig.SOUL_ENABLED.get() &&
            state.is(BlockTags.WITHER_SUMMON_BASE_BLOCKS) &&
            level.getBlockState(pos.above()).isAir() &&
            random.nextFloat() < .005F)
        {
            double x = pos.getX() + random.nextDouble();
            double y = pos.getY() + 1.1D;
            double z = pos.getZ() + random.nextDouble();
            level.addParticle(VisualityParticles.SOUL.get(), x, y, z, 0, 0, 0);
        }
        if (VisualityConfig.SHINY_BLOCK_ENABLED.get() && VisualityConfig.SHINY_BLOCK_REGISTRY.contains(this)) {
            for(Direction direction : Direction.values()) {
                BlockPos blockPos = pos.relative(direction);
                if(!level.getBlockState(blockPos).isSolidRender(level, blockPos)) {
                    if(random.nextFloat() > 0.8) {
                        Direction.Axis axis = direction.getAxis();
                        double x = axis == Direction.Axis.X ? 0.5 + 0.5625 * direction.getStepX() : random.nextFloat();
                        double y = axis == Direction.Axis.Y ? 0.5 + 0.5625 * direction.getStepY() : random.nextFloat();
                        double z = axis == Direction.Axis.Z ? 0.5 + 0.5625 * direction.getStepZ() : random.nextFloat();
                        level.addParticle(VisualityParticles.SPARKLE.get(), pos.getX() + x, pos.getY() + y, pos.getZ() + z, 0, 0, 0);
                    }
                }
            }
        }
    }

}
