package plus.dragons.visuality.config;

import net.minecraftforge.client.event.RegisterClientReloadListenersEvent;
import net.minecraftforge.common.ForgeConfigSpec;

public class Config {
	public static EntityHitParticleConfig ENTITY_HIT_PARTICLES;
	public static EntityArmorParticleConfig ENTITY_ARMOR_PARTICLES;
	public static BlockAmbientParticleConfig BLOCK_AMBIENT_PARTICLES;
	public static BlockStepParticleConfig BLOCK_STEP_PARTICLES;
	
    public static final ForgeConfigSpec.BooleanValue SLIME_ENABLED;
    public static final ForgeConfigSpec.BooleanValue CHARGE_ENABLED;

    public static final ForgeConfigSpec.BooleanValue WATER_CIRCLE_ENABLED;
    public static final ForgeConfigSpec.BooleanValue WATER_CIRCLE_COLORED;
    public static final ForgeConfigSpec.IntValue WATER_CIRCLE_DENSITY;

    public static final ForgeConfigSpec SPEC;
	
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
		
		SPEC = builder.build();
	}
	
	public static void registerClientResourceListener(RegisterClientReloadListenersEvent event) {
	    event.registerReloadListener(ENTITY_HIT_PARTICLES = new EntityHitParticleConfig());
	    event.registerReloadListener(ENTITY_ARMOR_PARTICLES = new EntityArmorParticleConfig());
	    event.registerReloadListener(BLOCK_AMBIENT_PARTICLES = new BlockAmbientParticleConfig());
	    event.registerReloadListener(BLOCK_STEP_PARTICLES = new BlockStepParticleConfig());
	}
	
}
