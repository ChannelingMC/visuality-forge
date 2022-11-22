package com.github.channelingmc.visuality.mixin;

import com.github.channelingmc.visuality.Visuality;
import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
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

@Mixin(Block.class)
public abstract class BlockMixin extends BlockBehaviour implements ItemLike {

    public BlockMixin(Properties properties) {
        super(properties);
    }

    @Inject(method = "fallOn", at = @At("TAIL"))
    private void fallOn$spawnParticles(Level level, BlockState state, BlockPos pos, Entity entity, float f, CallbackInfo ci) {
        if (level.isClientSide) {
            int amount = Mth.log2(Mth.ceil(f)) + 1;
            Visuality.BLOCK_STEP_PARTICLES.spawnParticles(amount, level, state, pos, entity);
        }
    }
    
    @Inject(method = "stepOn", at = @At("TAIL"))
    private void stepOn$spawnParticles(Level level, BlockPos pos, BlockState state, Entity entity, CallbackInfo ci) {
        if (level.isClientSide && (entity.tickCount - entity.getId()) % Visuality.BLOCK_STEP_PARTICLES.interval() == 0) {
            Visuality.BLOCK_STEP_PARTICLES.spawnParticles(1, level, state, pos, entity);
        }
    }

    @Inject(method = "animateTick", at = @At("TAIL"))
    private void animateTick$spawnParticles(BlockState state, Level level, BlockPos pos, RandomSource random, CallbackInfo ci) {
        Visuality.BLOCK_AMBIENT_PARTICLES.spawnParticles(state, level, pos, random);
    }

}
