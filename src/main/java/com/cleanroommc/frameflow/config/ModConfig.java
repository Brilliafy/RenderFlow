package com.cleanroommc.frameflow.config;

import net.minecraftforge.common.config.Config;
import net.minecraftforge.common.config.ConfigManager;
import net.minecraftforge.fml.client.event.ConfigChangedEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

@Config(modid = "frameflow", name = "frameflow")
public class ModConfig {

    @Config.Comment("Replace erratic Display.sync() with a nanosecond-precision monotonic hybrid sleep-spin frame pacer.")
    @Config.Name("Enable Frame Pacing")
    public static boolean enableFramePacing = true;

    @Config.Comment("Eliminate gratuitous Thread.yield() calls that cause OS scheduler descheduling and micro-stutter.")
    @Config.Name("Eliminate Thread Yield")
    public static boolean eliminateThreadYield = true;

    @Config.Comment("Enforce a strict render-thread time slice on chunk VBO uploads to prevent chunk loading stutter spikes.")
    @Config.Name("Enable Chunk Upload Time Slicing")
    public static boolean enableChunkUploadTimeSlicing = true;

    @Config.Comment("Maximum milliseconds per frame allowed for uploading compiled chunk meshes to OpenGL (default: 1.5ms).")
    @Config.RangeDouble(min = 0.2D, max = 10.0D)
    @Config.Name("Chunk Upload Budget (ms)")
    public static double chunkUploadBudgetMs = 1.5D;

    public static long getChunkUploadBudgetNanos() {
        return (long) (chunkUploadBudgetMs * 1_000_000.0);
    }

    @Mod.EventBusSubscriber(modid = "frameflow")
    public static class ConfigSyncHandler {
        @SubscribeEvent
        public static void onConfigChanged(ConfigChangedEvent.OnConfigChangedEvent event) {
            if (event.getModID().equals("frameflow")) {
                ConfigManager.sync("frameflow", Config.Type.INSTANCE);
            }
        }
    }
}
