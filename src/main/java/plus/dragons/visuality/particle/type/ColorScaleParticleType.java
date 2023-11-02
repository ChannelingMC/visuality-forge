package plus.dragons.visuality.particle.type;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.util.ExtraCodecs;
import net.neoforged.neoforge.registries.ForgeRegistries;
import org.joml.Vector3f;

import java.util.Locale;

public class ColorScaleParticleType extends ParticleType<ColorScaleParticleType.Options> {
    private final Codec<Options> codec;
    
    public ColorScaleParticleType(boolean overrideLimiter) {
        super(overrideLimiter, Deserializer.INSTANCE);
        this.codec = RecordCodecBuilder.create(instance -> instance.group(
            ExtraCodecs.VECTOR3F.fieldOf("color")
                .forGetter(Options::color),
            Codec.FLOAT.fieldOf("scale")
                .forGetter(Options::scale)
        ).apply(instance, Options::new));
    }
    
    public ColorScaleParticleType() {
        this(false);
    }
    
    public Options withColor(float r, float g, float b) {
        return new Options(r, g, b, 1);
    }
    
    public Options withColor(int color) {
        float r = (color >> 16 & 255) / 255.0F;
        float g = (color >> 8 & 255) / 255.0F;
        float b = (color & 255) / 255.0F;
        return new Options(r, g, b, 1);
    }
    
    public Options withColorAndScale(float r, float g, float b, float scale) {
        return new Options(r, g, b, scale);
    }
    
    public Options withColorAndScale(int color, float scale) {
        float r = (color >> 16 & 255) / 255.0F;
        float g = (color >> 8 & 255) / 255.0F;
        float b = (color & 255) / 255.0F;
        return new Options(r, g, b, scale);
    }
    
    @Override
    public Codec<Options> codec() {
        return codec;
    }
    
    public class Options implements ParticleOptions {
        public final float r;
        public final float g;
        public final float b;
        public final float scale;
    
        private Options(float r, float g, float b, float scale) {
            this.r = r;
            this.g = g;
            this.b = b;
            this.scale = scale;
        }
        
        private Options(Vector3f vec, float scale) {
            this(vec.x(), vec.y(), vec.z(), scale);
        }
        
        public Vector3f color() {
            return new Vector3f(r, g, b);
        }
        
        public float scale() {
            return scale;
        }
    
        @Override
        public ParticleType<?> getType() {
            return ColorScaleParticleType.this;
        }
        
        @Override
        public void writeToNetwork(FriendlyByteBuf buffer) {
            buffer.writeFloat(r);
            buffer.writeFloat(g);
            buffer.writeFloat(b);
        }
        
        @Override
        public String writeToString() {
            return String.format(Locale.ROOT, "%s %.2f %.2f %.2f %.2f",
                ForgeRegistries.PARTICLE_TYPES.getKey(getType()),
                r, g, b, scale);
        }
        
    }
    
    @SuppressWarnings("deprecation")
    private enum Deserializer implements ParticleOptions.Deserializer<Options> {
        INSTANCE;
        
        @Override
        public Options fromCommand(ParticleType<Options> type, StringReader reader) throws CommandSyntaxException {
            reader.expect(' ');
            float r = reader.readFloat();
            reader.expect(' ');
            float g = reader.readFloat();
            reader.expect(' ');
            float b = reader.readFloat();
            reader.expect(' ');
            float scale = reader.readFloat();
            return ((ColorScaleParticleType)type).new Options(r, g, b, scale);
        }
        
        @Override
        public Options fromNetwork(ParticleType<Options> type, FriendlyByteBuf buffer) {
            return ((ColorScaleParticleType)type).new Options(
                buffer.readFloat(),
                buffer.readFloat(),
                buffer.readFloat(),
                buffer.readFloat()
            );
        }
        
    }
    
}
