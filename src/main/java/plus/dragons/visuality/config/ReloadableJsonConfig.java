package plus.dragons.visuality.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.JsonOps;
import net.minecraft.Util;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimplePreparableReloadListener;
import net.minecraft.util.GsonHelper;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraftforge.common.crafting.CraftingHelper;
import net.minecraftforge.common.crafting.conditions.ICondition;
import net.minecraftforge.fml.loading.FMLPaths;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.Reader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * A Json-based config which can be load from config and resource packs
 */
public abstract class ReloadableJsonConfig extends SimplePreparableReloadListener<List<Pair<String, JsonObject>>> {
    private static final Map<ResourceLocation, ReloadableJsonConfig> CONFIGS = new HashMap<>();
    protected static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    protected final ResourceLocation id;
    protected final Path path;
    protected final Logger logger;
    @Nullable
    private JsonObject config;
    
    protected ReloadableJsonConfig(ResourceLocation id) {
        this.id = new ResourceLocation(id.getNamespace(), id.getPath() + ".json");
        this.path = FMLPaths.CONFIGDIR.get().resolve(this.id.getNamespace()).resolve(this.id.getPath());
        this.logger = LoggerFactory.getLogger(this.getClass());
        CONFIGS.put(id, this);
    }
    
    protected List<Pair<String, JsonObject>> prepare(ResourceManager resourceManager, ProfilerFiller profiler) {
        profiler.startTick();
        profiler.push("config");
        profiler.push("parse");
        config = loadConfig();
        profiler.pop();
        profiler.pop();
        List<Pair<String, JsonObject>> list = new ArrayList<>();
        try {
            for(String namespace : resourceManager.getNamespaces()) {
                profiler.push(namespace);
                ResourceLocation id = new ResourceLocation(namespace, this.id.getPath());
                for (Resource resource : resourceManager.getResourceStack(id)) {
                    profiler.push(resource.sourcePackId());
                    try {
                        Reader reader = resource.openAsReader();
                        profiler.push("parse");
                        JsonObject object = GsonHelper.fromJson(GSON, reader, JsonObject.class);
                        profiler.pop();
                        list.add(Pair.of(resource.sourcePackId() + '#' + id, object));
                    } catch (RuntimeException exception) {
                        logger.warn("Invalid {} in resourcepack: '{}'", id, resource.sourcePackId(), exception);
                    }
                    profiler.pop();
                }
                profiler.pop();
            }
        } catch (IOException ignored) {
        }
        profiler.endTick();
        return list;
    }
    
    protected void apply(List<Pair<String, JsonObject>> list, ResourceManager resourceManager, ProfilerFiller profiler) {
        profiler.startTick();
        config = config == null ? serializeConfig() : apply(config, true, path.toString(), profiler);
        for (var entry : list) {
            String name = entry.getFirst();
            JsonObject object = entry.getSecond();
            if (!processConditions(object, "conditions", ICondition.IContext.EMPTY)) {
                logger.debug("Skipping loading {} from {} as it's conditions were not met", id, name);
                continue;
            }
            apply(object, false, name, profiler);
        }
        if (config != null) {
            profiler.push("save");
            saveConfig(config);
            //config = null;
            profiler.pop();
        }
        profiler.pop();
        profiler.endTick();
    }

    protected boolean processConditions(JsonObject json, String item, ICondition.IContext context){
        if(json.has(item)){
            var condition = Util.getOrThrow(ICondition.SAFE_CODEC.parse(JsonOps.INSTANCE, json.getAsJsonObject(item)), JsonParseException::new);
            return condition.test(context);
        }
        return false;
    }
    
    /**
     * Deserialize the config JsonObject and refresh the config data
     * @param input the JsonObject read from file in {@link ReloadableJsonConfig#loadConfig()}
     * @param config if the JsonObject is from config
     * @param source a String to identify the source of the JsonObject
     * @return the corrected JsonElement to save to file, or null if it doesn't need correction
     */
    @Nullable
    protected abstract JsonObject apply(JsonObject input, boolean config, String source, ProfilerFiller profiler);
    
    /**
     * Serialize the config data into JsonElement for saving
     * @return the serialized config data
     */
    protected abstract JsonObject serializeConfig();
    
    /**
     * Load the config JsonElement from file
     * @return the raw JsonElement, or null if failed to load
     */
    @Nullable
    protected JsonObject loadConfig() {
        if (!Files.exists(path)) {
            return null;
        }
        try (BufferedReader reader = Files.newBufferedReader(path)) {
            return GSON.fromJson(reader, JsonObject.class);
        } catch (Throwable throwable) {
            logger.warn("Failed to read config from {}", path, throwable);
            return null;
        }
    }
    
    /**
     * Save the config to file
     * @param output the config JsonElement to save
     */
    private void saveConfig(JsonObject output) {
        try {
            Files.createDirectories(path.getParent());
            BufferedWriter writer = Files.newBufferedWriter(path);
            GSON.toJson(output, writer);
            writer.close();
            logger.info("Saved config to {}", path);
        } catch (Throwable throwable) {
            logger.error("Failed to save config to {}", path, throwable);
        }
    }

}
