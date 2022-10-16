package net.notcoded.runnerhunter.commands;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.LiteralText;
import net.notcoded.runnerhunter.Main;
import net.notcoded.runnerhunter.utilities.Permissions;
import net.notcoded.runnerhunter.utilities.RunnerHunter;
import net.notcoded.runnerhunter.utilities.Utilities;

import static net.minecraft.server.command.CommandManager.literal;

public class runnerhunter {

    static String prefix = "§8[§c§lRunner§4§lHunter§8] ";
    public static void register(CommandDispatcher<ServerCommandSource> dispatcher, boolean dedicated) {
        dispatcher.register(CommandManager.literal("runnerhunter")
                .executes(c -> {
                    Utilities.sendPlayerMessage(c.getSource().getPlayer(), prefix + "§cInvalid syntax! Use /runnerhunter help");
                    return Command.SINGLE_SUCCESS;
                })
                .then(CommandManager.literal("help")
                        .executes(c -> {
                            Utilities.sendPlayerMessage(c.getSource().getPlayer(), prefix + "§cCommands:");
                            Utilities.sendPlayerMessage(c.getSource().getPlayer(), "§4 - §c/pick <runner/hunter> - Shows the current runner/hunter(s).");
                            Utilities.sendPlayerMessage(c.getSource().getPlayer(), "§4 - §c/pick <reset/runner/hunter> <player> - Picks the runner/hunter.");
                            Utilities.sendPlayerMessage(c.getSource().getPlayer(), "§4 - §c/runnerhunter options onehit <true/false> - §cSelects if the hunter becomes the runner only by hitting the runner(s) once.");
                            Utilities.sendPlayerMessage(c.getSource().getPlayer(), "§4 - §c/runnerhunter options glow <hunter/runner> <true/false> - §cGlow hunters/runners | Option to glow the hunter/runner(s)");
                            Utilities.sendPlayerMessage(c.getSource().getPlayer(),"§4 - §c/glow <player> <true/false>" );
                            Utilities.sendPlayerMessage(c.getSource().getPlayer(), "§4- §c/runnerhunter <help>");
                            return Command.SINGLE_SUCCESS;
                }))
                .then(CommandManager.literal("options")
                        .executes(c -> {
                            if (!Permissions.hasPermission(c.getSource(), "runnerhunter.options", 4)) {
                                Utilities.sendPlayerMessage(c.getSource().getPlayer(), prefix + "§cYou do not have permission!");
                                return Command.SINGLE_SUCCESS;
                            }
                            Utilities.sendPlayerMessage(c.getSource().getPlayer(), prefix + "§cOptions:");
                            Utilities.sendPlayerMessage(c.getSource().getPlayer(), "§4 - §c/runnerhunter options onehit <true/false> - §cSelects if the hunter becomes the runner only by hitting the runner(s) once.");
                            Utilities.sendPlayerMessage(c.getSource().getPlayer(), "§4 - §c/runnerhunter options glow <hunter/runner> <true/false> - §cGlow hunters/runners | Option to glow the hunter/runner(s)");
                            return Command.SINGLE_SUCCESS;
                        })
                        .then(CommandManager.literal("onehit")
                                .executes(ctx -> {
                                    if (!Permissions.hasPermission(ctx.getSource(), "runnerhunter.onehit", 4)) {
                                        Utilities.sendPlayerMessage(ctx.getSource().getPlayer(), prefix + "§cYou do not have permission!");
                                        return Command.SINGLE_SUCCESS;
                                    }
                                    Main.isOneHit = !Main.isOneHit;
                                    Utilities.sendPlayerMessage(ctx.getSource().getPlayer(), prefix + " §cOne hit mode is now §4" + Main.isOneHit);
                                    return Command.SINGLE_SUCCESS;
                                })
                                .then(literal("true")
                                        .executes(ctx -> {
                                            if (!Permissions.hasPermission(ctx.getSource(), "runnerhunter.onehit", 4)) {
                                                Utilities.sendPlayerMessage(ctx.getSource().getPlayer(), prefix + "§cYou do not have permission!");
                                                return Command.SINGLE_SUCCESS;
                                            }
                                            Main.isOneHit = true;
                                            Utilities.sendPlayerMessage(ctx.getSource().getPlayer(), prefix + " §cOne hit mode is now §4" + Main.isOneHit);
                                            return Command.SINGLE_SUCCESS;
                                        }))
                                .then(literal("false")
                                        .executes(ctx -> {
                                            if (!Permissions.hasPermission(ctx.getSource(), "runnerhunter.onehit", 4)) {
                                                Utilities.sendPlayerMessage(ctx.getSource().getPlayer(), prefix + "§cYou do not have permission!");
                                                return Command.SINGLE_SUCCESS;
                                            }
                                            Main.isOneHit = false;
                                            Utilities.sendPlayerMessage(ctx.getSource().getPlayer(), prefix + " §cOne hit mode is now §4" + Main.isOneHit);
                                            return Command.SINGLE_SUCCESS;
                                        })))
                        .then(CommandManager.literal("glow")
                                .executes(c -> {
                                    if (!Permissions.hasPermission(c.getSource(), "runnerhunter.glow", 4)) {
                                        Utilities.sendPlayerMessage(c.getSource().getPlayer(), prefix + "§cYou do not have permission!");
                                        return Command.SINGLE_SUCCESS;
                                    }
                                    Utilities.sendPlayerMessage(c.getSource().getPlayer(), prefix + "§cOptions:");
                                    Utilities.sendPlayerMessage(c.getSource().getPlayer(), "§4/runnerhunter options onehit <true/false> - §cSelects if the hunter becomes the runner only by hitting the runner(s) once.");
                                    Utilities.sendPlayerMessage(c.getSource().getPlayer(), "§4/runnerhunter options glow <hunter/runner> <true/false> - §cGlow hunters/runners | Option to glow the hunter/runner(s)");
                                    return Command.SINGLE_SUCCESS;
                                })
                                .then(literal("hunter")
                                        .executes(ctx -> {
                                            if (!Permissions.hasPermission(ctx.getSource(), "runnerhunter.glow", 4)) {
                                                Utilities.sendPlayerMessage(ctx.getSource().getPlayer(), prefix + "§cYou do not have permission!");
                                                return Command.SINGLE_SUCCESS;
                                            }
                                            RunnerHunter.shouldGlowHunters = !RunnerHunter.shouldGlowHunters;
                                            Utilities.sendPlayerMessage(ctx.getSource().getPlayer(), prefix + " §cGlowing hunters is now §4" + RunnerHunter.shouldGlowHunters);
                                            return Command.SINGLE_SUCCESS;
                                        })
                                        .then(literal("true")
                                                .executes(ctx -> {
                                                    if (!Permissions.hasPermission(ctx.getSource(), "runnerhunter.glow", 4)) {
                                                        Utilities.sendPlayerMessage(ctx.getSource().getPlayer(), prefix + "§cYou do not have permission!");
                                                        return Command.SINGLE_SUCCESS;
                                                    }
                                                    RunnerHunter.shouldGlowHunters = true;
                                                    Utilities.sendPlayerMessage(ctx.getSource().getPlayer(), prefix + " §cGlowing hunters is now §4" + RunnerHunter.shouldGlowHunters);
                                                    return Command.SINGLE_SUCCESS;
                                                }))
                                        .then(literal("false")
                                                .executes(ctx -> {
                                                    if (!Permissions.hasPermission(ctx.getSource(), "runnerhunter.glow", 4)) {
                                                        Utilities.sendPlayerMessage(ctx.getSource().getPlayer(), prefix + "§cYou do not have permission!");
                                                        return Command.SINGLE_SUCCESS;
                                                    }
                                                    RunnerHunter.shouldGlowHunters = false;
                                                    Utilities.sendPlayerMessage(ctx.getSource().getPlayer(), prefix + " §cGlowing hunters is now §4" + RunnerHunter.shouldGlowHunters);
                                                    return Command.SINGLE_SUCCESS;
                                                })))





                                .then(literal("runner")
                                        .executes(ctx -> {
                                            if (!Permissions.hasPermission(ctx.getSource(), "runnerhunter.glow", 4)) {
                                                Utilities.sendPlayerMessage(ctx.getSource().getPlayer(), prefix + "§cYou do not have permission!");
                                                return Command.SINGLE_SUCCESS;
                                            }
                                            RunnerHunter.shouldGlowRunners = !RunnerHunter.shouldGlowRunners;
                                            Utilities.sendPlayerMessage(ctx.getSource().getPlayer(), prefix + " §cGlowing runners is now §4" + RunnerHunter.shouldGlowRunners);
                                            return Command.SINGLE_SUCCESS;
                                        })
                                        .then(literal("true")
                                                .executes(ctx -> {
                                                    if (!Permissions.hasPermission(ctx.getSource(), "runnerhunter.glow", 4)) {
                                                        Utilities.sendPlayerMessage(ctx.getSource().getPlayer(), prefix + "§cYou do not have permission!");
                                                        return Command.SINGLE_SUCCESS;
                                                    }
                                                    RunnerHunter.shouldGlowRunners = true;
                                                    Utilities.sendPlayerMessage(ctx.getSource().getPlayer(), prefix + " §cGlowing runners is now §4" + RunnerHunter.shouldGlowRunners);
                                                    return Command.SINGLE_SUCCESS;
                                                }))
                                        .then(literal("false")
                                                .executes(ctx -> {
                                                    if (!Permissions.hasPermission(ctx.getSource(), "runnerhunter.glow", 4)) {
                                                        Utilities.sendPlayerMessage(ctx.getSource().getPlayer(), prefix + "§cYou do not have permission!");
                                                        return Command.SINGLE_SUCCESS;
                                                    }
                                                    RunnerHunter.shouldGlowRunners = false;
                                                    Utilities.sendPlayerMessage(ctx.getSource().getPlayer(), prefix + " §cGlowing runners is now §4" + RunnerHunter.shouldGlowRunners);
                                                    return Command.SINGLE_SUCCESS;
                                                }))))));
    }
}
