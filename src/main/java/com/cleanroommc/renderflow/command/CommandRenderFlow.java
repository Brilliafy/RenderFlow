package com.cleanroommc.renderflow.command;

import com.cleanroommc.renderflow.RenderFlow;
import com.cleanroommc.renderflow.config.ModConfig;
import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.common.config.Config;
import net.minecraftforge.common.config.ConfigManager;

import java.util.Collections;
import java.util.List;

public class CommandRenderFlow extends CommandBase {

    @Override
    public String getName() {
        return "renderflow";
    }

    @Override
    public List<String> getAliases() {
        return Collections.singletonList("rf");
    }

    @Override
    public String getUsage(ICommandSender sender) {
        return "/renderflow [status|pacing|yield|slicing|budget <ms>|reload]";
    }

    @Override
    public int getRequiredPermissionLevel() {
        return 0;
    }

    @Override
    public boolean checkPermission(MinecraftServer server, ICommandSender sender) {
        return true;
    }

    @Override
    public void execute(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException {
        String sub = args.length == 0 ? "status" : args[0].toLowerCase();

        switch (sub) {
            case "status":
            case "stats":
                printStatus(sender);
                break;

            case "pacing":
                ModConfig.enableFramePacing = !ModConfig.enableFramePacing;
                syncConfig();
                sender.sendMessage(new TextComponentString(TextFormatting.YELLOW + "Frame Pacing: " +
                        (ModConfig.enableFramePacing ? TextFormatting.GREEN + "ENABLED" : TextFormatting.RED + "DISABLED")));
                break;

            case "yield":
                ModConfig.eliminateThreadYield = !ModConfig.eliminateThreadYield;
                syncConfig();
                sender.sendMessage(new TextComponentString(TextFormatting.YELLOW + "Eliminate Thread Yield: " +
                        (ModConfig.eliminateThreadYield ? TextFormatting.GREEN + "ENABLED" : TextFormatting.RED + "DISABLED")));
                break;

            case "slicing":
                ModConfig.enableChunkUploadTimeSlicing = !ModConfig.enableChunkUploadTimeSlicing;
                syncConfig();
                sender.sendMessage(new TextComponentString(TextFormatting.YELLOW + "Chunk Upload Time Slicing: " +
                        (ModConfig.enableChunkUploadTimeSlicing ? TextFormatting.GREEN + "ENABLED" : TextFormatting.RED + "DISABLED")));
                break;

            case "budget":
                if (args.length < 2) {
                    sender.sendMessage(new TextComponentString(TextFormatting.RED + "Usage: /renderflow budget <0.2 - 10.0>"));
                    return;
                }
                try {
                    double budget = Double.parseDouble(args[1]);
                    if (budget < 0.2D || budget > 10.0D) {
                        sender.sendMessage(new TextComponentString(TextFormatting.RED + "Budget must be between 0.2 and 10.0 ms."));
                        return;
                    }
                    ModConfig.chunkUploadBudgetMs = budget;
                    syncConfig();
                    sender.sendMessage(new TextComponentString(TextFormatting.GREEN + "Chunk upload budget set to " + budget + " ms."));
                } catch (NumberFormatException e) {
                    sender.sendMessage(new TextComponentString(TextFormatting.RED + "Invalid number format: " + args[1]));
                }
                break;

            case "reload":
                ConfigManager.sync(RenderFlow.MOD_ID, Config.Type.INSTANCE);
                sender.sendMessage(new TextComponentString(TextFormatting.GREEN + "RenderFlow configuration reloaded."));
                break;

            default:
                sender.sendMessage(new TextComponentString(TextFormatting.RED + "Usage: " + getUsage(sender)));
                break;
        }
    }

    private static void syncConfig() {
        ConfigManager.sync(RenderFlow.MOD_ID, Config.Type.INSTANCE);
    }

    private static void printStatus(ICommandSender sender) {
        sender.sendMessage(new TextComponentString(TextFormatting.GOLD + "=== RenderFlow Status ==="));
        sender.sendMessage(new TextComponentString(TextFormatting.GRAY + "Frame Pacing: " +
                (ModConfig.enableFramePacing ? TextFormatting.GREEN + "ENABLED" : TextFormatting.RED + "DISABLED")));
        sender.sendMessage(new TextComponentString(TextFormatting.GRAY + "Eliminate Thread Yield: " +
                (ModConfig.eliminateThreadYield ? TextFormatting.GREEN + "ENABLED" : TextFormatting.RED + "DISABLED")));
        sender.sendMessage(new TextComponentString(TextFormatting.GRAY + "Chunk Upload Time Slicing: " +
                (ModConfig.enableChunkUploadTimeSlicing ? TextFormatting.GREEN + "ENABLED" : TextFormatting.RED + "DISABLED")));
        sender.sendMessage(new TextComponentString(TextFormatting.GRAY + "Chunk Upload Budget: " +
                TextFormatting.AQUA + ModConfig.chunkUploadBudgetMs + " ms"));
    }
}
