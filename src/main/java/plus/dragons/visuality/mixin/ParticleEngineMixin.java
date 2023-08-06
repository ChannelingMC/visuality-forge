package plus.dragons.visuality.mixin;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.client.particle.ParticleEngine;
import net.minecraft.client.particle.ParticleProvider;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.registries.RegistryObject;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import plus.dragons.visuality.particle.VisualityParticleEngine;
import plus.dragons.visuality.registry.VisualityRegistries;

import java.util.Map;

@Mixin(ParticleEngine.class)
public class ParticleEngineMixin implements VisualityParticleEngine {
    
    @Shadow @Final private Map<ResourceLocation, ParticleProvider<?>> providers;
    
    @Shadow @Final private Map<ResourceLocation, ParticleEngine.MutableSpriteSet> spriteSets;
    
    @Override
    public <O extends ParticleOptions, T extends ParticleType<O>> void registerVisuality(RegistryObject<T> type, ParticleProvider<O> provider) {
        this.providers.put(type.getId(), provider);
    }
    
    @Override
    public <O extends ParticleOptions, T extends ParticleType<O>> void registerVisuality(RegistryObject<T> type, ParticleEngine.SpriteParticleRegistration<O> registration) {
        ParticleEngine.MutableSpriteSet spriteSet = new ParticleEngine.MutableSpriteSet();
        this.spriteSets.put(type.getId(), spriteSet);
        this.providers.put(type.getId(), registration.create(spriteSet));
    }
    
    @Nullable
    @ModifyExpressionValue(method = "makeParticle", at = @At(value = "INVOKE", target = "Lnet/minecraft/core/Registry;getKey(Ljava/lang/Object;)Lnet/minecraft/resources/ResourceLocation;"))
    private ResourceLocation visuality$particleAlias(@Nullable ResourceLocation original, @Local(ordinal = 0, argsOnly = true) ParticleOptions options) {
        if(VisualityRegistries.PARTICLE_TYPES!=null && VisualityRegistries.PARTICLE_TYPES.get()!=null){
            return original == null ? VisualityRegistries.PARTICLE_TYPES.get().getKey(options.getType()) : original;
        }
        return original;
    }
    
}
