package com.github.channelingmc.visuality.config;

import net.minecraftforge.common.ForgeConfigSpec;

public class ClientConfig {

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

}
