package com.github.channelingmc.visuality.config.entity;

import com.github.channelingmc.visuality.Visuality;
import com.github.channelingmc.visuality.config.ReloadableJsonConfig;
import com.github.channelingmc.visuality.data.Particle;
import com.github.channelingmc.visuality.data.VisualityCodecs;
import com.github.channelingmc.visuality.particle.type.VisualityParticleTypes;
import com.google.common.collect.Lists;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.mojang.serialization.Codec;
import com.mojang.serialization.JsonOps;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.util.GsonHelper;
import net.minecraft.util.Mth;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.living.LivingAttackEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.registries.ForgeRegistries;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.IdentityHashMap;
import java.util.List;

@ApiStatus.Internal
public class EntityHitParticles extends ReloadableJsonConfig {
    private boolean enabled = true;
    private int minAmount = 1;
    private int maxAmount = 20;
    private List<Entry> entries;
    private final IdentityHashMap<EntityType<?>, Particle> particles = new IdentityHashMap<>();
    
    public EntityHitParticles() {
        super(Visuality.loc("entity/hit"));
        this.entries = createDefaultEntries();
        for (Entry entry : entries) {
            for (EntityType<?> type : entry.entities) {
                particles.put(type, entry.particle);
            }
        }
        MinecraftForge.EVENT_BUS.addListener(EventPriority.LOWEST, true, this::spawnHitParticles);
    }
    
    public void spawnHitParticles(LivingAttackEvent event) {
        if (!this.enabled)
            return;
        
        DamageSource source = event.getSource();
        if (!(source.getEntity() instanceof AbstractClientPlayer attacker))
            return;
        
        LivingEntity entity = event.getEntity();
        EntityType<?> type = entity.getType();
        if (!particles.containsKey(type) ||
            entity.isInvulnerableTo(source) ||
            entity.isDeadOrDying() ||
            source.isFire() && entity.hasEffect(MobEffects.FIRE_RESISTANCE))
            return;
        
        var modifiers = attacker.getMainHandItem()
            .getAttributeModifiers(EquipmentSlot.MAINHAND)
            .get(Attributes.ATTACK_DAMAGE);
        double addition = 0;
        double multiplyBase = 1;
        double multiplyTotal = 1;
        for (var modifier : modifiers) {
            switch (modifier.getOperation()) {
                case ADDITION -> addition += modifier.getAmount();
                case MULTIPLY_BASE -> multiplyBase += modifier.getAmount();
                case MULTIPLY_TOTAL -> multiplyTotal *= (1 + modifier.getAmount());
            }
        }
        double amount = event.getAmount() * (1 + addition) * multiplyBase * multiplyTotal;
        if (amount <= 0)
            return;
        
        Particle particle = particles.get(entity.getType());
        int count = Mth.clamp((int) Math.sqrt(amount), minAmount, maxAmount);
        double x = entity.getX();
        double y = entity.getY(0.5);
        double z = entity.getZ();
        for (int i = 0; i < count; ++i) {
            particle.spawn(entity.level, x, y, z);
        }
    }
    
    @Override
    @Nullable
    protected JsonObject apply(JsonObject input, boolean config, String source, ProfilerFiller profiler) {
        profiler.push(source);
        if (config) {
            enabled = GsonHelper.getAsBoolean(input, "enabled", true);
            minAmount = GsonHelper.getAsInt(input, "min_amount", 1);
            maxAmount = GsonHelper.getAsInt(input, "max_amount", 20);
        }
        JsonArray array = GsonHelper.getAsJsonArray(input, "entries", null);
        if (array == null) {
            logger.warn("Failed to load options entries from {}: Missing JsonArray 'entries'.", source);
            profiler.pop();
            return config ? serializeConfig() : null;
        }
        boolean save = false;
        List<Entry> newEntries = new ArrayList<>();
        List<JsonElement> elements = Lists.newArrayList(array);
        for (JsonElement element : elements) {
            var data = Entry.CODEC.parse(JsonOps.INSTANCE, element);
            if (data.error().isPresent()) {
                save = config;
                logger.warn("Error parsing {} from {}: {}", id, source, data.error().get().message());
                continue;
            }
            if (data.result().isPresent())
                newEntries.add(data.result().get());
            else {
                save = config;
                logger.warn("Error parsing {} from {}: Missing decode result", id, source);
            }
        }
        if (config) {
            entries = newEntries;
            particles.clear();
        }
        for (Entry entry : newEntries) {
            for (EntityType<?> type : entry.entities) {
                particles.put(type, entry.particle);
            }
        }
        profiler.pop();
        return save ? serializeConfig() : null;
    }
    
    @Override
    protected JsonObject serializeConfig() {
        JsonObject object = new JsonObject();
        object.addProperty("enabled", enabled);
        object.addProperty("min_amount", minAmount);
        object.addProperty("max_amount", maxAmount);
        object.add("entries", Entry.LIST_CODEC.encodeStart(JsonOps.INSTANCE, entries)
            .getOrThrow(true, msg -> logger.error("Failed to serialize config entries: {}", msg)));
        return object;
    }
    
    private record Entry(List<EntityType<?>> entities, Particle particle) {
    
        private static final Codec<Entry> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            VisualityCodecs.compactListOf(ForgeRegistries.ENTITY_TYPES.getCodec()).fieldOf("entity")
                .forGetter(Entry::entities),
            Particle.CODEC.fieldOf("options")
                .forGetter(Entry::particle)
        ).apply(instance, Entry::new));
    
        private static final Codec<List<Entry>> LIST_CODEC = CODEC.listOf();
    
        private static Entry of(List<EntityType<?>> types, ParticleOptions particle) {
            return new Entry(types, Particle.ofZeroVelocity(particle));
        }
    
        private static Entry of(EntityType<?> type, ParticleOptions particle) {
            return new Entry(List.of(type), Particle.ofZeroVelocity(particle));
        }
        
    }
    
    private static List<Entry> createDefaultEntries() {
        List<Entry> entries = new ArrayList<>();
        entries.add(Entry.of(List.of(
            EntityType.SKELETON,
            EntityType.SKELETON_HORSE,
            EntityType.STRAY
        ), VisualityParticleTypes.BONE.get()));
        entries.add(Entry.of(EntityType.WITHER_SKELETON, VisualityParticleTypes.WITHER_BONE.get()));
        entries.add(Entry.of(EntityType.CHICKEN, VisualityParticleTypes.FEATHER.get()));
        entries.add(Entry.of(List.of(
            EntityType.VILLAGER,
            EntityType.WANDERING_TRADER
        ), VisualityParticleTypes.EMERALD.get()));
        return entries;
    }
    
}
