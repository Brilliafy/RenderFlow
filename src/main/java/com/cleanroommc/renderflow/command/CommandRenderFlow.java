package com.cleanroommc.renderflow.command;

import com.cleanroommc.renderflow.RenderFlow;
import com.cleanroommc.renderflow.config.ModConfig;
import com.cleanroommc.renderflow.culling.OcclusionCullingEngine;
import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.common.config.Config;
import net.minecraftforge.common.config.ConfigManager;

public class CommandRenderFlow extends CommandBase {

    @Override
    public String getName() {
        return "renderflow";
    }

    @Override
    public String getUsage(ICommandSender sender) {
        return "/renderflow [stats|clear|toggle|reload]";
    }

    @Override
    public int getRequiredPermissionLevel() {
        return 0; // Available to client users
    }

    @Override
    public boolean checkPermission(MinecraftServer server, ICommandSender sender) {
        return true;
    }

    @Override
    public void execute(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException {
        String sub = args.length == 0 ? "stats" : args[0].toLowerCase();

        if (sub.equals("stats") || sub.equals("status")) {
            printStats(sender);
        } else if (sub.equals("clear")) {
            OcclusionCullingEngine.clearStats();
            sender.sendMessage(new TextComponentString(TextFormatting.GREEN + "Render Flow statistics reset."));
        } else if (sub.equals("toggle")) {
            ModConfig.OCCLUSION_CULLING.enabled = !ModConfig.OCCLUSION_CULLING.enabled;
            sender.sendMessage(new TextComponentString(TextFormatting.YELLOW + "Hi-Z Occlusion Culling now: " +
                    (ModConfig.OCCLUSION_CULLING.enabled ? TextFormatting.GREEN + "ENABLED" : TextFormatting.RED + "DISABLED")));
        } else if (sub.equals("reload")) {
            ConfigManager.sync(RenderFlow.MOD_ID, Config.Type.INSTANCE);
            sender.sendMessage(new TextComponentString(TextFormatting.GREEN + "Render Flow configuration reloaded."));
        } else {
            sender.sendMessage(new TextComponentString(TextFormatting.RED + "Usage: " + getUsage(sender)));
        }
    }

    private static void printStats(ICommandSender sender) {
        long entCulled = OcclusionCullingEngine.ENTITIES_CULLED.get();
        long entRendered = OcclusionCullingEngine.ENTITIES_RENDERED.get();
        long teCulled = OcclusionCullingEngine.TILE_ENTITIES_CULLED.get();
        long teRendered = OcclusionCullingEngine.TILE_ENTITIES_RENDERED.get();

        long totalEntities = entCulled + entRendered;
        double entPercent = totalEntities > 0 ? (entCulled * 100.0 / totalEntities) : 0.0;

        long totalTE = teCulled + teRendered;
        double tePercent = totalTE > 0 ? (teCulled * 100.0 / totalTE) : 0.0;

        sender.sendMessage(new TextComponentString(TextFormatting.GOLD + "=== Hi-Z Occlusion Culling Statistics ==="));
        sender.sendMessage(new TextComponentString(TextFormatting.YELLOW + "Status: " +
                (ModConfig.OCCLUSION_CULLING.enabled ? TextFormatting.GREEN + "ACTIVE" : TextFormatting.RED + "DISABLED")));
        sender.sendMessage(new TextComponentString(TextFormatting.AQUA + "Entities Culled: " + TextFormatting.WHITE + entCulled +
                TextFormatting.GRAY + " (" + String.format("%.1f", entPercent) + "% saved)"));
        sender.sendMessage(new TextComponentString(TextFormatting.DARK_AQUA + "Entities Rendered: " + TextFormatting.WHITE + entRendered));
        sender.sendMessage(new TextComponentString(TextFormatting.GREEN + "TileEntities Culled: " + TextFormatting.WHITE + teCulled +
                TextFormatting.GRAY + " (" + String.format("%.1f", tePercent) + "% saved)"));
        sender.sendMessage(new TextComponentString(TextFormatting.DARK_GREEN + "TileEntities Rendered: " + TextFormatting.WHITE + teRendered));
    }
}
