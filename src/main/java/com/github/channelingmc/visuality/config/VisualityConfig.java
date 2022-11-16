package com.github.channelingmc.visuality.config;

import com.mojang.logging.LogUtils;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.registries.ForgeRegistries;
import org.slf4j.Logger;

import java.util.ArrayList;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;

public class VisualityConfig {

    public static final ArrayList<String> DEFAULT_HIT_PARTICLES = new ArrayList<>(List.of(
		"minecraft:skeleton=visuality:bone",
	    "minecraft:skeleton_horse=visuality:bone",
	    "minecraft:stray=visuality:bone",
	    "minecraft:wither_skeleton=visuality:wither_bone",
	    "minecraft:chicken=visuality:feather",
	    "minecraft:villager=visuality:emerald"
    ));

    public static final ArrayList<String> DEFAULT_SHINY_ARMOR = new ArrayList<>(List.of(
		"minecraft:golden_helmet",
	    "minecraft:golden_chestplate",
	    "minecraft:golden_leggings",
	    "minecraft:golden_boots",
	    "minecraft:diamond_helmet",
	    "minecraft:diamond_chestplate",
	    "minecraft:diamond_leggings",
	    "minecraft:diamond_boots"
    ));

    public static final ArrayList<String> DEFAULT_SHINY_BLOCKS = new ArrayList<>(List.of(
		"minecraft:gold_ore",
	    "minecraft:deepslate_gold_ore",
	    "minecraft:nether_gold_ore",
	    "minecraft:diamond_ore",
	    "minecraft:deepslate_diamond_ore",
	    "minecraft:emerald_ore",
	    "minecraft:deepslate_emerald_ore"
    ));

    public static final ForgeConfigSpec.BooleanValue SLIME_ENABLED;
    public static final ForgeConfigSpec.BooleanValue CHARGE_ENABLED;
    public static final ForgeConfigSpec.BooleanValue SPARKLE_ENABLED;
    public static final ForgeConfigSpec.BooleanValue SOUL_ENABLED;

    public static final ForgeConfigSpec.BooleanValue WATER_CIRCLE_ENABLED;
    public static final ForgeConfigSpec.BooleanValue WATER_CIRCLE_COLORED;
    public static final ForgeConfigSpec.IntValue WATER_CIRCLE_DENSITY;

    public static final ForgeConfigSpec.BooleanValue HIT_PARTICLES_ENABLED;
    private static final ForgeConfigSpec.ConfigValue<List<? extends String>> HIT_PARTICLE_ENTRIES;
	public static final Map<EntityType<?>, ParticleOptions> HIT_PARTICLE_REGISTRY = new IdentityHashMap<>();

    public static final ForgeConfigSpec.BooleanValue SHINY_ARMOR_ENABLED;
    private static final ForgeConfigSpec.ConfigValue<List<? extends String>> SHINY_ARMOR_ENTRIES;
	public static final List<Item> SHINY_ARMOR_REGISTRY = new ArrayList<>();

    public static final ForgeConfigSpec.BooleanValue SHINY_BLOCK_ENABLED;
    private static final ForgeConfigSpec.ConfigValue<List<? extends String>> SHINY_BLOCK_ENTRIES;
	public static final List<Block> SHINY_BLOCK_REGISTRY = new ArrayList<>();

    public static final ForgeConfigSpec SPEC;
	
	private static final Logger LOGGER = LogUtils.getLogger();
	
	private static boolean validRL(Object obj) {
		return obj instanceof String s && ResourceLocation.isValidResourceLocation(s);
	}
	
	private static boolean validRLPair(Object obj) {
		if (obj instanceof String s) {
			String[] pair = s.replace(" ", "").split("=");
			return pair.length == 2 && validRL(pair[0]) && validRL(pair[1]);
		}
		return false;
	}
	
	private static void reloadHitParticles() {
		HIT_PARTICLE_REGISTRY.clear();
		for (String entry : HIT_PARTICLE_ENTRIES.get()) {
			try {
				String[] pair = entry.replace(" ", "").split("=");
				if (pair.length != 2)
					throw new RuntimeException("Unexpected input for format: \"key = value\"");
				EntityType<?> entityType = ForgeRegistries.ENTITY_TYPES.getValue(new ResourceLocation(pair[0]));
				if (entityType == null)
					throw new RuntimeException("Entity Type " + pair[0] + " unregistered");
				ParticleType<?> particleType = ForgeRegistries.PARTICLE_TYPES.getValue(new ResourceLocation(pair[1]));
				if (particleType instanceof ParticleOptions particleOptions) {
					HIT_PARTICLE_REGISTRY.put(entityType, particleOptions);
				} else if (particleType == null) {
					throw new RuntimeException("Particle " + pair[1] + " unregistered");
				} else {
					throw new RuntimeException("Complex Particle type is unsupported");
				}
			} catch (Throwable t) {
				LOGGER.error("Error parsing config entry [" + entry + "] for Hit Particles: ", t);
			}
		}
	}
	
