package plus.dragons.visuality.data;

import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.JsonOps;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.neoforged.neoforge.registries.IForgeRegistry;
import net.neoforged.neoforge.registries.RegistryManager;

/**
 * <p>A {@link Codec} backed by a vanilla {@link Registry} and a custom {@link IForgeRegistry}.
 * <p>Compressed {@link DynamicOps} are only available for vanilla registries,
 * this {@link Codec} should be used with {@link JsonOps#INSTANCE}.
 * @param <A>
 * @author LimonBlaze
 */
public class CompositeRegistryCodec<A> implements Codec<A> {
    private final Codec<A> primaryCodec;
    private final ResourceKey<Registry<A>> secondaryKey;
    private Codec<A> secondaryCodec;
    
    private CompositeRegistryCodec(Codec<A> primaryCodec, ResourceKey<Registry<A>> secondaryKey) {
        this.primaryCodec = primaryCodec;
        this.secondaryKey = secondaryKey;
    }
    
    public static <T> CompositeRegistryCodec<T> of(Codec<T> primaryCodec, ResourceKey<Registry<T>> secondaryKey) {
        return new CompositeRegistryCodec<>(primaryCodec, secondaryKey);
    }
    
    @Override
    public <T> DataResult<Pair<A, T>> decode(DynamicOps<T> ops, T input) {
        final var result = primaryCodec.decode(ops, input);
        if (result.error().isEmpty() || ops.compressMaps())
            return result;
        if (secondaryCodec == null) {
            IForgeRegistry<A> registry = RegistryManager.ACTIVE.getRegistry(secondaryKey);
            if (registry == null)
                return result;
            secondaryCodec = registry.getCodec();
        }
        return secondaryCodec.decode(ops, input);
    }
    
    @Override
    public <T> DataResult<T> encode(A input, DynamicOps<T> ops, T prefix) {
        final var result = primaryCodec.encode(input, ops, prefix);
        if (result.error().isEmpty() || ops.compressMaps())
            return result;
        if (secondaryCodec == null) {
            IForgeRegistry<A> registry = RegistryManager.ACTIVE.getRegistry(secondaryKey);
            if (registry == null)
                return result;
            secondaryCodec = registry.getCodec();
        }
        return secondaryCodec.encode(input, ops, prefix);
    }
    
}
