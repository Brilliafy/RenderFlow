package com.cleanroommc.renderflow;

import com.cleanroommc.renderflow.command.CommandRenderFlow;
import com.cleanroommc.renderflow.config.ModConfig;
import net.minecraftforge.client.ClientCommandHandler;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.config.Config;
import net.minecraftforge.common.config.ConfigManager;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * RenderFlow - client-side frame pacing, thread yield elimination,
 * and chunk upload time-slicing for Minecraft 1.12.2 on Cleanroom or Forge.
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
    public static final String MOD_NAME = "RenderFlow";
    public static final String VERSION = "1.0.0";

    public static final Logger LOGGER = LogManager.getLogger(MOD_NAME);

    @Mod.Instance(MOD_ID)
    public static RenderFlow instance;

    @Mod.EventHandler
    public void preInit(FMLPreInitializationEvent event) {
        MinecraftForge.EVENT_BUS.register(ModConfig.ConfigSyncHandler.class);
        ConfigManager.sync(MOD_ID, Config.Type.INSTANCE);

        LOGGER.info("[RenderFlow] Initialized; frame pacing {}, chunk upload budget {} ms, thread yield elimination {}.",
                ModConfig.enableFramePacing ? "enabled" : "disabled",
                ModConfig.chunkUploadBudgetMs,
                ModConfig.eliminateThreadYield ? "enabled" : "disabled");
    }

    @Mod.EventHandler
    public void init(FMLInitializationEvent event) {
        if (event.getSide().isClient()) {
            ClientCommandHandler.instance.registerCommand(new CommandRenderFlow());
        }
    }
}
