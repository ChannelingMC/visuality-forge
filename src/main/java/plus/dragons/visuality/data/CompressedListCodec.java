package plus.dragons.visuality.data;

import com.google.common.collect.Lists;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.DynamicOps;
import net.minecraft.nbt.NbtOps;

import java.util.List;

/**
 * A {@link Codec} which compresses singleton list into single element instead of a list. <br>
 * Element must not be a list and should not be possibly encode into a list in any cases. <br>
 * @param <A>
 * @author LimonBlaze
 */
public class CompressedListCodec<A> implements Codec<List<A>> {
    private final Codec<A> elementCodec;
    private final Codec<List<A>> listCodec;
    
    private CompressedListCodec(Codec<A> elementCodec) {
        this.elementCodec = elementCodec;
        this.listCodec = elementCodec.listOf();
    }
    
    @SafeVarargs
    public static <T> CompressedListCodec<T> of(Codec<T> elementCodec, T... typeGetter) {
        if (typeGetter.length != 0)
            throw new IllegalArgumentException("typeGetter must be of length 0!");
        Class<?> type = typeGetter.getClass().getComponentType();
        if (List.class.isAssignableFrom(type))
            throw new IllegalArgumentException("CompressedListCodec does not support List element!");
        return new CompressedListCodec<>(elementCodec);
    }
    
    @Override
    public <T> DataResult<Pair<List<A>, T>> decode(DynamicOps<T> ops, T input) {
        //Nbt is sensitive to types, so we fall back to normal list codec here
        if (ops instanceof NbtOps || ops.getStream(input).error().isEmpty()) {
            return listCodec.decode(ops, input);
        }
        return elementCodec.parse(ops, input).map(a -> Pair.of(Lists.newArrayList(a), input));
    }
    
    @Override
    public <T> DataResult<T> encode(List<A> input, DynamicOps<T> ops, T prefix) {
        //Nbt is sensitive to types, so we fall back to normal list codec here
        if (ops instanceof NbtOps || input.size() != 1 || prefix != ops.empty()) {
            return listCodec.encode(input, ops, prefix);
        }
        return elementCodec.encodeStart(ops, input.get(0));
    }
    
}
