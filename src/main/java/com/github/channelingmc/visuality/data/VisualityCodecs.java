package com.github.channelingmc.visuality.data;

import com.google.common.collect.ImmutableList;
import com.mojang.serialization.Codec;
import net.minecraft.Util;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class VisualityCodecs {
    
    public static Codec<Vec3> VEC3 = Codec.DOUBLE.listOf().comapFlatMap(
        list -> Util.fixedSize(list, 3).map(params -> new Vec3(params.get(0), params.get(1), params.get(2))),
        vec -> ImmutableList.of(vec.x, vec.y, vec.z)
    );
    
    public static Codec<ParticleOptions> PARTICLE = new CompactParticleCodec();
    
    public static <T> Codec<List<T>> compactListOf(Codec<T> codec) {
        return new CompactListCodec<>(codec);
    }
    
    public static <T> Codec<Set<T>> compactSetOf(Codec<T> codec) {
        return compactListOf(codec).xmap(HashSet::new, ArrayList::new);
    }
    
}
