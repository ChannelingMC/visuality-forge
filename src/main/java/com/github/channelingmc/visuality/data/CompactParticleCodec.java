package com.github.channelingmc.visuality.data;

import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.DynamicOps;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.NbtOps;
import net.minecraftforge.registries.ForgeRegistries;

/**
 * A codec for {@link ParticleOptions} which compacts {@link net.minecraft.core.particles.SimpleParticleType} into {@link net.minecraft.resources.ResourceLocation}
 */
public class CompactParticleCodec implements Codec<ParticleOptions> {
    private static final Codec<ParticleType<?>> PARTICLE_TYPE_CODEC = ForgeRegistries.PARTICLE_TYPES.getCodec();
    
    CompactParticleCodec() {}
    
    @Override
    public <T> DataResult<Pair<ParticleOptions, T>> decode(DynamicOps<T> ops, T input) {
        //Nbt is sensitive to types, so we fall back to normal ParticleTypes#CODEC here
        if (ops instanceof NbtOps)
            return ParticleTypes.CODEC.decode(ops, input);
        return PARTICLE_TYPE_CODEC.parse(ops, input)
            .flatMap(type -> type instanceof ParticleOptions particle
                ? DataResult.success(particle)
                : DataResult.error(ops.getStringValue(input) + " is not a ParticleOption!"))
            .get()
            .map(
                particle -> DataResult.success(Pair.of(particle, input)),
                partial -> ParticleTypes.CODEC.decode(ops, input)
            );
    }
    
    @Override
    public <T> DataResult<T> encode(ParticleOptions input, DynamicOps<T> ops, T prefix) {
        //Nbt is sensitive to types, so we fall back to normal ParticleTypes#CODEC here
        if (ops instanceof NbtOps)
            return ParticleTypes.CODEC.encode(input, ops, prefix);
        return input instanceof ParticleType<?> type
            ? PARTICLE_TYPE_CODEC.encode(type, ops, prefix)
            : ParticleTypes.CODEC.encode(input, ops, prefix);
    }
    
}