	private static void reloadShinyArmor() {
		SHINY_ARMOR_REGISTRY.clear();
		for (String entry : SHINY_ARMOR_ENTRIES.get()) {
			try {
				Item item = ForgeRegistries.ITEMS.getValue(new ResourceLocation(entry));
				if (item == null)
					throw new RuntimeException("Item " + entry + " unregistered");
				SHINY_ARMOR_REGISTRY.add(item);
			} catch (Throwable t) {
				LOGGER.error("Error parsing config entry [" + entry + "] for Shiny Armor: ", t);
			}
		}
	}
	
	private static void reloadShinyBlocks() {
		SHINY_BLOCK_REGISTRY.clear();
		for (String entry : SHINY_BLOCK_ENTRIES.get()) {
			try {
				Block block = ForgeRegistries.BLOCKS.getValue(new ResourceLocation(entry));
				if (block == null)
					throw new RuntimeException("Block " + entry + " unregistered");
				SHINY_BLOCK_REGISTRY.add(block);
			} catch (Throwable t) {
				LOGGER.error("Error parsing config entry [" + entry + "] for Shiny Blocks: ", t);
			}
		}
	}
	
	public static void reload() {
		reloadHitParticles();
		reloadShinyArmor();
		reloadShinyBlocks();
		LOGGER.info("Reloaded config for Visuality");
	}
	
	static {
		ForgeConfigSpec.Builder builder = new ForgeConfigSpec.Builder();
		
		builder.push("slime");
		SLIME_ENABLED = builder
			.translation("config.visuality.option.slime")
			.comment("Slime Blobs Enabled")
			.define("enabled", true);
		builder.pop();
		
		builder.push("charge");
		CHARGE_ENABLED = builder
			.translation("config.visuality.option.charge")
			.comment("Charge Particle Enabled")
			.define("enabled", true);
		builder.pop();
		
		builder.push("sparkle");
		SPARKLE_ENABLED = builder
			.translation("config.visuality.option.sparkle")
			.comment("Sparkle Particle Enabled")
			.define("enabled", true);
		builder.pop();
		
		builder.push("soul");
		SOUL_ENABLED = builder
			.translation("config.visuality.option.soul")
			.comment("Soul Particle Enabled")
			.define("enabled", true);
		builder.pop();
		
		builder.push("waterCircle");
		WATER_CIRCLE_ENABLED = builder
			.translation("config.visuality.option.waterCircle")
			.comment("Water Circles Enabled")
			.define("enabled", true);
		WATER_CIRCLE_COLORED = builder
			.translation("config.visuality.option.waterCircle.colored")
			.comment("Water Circles Colored")
			.define("colored", true);
		WATER_CIRCLE_DENSITY = builder
			.translation("config.visuality.option.waterCircle.density")
			.comment("Water Circles Density")
			.defineInRange("density", 16, 0, 64);
		builder.pop();
		
		builder.push("hitParticles");
		HIT_PARTICLES_ENABLED = builder
			.translation("config.visuality.option.hitParticles")
			.comment("Hit Particles Enabled")
			.define("enabled", true);
		HIT_PARTICLE_ENTRIES = builder
			.translation("config.visuality.option.hitParticles.entries")
			.comment("Hit Particles Entries")
			.defineList("entries", () -> DEFAULT_HIT_PARTICLES, VisualityConfig::validRLPair);
		builder.pop();
		
		builder.push("shinyArmor");
		SHINY_ARMOR_ENABLED = builder
			.translation("config.visuality.option.shinyArmor")
			.comment("Shiny Armor Enabled")
			.define("enabled", true);
		SHINY_ARMOR_ENTRIES = builder
			.translation("config.visuality.option.shinyArmor.entries")
			.comment("Shiny Armor Entries")
			.defineList("entries", () -> DEFAULT_SHINY_ARMOR, VisualityConfig::validRL);
		builder.pop();
		
		builder.push("shinyBlocks");
		SHINY_BLOCK_ENABLED = builder
			.translation("config.visuality.option.shinyBlocks")
			.comment("Shiny Blocks Enabled")
			.define("enabled", true);
		SHINY_BLOCK_ENTRIES = builder
			.translation("config.visuality.option.shinyBlocks.entries")
			.comment("Shiny Blocks Entries")
			.defineList("entries", () -> DEFAULT_SHINY_BLOCKS, VisualityConfig::validRL);
		builder.pop();
		
		SPEC = builder.build();
	}

}
