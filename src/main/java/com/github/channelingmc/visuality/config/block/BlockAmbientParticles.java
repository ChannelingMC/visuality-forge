package com.github.channelingmc.visuality.config.block;

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
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import com.mojang.serialization.JsonOps;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.util.GsonHelper;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.registries.ForgeRegistries;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;

import java.util.*;

@ApiStatus.Internal
public class BlockAmbientParticles extends ReloadableJsonConfig {
    static final EnumSet<Direction> ALL_DIRECTIONS = EnumSet.allOf(Direction.class);
    private boolean enabled = true;
    private int interval = 10;
    private List<Entry> entries;
    private final IdentityHashMap<Block, Pair<Direction[], Particle>> particles = new IdentityHashMap<>();
    
    public BlockAmbientParticles() {
        super(Visuality.loc("particle_emitters/block/ambient"));
        this.entries = createDefaultEntries();
        for (Entry entry : entries) {
            for (Block block : entry.blocks) {
                particles.put(block, Pair.of(entry.directions.toArray(Direction[]::new), entry.particle));
            }
        }
    }
    
    @SuppressWarnings("deprecation")
    public void spawnParticles(BlockState state, Level level, BlockPos pos, Random random) {
        if (!enabled || !level.isAreaLoaded(pos, 1))
            return;
        var keys = particles.keySet();
        Block block = state.getBlock();
        if (!particles.containsKey(block))
            return;
        
        var entry = particles.get(block);
        var directions = entry.getFirst();
        int i = random.nextInt(interval);
        if (i >= directions.length)
            return;
        boolean fullBlock = state.isSolidRender(level, pos);
        if (fullBlock) {
            Direction direction = directions[i];
            BlockPos facePos = pos.relative(direction);
            if (level.getBlockState(facePos).isSolidRender(level, facePos))
                return;
            Direction.Axis axis = direction.getAxis();
            double x = pos.getX() + (axis == Direction.Axis.X ? 0.5 + 0.5625 * direction.getStepX() : random.nextDouble());
            double y = pos.getY() + (axis == Direction.Axis.Y ? 0.5 + 0.5625 * direction.getStepY() : random.nextDouble());
            double z = pos.getZ() + (axis == Direction.Axis.Z ? 0.5 + 0.5625 * direction.getStepZ() : random.nextDouble());
            entry.getSecond().spawn(level, x, y, z);
        } else {
            double x = pos.getX() + random.nextDouble();
            double y = pos.getY() + random.nextDouble();
            double z = pos.getZ() + random.nextDouble();
            entry.getSecond().spawn(level, x, y, z);
        }
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
            var result = data.result().filter(entry -> !entry.directions.isEmpty());
            if (result.isPresent()) {
                newEntries.add(result.get());
            } else {
                logger.warn("Error parsing {} from {}: Directions must not be empty", id, source);
                save = config;
            }
        }
        if (config) {
            entries = newEntries;
            particles.clear();
        }
        for (Entry entry : newEntries) {
            for (Block block : entry.blocks) {
                particles.put(block, Pair.of(entry.directions.toArray(Direction[]::new), entry.particle));
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
    
    private record Entry(List<Block> blocks, Set<Direction> directions, Particle particle) {
    
        private static final Codec<Entry> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            VisualityCodecs.compactListOf(ForgeRegistries.BLOCKS.getCodec()).fieldOf("block")
                .forGetter(Entry::blocks),
            Codec.optionalField("direction", VisualityCodecs.compactSetOf(Direction.CODEC))
                .xmap(
                    optional -> optional.orElse(ALL_DIRECTIONS),
                    set -> set.size() == 6 ? Optional.empty() : Optional.of(set)
                ).forGetter(Entry::directions),
            Particle.CODEC.fieldOf("particle")
                .forGetter(Entry::particle)
        ).apply(instance, Entry::new));
    
        private static final Codec<List<Entry>> LIST_CODEC = CODEC.listOf();
    
        private static Entry of(Block block, Set<Direction> directions, ParticleOptions particle) {
            return new Entry(List.of(block), ALL_DIRECTIONS, Particle.ofZeroVelocity(particle));
        }
        
        static Entry of(List<Block> blocks, Set<Direction> directions, ParticleOptions particle) {
            return new Entry(blocks, directions, Particle.ofZeroVelocity(particle));
        }
        
    }
    
    private static List<Entry> createDefaultEntries() {
        List<Entry> entries = new ArrayList<>();
        ColorParticleType.Options gold = VisualityParticleTypes.SPARKLE.get().withColor(0xFEFFBD);
        entries.add(Entry.of(List.of(
            Blocks.GOLD_ORE, Blocks.DEEPSLATE_GOLD_ORE, Blocks.NETHER_GOLD_ORE
        ), ALL_DIRECTIONS, gold));
        ColorParticleType.Options diamond = VisualityParticleTypes.SPARKLE.get().withColor(0xB4FDEE);
        entries.add(Entry.of(List.of(
            Blocks.DIAMOND_BLOCK, Blocks.DEEPSLATE_DIAMOND_ORE
        ), ALL_DIRECTIONS, diamond));
        ColorParticleType.Options emerald = VisualityParticleTypes.SPARKLE.get().withColor(0xD9FFEB);
        entries.add(Entry.of(List.of(
            Blocks.EMERALD_ORE, Blocks.DEEPSLATE_EMERALD_ORE
        ), ALL_DIRECTIONS, emerald));
        ColorParticleType.Options amethyst = VisualityParticleTypes.SPARKLE.get().withColor(0xFECBE6);
        entries.add(Entry.of(Blocks.AMETHYST_CLUSTER, ALL_DIRECTIONS, amethyst));
        SimpleParticleType soul = VisualityParticleTypes.SOUL.get();
        entries.add(Entry.of(List.of(
            Blocks.SOUL_SAND, Blocks.SOUL_SOIL
        ), Set.of(Direction.UP), soul));
        return entries;
    }
    
}
