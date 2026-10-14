package com.cleanroommc.renderflow.config;

import net.minecraftforge.common.config.Config;
import net.minecraftforge.common.config.ConfigManager;
import net.minecraftforge.fml.client.event.ConfigChangedEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

@Config(modid = "renderflow", name = "renderflow")
public class ModConfig {

    @Config.Name("Frame Pacing")
    @Config.Comment("Monotonic frame pacing, chunk-upload time slicing and stutter elimination (from ConsistentFrametime).")
    public static FramePacing FRAME_PACING = new FramePacing();

    @Config.Name("Occlusion Culling")
    @Config.Comment("Software ray-traced occlusion culling for entities and tile entities (from HiZCulling112).")
    public static OcclusionCulling OCCLUSION_CULLING = new OcclusionCulling();

    @Config.Comment("Log debug information to the game log.")
    @Config.Name("Debug Logging")
    public static boolean debugLogging = false;

    public static class FramePacing {

        @Config.Comment("Replace erratic Display.sync() with a nanosecond-precision monotonic hybrid sleep-spin frame pacer.")
        @Config.Name("Enable Frame Pacing")
        public boolean enableFramePacing = true;

        @Config.Comment("Eliminate gratuitous Thread.yield() calls that cause OS scheduler descheduling and micro-stutter.")
        @Config.Name("Eliminate Thread Yield")
        public boolean eliminateThreadYield = true;

        @Config.Comment("Enforce a strict render-thread time slice on chunk VBO uploads to completely eliminate chunk loading freezes.")
        @Config.Name("Enable Chunk Upload Time Slicing")
        public boolean enableChunkUploadTimeSlicing = true;

        @Config.Comment("Maximum milliseconds per frame allowed for uploading compiled chunk meshes to OpenGL (default: 1.5ms).")
        @Config.RangeDouble(min = 0.2D, max = 10.0D)
        @Config.Name("Chunk Upload Budget (ms)")
        public double chunkUploadBudgetMs = 1.5D;

        @Config.Comment("Throttle off-screen and invisible animated textures to conserve PCIe bandwidth and CPU time.")
        @Config.Name("Enable Smart Animations")
        public boolean enableSmartAnimations = true;
    }

    public static class OcclusionCulling {

        @Config.Comment("Master toggle for software Hierarchical Occlusion Culling.")
        @Config.Name("Enable Mod")
        public boolean enabled = true;

        @Config.Comment("Enable occlusion culling for living entities and mobs.")
        @Config.Name("Cull Entities")
        public boolean cullEntities = true;

        @Config.Comment("Enable occlusion culling for tile entities (chests, spawners, etc.).")
        @Config.Name("Cull Tile Entities")
        public boolean cullTileEntities = true;

        @Config.Comment("Minimum distance (in blocks) before an entity or tile entity is eligible for occlusion culling.")
        @Config.RangeDouble(min = 1.0D, max = 16.0D)
        @Config.Name("Min Cull Distance")
        public double minCullDistance = 3.0D;

        @Config.Comment("Maximum distance (in blocks) to raytrace occlusion geometry.")
        @Config.RangeDouble(min = 16.0D, max = 128.0D)
        @Config.Name("Max Raytrace Distance")
        public double maxRaytraceDistance = 80.0D;

        @Config.Comment("Always exempt bosses, dragons, and entities with health bars.")
        @Config.Name("Exempt Bosses")
        public boolean exemptBosses = true;

        @Config.Comment("Enable debug logging to console.")
        @Config.Name("Debug Logging")
        public boolean debugLogging = false;
    }

    public static long getChunkUploadBudgetNanos() {
        return (long) (FRAME_PACING.chunkUploadBudgetMs * 1_000_000.0);
    }

    @Mod.EventBusSubscriber(modid = "renderflow")
    public static class ConfigSyncHandler {
        @SubscribeEvent
        public static void onConfigChanged(ConfigChangedEvent.OnConfigChangedEvent event) {
            if (event.getModID().equals("renderflow")) {
                ConfigManager.sync("renderflow", Config.Type.INSTANCE);
            }
        }
    }
}
