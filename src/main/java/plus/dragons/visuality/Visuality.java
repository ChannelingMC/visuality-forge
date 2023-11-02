package plus.dragons.visuality;

import net.minecraft.resources.ResourceLocation;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModLoadingContext;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.fml.javafmlmod.FMLJavaModLoadingContext;
import net.neoforged.fml.loading.FMLLoader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import plus.dragons.visuality.config.Config;
import plus.dragons.visuality.registry.VisualityParticles;
import plus.dragons.visuality.registry.VisualityRegistries;

@Mod(Visuality.ID)
public class Visuality {
    public static final String ID = "visuality";
    private static final Logger LOGGER = LoggerFactory.getLogger("Visuality");
    
    public Visuality() {
        if (FMLLoader.getDist() == Dist.CLIENT) {
            this.onInitialize();
            LOGGER.info("Visuality has initialized, have fun with more particles!");
        } else {
            LOGGER.warn("Visuality is installed on a dedicated server, skip loading...");
        }
    }
    
    public void onInitialize() {
        ModLoadingContext.get().registerConfig(ModConfig.Type.CLIENT, Config.SPEC, ID + "/config.toml");
        
        IEventBus modBus = FMLJavaModLoadingContext.get().getModEventBus();
        VisualityRegistries.Registers.register(modBus);
        VisualityParticles.register();
        
        modBus.addListener(Config::registerClientResourceListener);
        modBus.addListener(VisualityParticles::registerProviders);
    }
    
    public static ResourceLocation location(String path) {
        return new ResourceLocation(ID, path);
    }

}
