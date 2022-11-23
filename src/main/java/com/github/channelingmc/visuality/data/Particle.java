package com.github.channelingmc.visuality.data;

import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.Dynamic;
import com.mojang.serialization.DynamicOps;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.util.Mth;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

public record Particle(ParticleOptions options, Vec3 velocity) {
    
    public static final Codec<Particle> CODEC =
        Codec.of(Particle::encode, Particle::decode);
    
    public static Particle ofZeroVelocity(ParticleOptions particle) {
        return new Particle(particle, Vec3.ZERO);
    }
    
    public boolean isZeroVelocity() {
        return Mth.equal(velocity.x, 0) &&
               Mth.equal(velocity.y, 0) &&
               Mth.equal(velocity.z, 0);
    }
    
    public void spawn(Level level, double x, double y, double z) {
        level.addParticle(options, x, y, z, velocity.x, velocity.y, velocity.z);
    }
    
    public static <T> DataResult<Pair<Particle, T>> decode(DynamicOps<T> ops, T input) {
        final Dynamic<T> dynamic = new Dynamic<>(ops, input);
        final DataResult<Particle> result = dynamic.getElement("particle")
            .flatMap(t -> VisualityCodecs.PARTICLE.parse(ops, t))
            .apply2(Particle::new, dynamic.getElement("velocity").flatMap(t -> VisualityCodecs.VEC3.parse(ops, t)));
        if (result.error().isEmpty())
            return result.map(pwv -> Pair.of(pwv, input));
        else
            return VisualityCodecs.PARTICLE.parse(dynamic)
                .map(Particle::ofZeroVelocity)
                .map(pwv -> Pair.of(pwv, input));
    }
    
    public static <T> DataResult<T> encode(Particle input, DynamicOps<T> ops, T prefix) {
        return input.isZeroVelocity()
            ? VisualityCodecs.PARTICLE.encode(input.options, ops, prefix)
            : ops.mapBuilder()
                .add("particle", VisualityCodecs.PARTICLE.encodeStart(ops, input.options))
                .add("velocity", VisualityCodecs.VEC3.encodeStart(ops, input.velocity))
                .build(prefix);
    }
    
}
