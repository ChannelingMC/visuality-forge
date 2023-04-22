package plus.dragons.visuality.data;

import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.Dynamic;
import com.mojang.serialization.DynamicOps;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.util.Mth;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

public record ParticleWithVelocity(ParticleOptions options, Vec3 velocity) {
    
    public static final Codec<ParticleWithVelocity> CODEC =
        Codec.of(ParticleWithVelocity::encode, ParticleWithVelocity::decode);
    
    public static ParticleWithVelocity ofZeroVelocity(ParticleOptions particle) {
        return new ParticleWithVelocity(particle, Vec3.ZERO);
    }
    
    public boolean isZeroVelocity() {
        return Mth.equal(velocity.x, 0) &&
               Mth.equal(velocity.y, 0) &&
               Mth.equal(velocity.z, 0);
    }
    
    public void spawn(Level level, double x, double y, double z) {
        level.addParticle(options, x, y, z, velocity.x, velocity.y, velocity.z);
    }
    
    public static <T> DataResult<Pair<ParticleWithVelocity, T>> decode(DynamicOps<T> ops, T input) {
        final Dynamic<T> dynamic = new Dynamic<>(ops, input);
        final DataResult<ParticleWithVelocity> result = dynamic.getElement("particle")
            .flatMap(t -> VisualityCodecs.PARTICLE_OPTIONS.parse(ops, t))
            .apply2(ParticleWithVelocity::new, dynamic.getElement("velocity").flatMap(t -> Vec3.CODEC.parse(ops, t)));
        if (result.error().isEmpty())
            return result.map(pwv -> Pair.of(pwv, input));
        else
            return VisualityCodecs.PARTICLE_OPTIONS.parse(dynamic)
                .map(ParticleWithVelocity::ofZeroVelocity)
                .map(pwv -> Pair.of(pwv, input));
    }
    
    public static <T> DataResult<T> encode(ParticleWithVelocity input, DynamicOps<T> ops, T prefix) {
        return input.isZeroVelocity()
            ? VisualityCodecs.PARTICLE_OPTIONS.encode(input.options, ops, prefix)
            : ops.mapBuilder()
                .add("particle", VisualityCodecs.PARTICLE_OPTIONS.encodeStart(ops, input.options))
                .add("velocity", Vec3.CODEC.encodeStart(ops, input.velocity))
                .build(prefix);
    }
    
}
