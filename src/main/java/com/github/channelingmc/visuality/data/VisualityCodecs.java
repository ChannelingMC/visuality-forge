package com.github.channelingmc.visuality.data;

import com.mojang.serialization.Codec;
import net.minecraft.core.particles.ParticleOptions;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class VisualityCodecs {
    
    public static Codec<ParticleOptions> PARTICLE = new CompactParticleCodec();
    
    public static <T> Codec<List<T>> compactListOf(Codec<T> codec) {
        return new CompactListCodec<>(codec);
    }
    
    public static <T> Codec<Set<T>> compactSetOf(Codec<T> codec) {
        return compactListOf(codec).xmap(HashSet::new, ArrayList::new);
    }
    
}
