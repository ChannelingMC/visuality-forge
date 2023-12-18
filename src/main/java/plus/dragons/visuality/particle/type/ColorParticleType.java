package plus.dragons.visuality.particle.type;

import com.google.common.collect.ImmutableList;
import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.serialization.Codec;
import net.minecraft.Util;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.FriendlyByteBuf;
import net.neoforged.neoforge.registries.NeoForgeRegistries;

import java.util.Locale;

public class ColorParticleType extends ParticleType<ColorParticleType.Options> {
    private final Codec<Options> codec;
    
    public ColorParticleType(boolean overrideLimiter) {
        super(overrideLimiter, Deserializer.INSTANCE);
        this.codec = Codec.FLOAT.listOf()
            .comapFlatMap(
                floats -> Util.fixedSize(floats, 3).map(it -> new Options(it.get(0), it.get(1), it.get(2))),
                options -> ImmutableList.of(options.r, options.g, options.b)
            );
    }
    
    public ColorParticleType() {
        this(false);
    }
    
    @Override
    public Codec<Options> codec() {
        return codec;
    }
    
    public Options withColor(float r, float g, float b) {
        return new Options(r, g, b);
    }
    
    public Options withColor(int color) {
        float r = (color >> 16 & 255) / 255.0F;
        float g = (color >> 8 & 255) / 255.0F;
        float b = (color & 255) / 255.0F;
        return new Options(r, g, b);
    }
    
    public class Options implements ParticleOptions {
        public final float r;
        public final float g;
        public final float b;
    
        private Options(float r, float g, float b) {
            this.r = r;
            this.g = g;
            this.b = b;
        }
    
        @Override
        public ParticleType<?> getType() {
            return ColorParticleType.this;
        }
        
        @Override
        public void writeToNetwork(FriendlyByteBuf buffer) {
            buffer.writeFloat(r);
            buffer.writeFloat(g);
            buffer.writeFloat(b);
        }
        
        @Override
        public String writeToString() {
            return String.format(Locale.ROOT, "%s %.2f %.2f %.2f", BuiltInRegistries.PARTICLE_TYPE.getKey(getType()), r, g, b);
        }
        
    }
    
    @SuppressWarnings("deprecation")
    public enum Deserializer implements ParticleOptions.Deserializer<Options> {
        INSTANCE;
        
        @Override
        public Options fromCommand(ParticleType<Options> type, StringReader reader) throws CommandSyntaxException {
            reader.expect(' ');
            float r = reader.readFloat();
            reader.expect(' ');
            float g = reader.readFloat();
            reader.expect(' ');
            float b = reader.readFloat();
            return ((ColorParticleType)type).withColor(r, g, b);
        }
        
        @Override
        public Options fromNetwork(ParticleType<Options> type, FriendlyByteBuf buffer) {
            return ((ColorParticleType)type).new Options(
                buffer.readFloat(),
                buffer.readFloat(),
                buffer.readFloat()
            );
        }
        
    }
    
}
