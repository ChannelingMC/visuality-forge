package plus.dragons.visuality.config;

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
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.util.GsonHelper;
import net.minecraft.util.RandomSource;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;
import plus.dragons.visuality.Visuality;
import plus.dragons.visuality.data.ParticleWithVelocity;
import plus.dragons.visuality.data.VisualityCodecs;
import plus.dragons.visuality.registry.VisualityParticles;

import java.util.*;

public class BlockAmbientParticleConfig extends ReloadableJsonConfig {
    private boolean enabled = true;
    private int interval = 10;
    private List<Entry> entries;
    private final IdentityHashMap<Block, Pair<Direction[], ParticleWithVelocity>> particles = new IdentityHashMap<>();
    
    public BlockAmbientParticleConfig() {
        super(Visuality.location("particle_emitters/block_ambient"));
        this.entries = createDefaultEntries();
        for (Entry entry : entries) {
            for (Block block : entry.blocks) {
                particles.put(block, Pair.of(entry.directions.toArray(Direction[]::new), entry.particle));
            }
        }
    }
    
    @SuppressWarnings("deprecation")
    public void spawnParticles(BlockState state, Level level, BlockPos pos, RandomSource random) {
        if (!enabled || !level.isAreaLoaded(pos, 1))
            return;
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
            interval = GsonHelper.getAsInt(input, "interval", 10);
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
    
    private record Entry(List<Block> blocks, EnumSet<Direction> directions, ParticleWithVelocity particle) {
        private static final EnumSet<Direction> ALL_DIRECTIONS = EnumSet.allOf(Direction.class);
    
        private static final Codec<Entry> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            VisualityCodecs.compressedListOf(BuiltInRegistries.BLOCK.byNameCodec()).fieldOf("block")
                .forGetter(Entry::blocks),
            Codec.optionalField("direction", VisualityCodecs.compressedSetOf(Direction.CODEC, EnumSet::copyOf))
                .xmap(optional -> optional.orElse(ALL_DIRECTIONS),
                      set -> set.size() == 6 ? Optional.empty() : Optional.of(set))
                .forGetter(Entry::directions),
            ParticleWithVelocity.CODEC.fieldOf("particle")
                .forGetter(Entry::particle)
        ).apply(instance, Entry::new));
        
        private static final Codec<List<Entry>> LIST_CODEC = CODEC.listOf();
        
        private static Entry of(ParticleOptions options, EnumSet<Direction> directions, Block... blocks) {
            return new Entry(List.of(blocks), directions, ParticleWithVelocity.ofZeroVelocity(options));
        }
    
        private static Entry of(ParticleOptions options, Block... blocks) {
            return new Entry(List.of(blocks), ALL_DIRECTIONS, ParticleWithVelocity.ofZeroVelocity(options));
        }
    
        private static Entry of(ParticleOptions options, Direction direction, Block... blocks) {
            return new Entry(List.of(blocks), EnumSet.of(direction), ParticleWithVelocity.ofZeroVelocity(options));
        }
        
    }
    
    private static List<Entry> createDefaultEntries() {
        List<Entry> entries = new ArrayList<>();
        
        entries.add(Entry.of(VisualityParticles.SPARKLE.get().withColor(0xFEFFBD),
            Blocks.GOLD_ORE,
            Blocks.DEEPSLATE_GOLD_ORE,
            Blocks.NETHER_GOLD_ORE));
        
        entries.add(Entry.of(VisualityParticles.SPARKLE.get().withColor(0xB4FDEE),
            Blocks.DIAMOND_ORE,
            Blocks.DEEPSLATE_DIAMOND_ORE));
        
        entries.add(Entry.of(VisualityParticles.SPARKLE.get().withColor(0xD9FFEB),
            Blocks.EMERALD_ORE,
            Blocks.DEEPSLATE_EMERALD_ORE));
        
        entries.add(Entry.of(VisualityParticles.SPARKLE.get().withColor(0xFECBE6),
            Blocks.AMETHYST_CLUSTER));
        
        entries.add(Entry.of(VisualityParticles.SOUL.get(), Direction.UP,
            Blocks.SOUL_SAND,
            Blocks.SOUL_SOIL));
        
        return entries;
    }
    
}
