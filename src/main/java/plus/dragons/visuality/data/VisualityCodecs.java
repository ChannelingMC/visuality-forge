package plus.dragons.visuality.data;

import com.mojang.serialization.Codec;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.core.registries.BuiltInRegistries;
import plus.dragons.visuality.registry.VisualityRegistries;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Function;

public class VisualityCodecs {
    public static final Codec<ParticleType<?>> PARTICLE_TYPE =
        CompositeRegistryCodec.of(BuiltInRegistries.PARTICLE_TYPE.byNameCodec(), VisualityRegistries.PARTICLE_TYPES_KEY);
    
    public static final Codec<ParticleOptions> PARTICLE_OPTIONS =
        PARTICLE_TYPE.dispatch("type", ParticleOptions::getType, ParticleType::codec);
    
    public static <T> Codec<List<T>> compressedListOf(Codec<T> codec) {
        return CompressedListCodec.of(codec);
    }
    
    public static <T> Codec<Set<T>> compressedSetOf(Codec<T> codec) {
        return CompressedListCodec.of(codec).xmap(HashSet::new, ArrayList::new);
    }
    
    public static <T, S extends Set<T>> Codec<S> compressedSetOf(Codec<T> codec, Function<List<T>, S> toSetFunction) {
        return CompressedListCodec.of(codec).xmap(toSetFunction, ArrayList::new);
    }
    
}
