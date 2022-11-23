package com.github.channelingmc.visuality.config.entity;

import com.github.channelingmc.visuality.Visuality;
import com.github.channelingmc.visuality.config.ReloadableJsonConfig;
import com.github.channelingmc.visuality.data.Particle;
import com.github.channelingmc.visuality.data.VisualityCodecs;
import com.github.channelingmc.visuality.particle.type.ColorParticleType;
import com.github.channelingmc.visuality.particle.type.VisualityParticleTypes;
import com.google.common.collect.Lists;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.mojang.serialization.Codec;
import com.mojang.serialization.JsonOps;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.util.GsonHelper;
import net.minecraft.util.Mth;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.registries.ForgeRegistries;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Random;

@ApiStatus.Internal
public class EntityArmorParticles extends ReloadableJsonConfig {
    private boolean enabled = true;
    private int interval = 20;
    private List<Entry> entries;
    private final IdentityHashMap<Item, Particle> particles = new IdentityHashMap<>();
    
    public EntityArmorParticles() {
        super(Visuality.loc("particle_emitters/entity/armor"));
        this.entries = createDefaultEntries();
        for (Entry entry : entries) {
            for (Item armor : entry.armors) {
                particles.put(armor, entry.particle);
            }
        }
        MinecraftForge.EVENT_BUS.addListener(this::spawnParticles);
    }
    
    public void spawnParticles(LivingEvent.LivingUpdateEvent event) {
        if (!enabled)
            return;
        
        LivingEntity entity = event.getEntityLiving();
        Level level = entity.level;
        if(!level.isClientSide || !entity.isAlive())
            return;
        
        Random random = entity.getRandom();
        if (random.nextInt(interval) != 0)
            return;
        
        Minecraft minecraft = Minecraft.getInstance();
        EntityRenderer<?> renderer = minecraft.getEntityRenderDispatcher().getRenderer(entity);
        if (!(renderer instanceof RenderLayerParent<?, ?> parent && parent.getModel() instanceof HumanoidModel<?>))
            return;
        
        if (minecraft.cameraEntity == entity && minecraft.options.getCameraType().isFirstPerson())
            return;
        
        double height = random.nextDouble();
        EquipmentSlot slot = switchEquipmentSlotFromHeight(height);
        Item armor = entity.getItemBySlot(slot).getItem();
        if (particles.containsKey(armor)) {
            double x, y, z;
            AABB aabb = entity.getBoundingBox();
            double radian = 2 * Math.PI * random.nextDouble();
            x = Mth.lerp(0.5 + 0.75 * Math.cos(radian), aabb.minX, aabb.maxX);
            y = Mth.lerp(height, aabb.minY, aabb.maxY);
            z = Mth.lerp(0.5 + 0.75 * Math.sin(radian), aabb.minZ, aabb.maxZ);
            particles.get(armor).spawn(level, x, y, z);
        }
    }
    
    private EquipmentSlot switchEquipmentSlotFromHeight(double height) {
        if (height < 3 / 16d) return EquipmentSlot.FEET;
        if (height < 8 / 16d) return EquipmentSlot.LEGS;
        if (height < 13 / 16d) return EquipmentSlot.CHEST;
        return EquipmentSlot.HEAD;
    }
    
    @Override
    @Nullable
    protected JsonObject apply(JsonObject input, boolean config, String source, ProfilerFiller profiler) {
        profiler.push(source);
        if (config) {
            enabled = GsonHelper.getAsBoolean(input, "enabled", true);
            interval = GsonHelper.getAsInt(input, "interval", 20);
        }
        JsonArray array = GsonHelper.getAsJsonArray(input, "entries", null);
        if (array == null){
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
            for (Item armor : entry.armors) {
                particles.put(armor, entry.particle);
            }
        }
        profiler.pop();
        return save ? serializeConfig() : null;
    }
    
    @Override
    protected JsonObject serializeConfig() {
        JsonObject object = new JsonObject();
        object.addProperty("enabled", enabled);
        object.addProperty("interval", interval);
        object.add("entries", Entry.LIST_CODEC.encodeStart(JsonOps.INSTANCE, entries)
            .getOrThrow(true, msg -> logger.error("Failed to serialize config entries: {}", msg)));
        return object;
    }
    
    private record Entry(List<Item> armors, Particle particle) {
    
        private static final Codec<Entry> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            VisualityCodecs.compactListOf(ForgeRegistries.ITEMS.getCodec()).fieldOf("armor")
                .forGetter(Entry::armors),
            Particle.CODEC.fieldOf("particle")
                .forGetter(Entry::particle)
        ).apply(instance, Entry::new));
    
        private static final Codec<List<Entry>> LIST_CODEC = CODEC.listOf();
    
        private static Entry of(List<Item> armors, ParticleOptions particle) {
            return new Entry(armors, Particle.ofZeroVelocity(particle));
        }
        
    }
    
    private static List<Entry> createDefaultEntries() {
        List<Entry> entries = new ArrayList<>();
        ColorParticleType.Options gold = VisualityParticleTypes.SPARKLE.get().withColor(0xFEFFBD);
        entries.add(Entry.of(List.of(
            Items.GOLDEN_HELMET,
            Items.GOLDEN_CHESTPLATE,
            Items.GOLDEN_LEGGINGS,
            Items.GOLDEN_BOOTS
        ), gold));
        ColorParticleType.Options diamond = VisualityParticleTypes.SPARKLE.get().withColor(0xB4FDEE);
        entries.add(Entry.of(List.of(
            Items.DIAMOND_HELMET,
            Items.DIAMOND_CHESTPLATE,
            Items.DIAMOND_LEGGINGS,
           Items.DIAMOND_BOOTS
        ), diamond));
        return entries;
    }
    
}
