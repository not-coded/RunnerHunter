package net.notcoded.runnerhunter.commands;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.notcoded.runnerhunter.utilities.Permissions;
import net.notcoded.runnerhunter.utilities.Utilities;

import static com.mojang.brigadier.arguments.StringArgumentType.getString;
import static com.mojang.brigadier.arguments.StringArgumentType.string;
import static net.minecraft.server.command.CommandManager.literal;

public class glow {
    static String prefix = "§8[§c§lRunner§4§lHunter§8] ";

    public static void register(CommandDispatcher<ServerCommandSource> dispatcher, boolean dedicated) {
        dispatcher.register(CommandManager.literal("glow")
                .executes(c -> {
                    Utilities.sendPlayerMessage(c.getSource().getPlayer(), prefix + "§cInvalid syntax! Use /runnerhunter help");
                    return Command.SINGLE_SUCCESS;
                })
                .then(CommandManager.argument("player", string())
                        .executes(c -> {
                            if (!Permissions.hasPermission(c.getSource(), "runnerhunter.glow", 4)) {
                                Utilities.sendPlayerMessage(c.getSource().getPlayer(), prefix + "§cYou do not have permission!");
                                return Command.SINGLE_SUCCESS;
                            }
                            Utilities.applyGlow(Utilities.getServerPlayerfromName(getString(c, "player")), !Utilities.getServerPlayerfromName(getString(c, "player")).isGlowing());
                            Utilities.sendPlayerMessage(c.getSource().getPlayer(), prefix + " §cGlowing " + getString(c, "player") + " is now §4" + Utilities.getServerPlayerfromName(getString(c, "player")).isGlowing());
                            return Command.SINGLE_SUCCESS;
                        })
                        .then(literal("true")
                                .executes(ctx -> {
                                    if (!Permissions.hasPermission(ctx.getSource(), "runnerhunter.glow", 4)) {
                                        Utilities.sendPlayerMessage(ctx.getSource().getPlayer(), prefix + "§cYou do not have permission!");
                                        return Command.SINGLE_SUCCESS;
                                    }
                                    Utilities.applyGlow(Utilities.getServerPlayerfromName(getString(ctx, "player")), true);
                                    Utilities.sendPlayerMessage(ctx.getSource().getPlayer(), prefix + " §c" + getString(ctx, "player") + " is now §4glowing.");
                                    return Command.SINGLE_SUCCESS;
                                }))
                        .then(literal("false")
                                .executes(ctx -> {
                                    if (!Permissions.hasPermission(ctx.getSource(), "runnerhunter.glow", 4)) {
                                        Utilities.sendPlayerMessage(ctx.getSource().getPlayer(), prefix + "§cYou do not have permission!");
                                        return Command.SINGLE_SUCCESS;
                                    }
                                    Utilities.applyGlow(Utilities.getServerPlayerfromName(getString(ctx, "player")), false);
                                    Utilities.sendPlayerMessage(ctx.getSource().getPlayer(), prefix + " §c" + getString(ctx, "player") + " is no longer §4glowing.");
                                    return Command.SINGLE_SUCCESS;
                                }))));


    }
}
