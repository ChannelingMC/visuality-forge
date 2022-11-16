package com.github.channelingmc.visuality.particle;

import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Quaternion;
import com.mojang.math.Vector3f;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.ParticleStatus;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.*;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.util.Mth;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.phys.Vec3;
import com.github.channelingmc.visuality.registry.VisualityParticles;

import static com.github.channelingmc.visuality.config.VisualityConfig.*;
import static com.github.channelingmc.visuality.config.VisualityConfig.WATER_CIRCLE_COLORED;

public class WaterCircleParticle extends TextureSheetParticle {
    private final SpriteSet sprites;
    private static final Quaternion QUATERNION = new Quaternion(0F, -0.7F, 0.7F, 0F);

    private WaterCircleParticle(ClientLevel level, double x, double y, double z, double color, SpriteSet sprites) {
        super(level, x, y, z, 0, 0, 0);
        this.lifetime = 5 + this.random.nextInt(3);
        this.setParticleSpeed(0D, 0D, 0D);
        if(color != 0) this.setColor((int) color);
        this.scale(2F + (float) this.random.nextInt(11) / 10);
        this.sprites = sprites;
        this.setSpriteFromAge(sprites);
    }

    public void setColor(int rgbHex) {
        float red = (float) ((rgbHex & 16711680) >> 16) / 255.0F;
        float green = (float) ((rgbHex & '\uff00') >> 8) / 255.0F;
        float blue = (float) ((rgbHex & 255)) / 255.0F;
        this.setColor(red, green, blue);
    }

    @Override
    public void tick() {
        if(this.age > this.lifetime / 2) {
            this.setAlpha(1.0F - ((float) this.age - (float) (this.lifetime / 2)) / (float) this.lifetime);
        }
        if(this.age++ >= this.lifetime) {
            this.remove();
        }
        else {
            this.setSpriteFromAge(sprites);
        }
    }

    @Override
    public void render(VertexConsumer buffer, Camera camera, float ticks) {
        Vec3 vec3 = camera.getPosition();
        float x = (float) (Mth.lerp(ticks, this.xo, this.x) - vec3.x());
        float y = (float) (Mth.lerp(ticks, this.yo, this.y) - vec3.y());
        float z = (float) (Mth.lerp(ticks, this.zo, this.z) - vec3.z());

        Vector3f[] vector3fs = new Vector3f[]{new Vector3f(-1.0F, -1.0F, 0.0F), new Vector3f(-1.0F, 1.0F, 0.0F), new Vector3f(1.0F, 1.0F, 0.0F), new Vector3f(1.0F, -1.0F, 0.0F)};
        float f4 = this.getQuadSize(ticks);

        for(int i = 0; i < 4; ++i) {
            Vector3f vector3f = vector3fs[i];
            vector3f.transform(QUATERNION);
            vector3f.mul(f4);
            vector3f.add(x, y, z);
        }

        float f7 = this.getU0();
        float f8 = this.getU1();
        float f5 = this.getV0();
        float f6 = this.getV1();
        int j = this.getLightColor(ticks);
        buffer.vertex(vector3fs[0].x(), vector3fs[0].y(), vector3fs[0].z()).uv(f8, f6).color(this.rCol, this.gCol, this.bCol, this.alpha).uv2(j).endVertex();
        buffer.vertex(vector3fs[1].x(), vector3fs[1].y(), vector3fs[1].z()).uv(f8, f5).color(this.rCol, this.gCol, this.bCol, this.alpha).uv2(j).endVertex();
        buffer.vertex(vector3fs[2].x(), vector3fs[2].y(), vector3fs[2].z()).uv(f7, f5).color(this.rCol, this.gCol, this.bCol, this.alpha).uv2(j).endVertex();
        buffer.vertex(vector3fs[3].x(), vector3fs[3].y(), vector3fs[3].z()).uv(f7, f6).color(this.rCol, this.gCol, this.bCol, this.alpha).uv2(j).endVertex();
    }

    @Override
    public ParticleRenderType getRenderType() {
        return ParticleRenderType.PARTICLE_SHEET_TRANSLUCENT;
    }

    public record Factory(SpriteSet sprites) implements ParticleProvider<SimpleParticleType> {
        
        @Override
        public Particle createParticle(SimpleParticleType simpleParticleType, ClientLevel world, double x, double y, double z, double velX, double velY, double velZ) {
            return new WaterCircleParticle(world, x, y, z, velX, sprites);
        }
        
    }
    
    public static void generate() {
        Minecraft minecraft = Minecraft.getInstance();
        if (WATER_CIRCLE_ENABLED.get() || minecraft.options.particles().get() == ParticleStatus.MINIMAL)
            return;
        if (minecraft.level == null || minecraft.player == null)
            return;
        ClientLevel level = minecraft.level;
        LocalPlayer player = minecraft.player;
        if (!level.isRainingAt(player.getOnPos()))
            return;
        
        Biome biome = level.getBiome(player.getOnPos()).value();
        int density = WATER_CIRCLE_DENSITY.get();
        int radius = WATER_CIRCLE_RADIUS.get();
        if(density <= 0 || radius <= 0) return;
        int randomDensity = level.random.nextInt(density) + (density / 2);
        
        for(int i = 0; i <= randomDensity; i++) {
            int x = level.random.nextInt(radius) - (radius / 2);
            int z = level.random.nextInt(radius) - (radius / 2);
            BlockPos playerPos = new BlockPos((int) player.getX() + x, (int) player.getY(), (int) player.getZ() + z);
            BlockPos pos = level.getHeightmapPos(Heightmap.Types.MOTION_BLOCKING, playerPos);
            
            if(level.getBlockState(pos.below()).is(Blocks.WATER) && level.getBlockState(pos).isAir()) {
                if(level.getFluidState(pos.below()).getAmount() == 8) {
                    int color = WATER_CIRCLE_COLORED.get() ? biome.getWaterColor() : 0;
                    ((Level) level).addParticle(VisualityParticles.WATER_CIRCLE.get(), pos.getX() + level.random.nextDouble(), pos.getY() + 0.05D, pos.getZ() + level.random.nextDouble(), color, 0, 0);
                }
            }
        }
    }
    
}
