package net.notcoded.runnerhunter.commands;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.LiteralText;
import net.notcoded.runnerhunter.utilities.Permissions;
import net.notcoded.runnerhunter.utilities.RunnerHunter;
import net.notcoded.runnerhunter.utilities.Utilities;

import java.util.Arrays;

import static com.mojang.brigadier.arguments.StringArgumentType.getString;
import static com.mojang.brigadier.arguments.StringArgumentType.string;
import static net.minecraft.server.command.CommandManager.literal;

public class pick {

    static String prefix = "§8[§c§lRunner§4§lHunter§8] ";
    public static void register(CommandDispatcher<ServerCommandSource> dispatcher, boolean dedicated) {
        dispatcher.register(literal("pick")
                .executes(c -> {
                    if (!Permissions.hasPermission(c.getSource(), "runnerhunter.pick", 4)) {
                        Utilities.sendPlayerMessage(c.getSource().getPlayer(), prefix + "§cYou do not have permission!");
                    } else {
                        Utilities.sendPlayerMessage(c.getSource().getPlayer(), prefix + "§cInvalid syntax! Use /runnerhunter help");
                    }
                    return Command.SINGLE_SUCCESS;
                })
                .then(literal("reset")
                        .executes(ctx -> {
                            if (!Permissions.hasPermission(ctx.getSource(), "runnerhunter.reset", 4)) {
                                Utilities.sendPlayerMessage(ctx.getSource().getPlayer(), prefix + "§cYou do not have permission!");
                            } else {
                                RunnerHunter.reset(ctx.getSource());
                            }
                            return Command.SINGLE_SUCCESS;
                        }))
                .then(literal("runner")
                        .executes(ctx -> {
                            if (!Permissions.hasPermission(ctx.getSource(), "runnerhunter.pick", 4)) {
                                Utilities.sendPlayerMessage(ctx.getSource().getPlayer(), prefix + "§cYou do not have permission!");
                            } else {
                                Utilities.sendPlayerMessage(ctx.getSource().getPlayer(), prefix + "§cHere are the current runners: " + Arrays.toString(Arrays.toString(RunnerHunter.runners).replaceAll("Placeholder", "").split(", ")));
                            }
                            return Command.SINGLE_SUCCESS;
                        })
                .then(CommandManager.argument("player", string())
                        .executes(ctx -> {
                            if (!Permissions.hasPermission(ctx.getSource(), "runnerhunter.pick", 4)) {
                                Utilities.sendPlayerMessage(ctx.getSource().getPlayer(), prefix + "§cYou do not have permission!");
                                return Command.SINGLE_SUCCESS;
                            }
                            if(RunnerHunter.isRunner(getString(ctx, "player"))){
                                RunnerHunter.removeRunner(getString(ctx, "player"));
                                Utilities.sendPlayerMessage(ctx.getSource().getPlayer(), prefix + " §c" + getString(ctx, "player") + " is no longer the runner.");
                            } else {
                                RunnerHunter.setRunner(getString(ctx, "player"), RunnerHunter.shouldGlowRunners);
                                Utilities.sendPlayerMessage(ctx.getSource().getPlayer(), prefix + " §c" + getString(ctx, "player") + " is now the runner.");
                            }
                            return Command.SINGLE_SUCCESS;
                        })))
                .then(literal("hunter")
                        .executes(ctx -> {
                            if (!Permissions.hasPermission(ctx.getSource(), "runnerhunter.pick", 4)) {
                                Utilities.sendPlayerMessage(ctx.getSource().getPlayer(), prefix + "§cYou do not have permission!");
                            } else {
                                Utilities.sendPlayerMessage(ctx.getSource().getPlayer(), prefix + "§cHere are the current hunters: " + Arrays.toString(Arrays.toString(RunnerHunter.hunters).replaceAll("Placeholder", "").split(", ")));
                            }
                            return Command.SINGLE_SUCCESS;
                        })

                .then(CommandManager.argument("player", string())
                        .executes(ctx -> {
                            if (!Permissions.hasPermission(ctx.getSource(), "runnerhunter.pick", 4)) {
                                Utilities.sendPlayerMessage(ctx.getSource().getPlayer(), prefix + "§cYou do not have permission!");
                                return Command.SINGLE_SUCCESS;
                            }
                            if(RunnerHunter.isHunter(getString(ctx, "player"))){
                                RunnerHunter.removeHunter(getString(ctx, "player"));
                                Utilities.sendPlayerMessage(ctx.getSource().getPlayer(), prefix + " §c" + getString(ctx, "player") + " is no longer the hunter.");
                            } else {
                                RunnerHunter.setHunter(getString(ctx, "player"), RunnerHunter.shouldGlowHunters);
                                Utilities.sendPlayerMessage(ctx.getSource().getPlayer(), prefix + " §c" + getString(ctx, "player") + " is now the hunter.");
                            }
                            return Command.SINGLE_SUCCESS;
                        }))));



    }
}
