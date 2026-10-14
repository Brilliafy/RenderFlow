package com.cleanroommc.renderflow;

import com.cleanroommc.renderflow.command.CommandRenderFlow;
import com.cleanroommc.renderflow.config.ModConfig;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.config.Config;
import net.minecraftforge.common.config.ConfigManager;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.event.FMLServerStartingEvent;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Render Flow - client-side rendering optimizations for Cleanroom 1.12.2.
 *
 * <p>Merged from two standalone optimizers:</p>
 * <ul>
 *   <li><b>ConsistentFrametime</b> - a nanosecond-precision monotonic frame pacer replacing
 *       {@code Display.sync()}, {@code Thread.yield()} elimination, and chunk-VBO upload time
 *       slicing.</li>
 *   <li><b>HiZCulling112</b> - software ray-traced occlusion culling for entities and tile
 *       entities.</li>
 * </ul>
 */
@Mod(
        modid = RenderFlow.MOD_ID,
        name = RenderFlow.MOD_NAME,
        version = RenderFlow.VERSION,
        acceptedMinecraftVersions = "[1.12.2]",
        acceptableRemoteVersions = "*",
        clientSideOnly = true
)
public class RenderFlow {

    public static final String MOD_ID = "renderflow";
    public static final String MOD_NAME = "Render Flow";
    public static final String VERSION = "1.0.0";

    public static final Logger LOGGER = LogManager.getLogger(MOD_NAME);

    @Mod.Instance(MOD_ID)
    public static RenderFlow instance;

    @Mod.EventHandler
    public void preInit(FMLPreInitializationEvent event) {
        MinecraftForge.EVENT_BUS.register(ModConfig.ConfigSyncHandler.class);
        ConfigManager.sync(MOD_ID, Config.Type.INSTANCE);

        LOGGER.info("[RenderFlow] Initialized; frame pacing {}, chunk upload budget {} ms; occlusion culling {}.",
                ModConfig.FRAME_PACING.enableFramePacing ? "enabled" : "disabled",
                ModConfig.FRAME_PACING.chunkUploadBudgetMs,
                ModConfig.OCCLUSION_CULLING.enabled ? "enabled" : "disabled");
    }

    @Mod.EventHandler
    public void serverStarting(FMLServerStartingEvent event) {
        event.registerServerCommand(new CommandRenderFlow());
    }
}
