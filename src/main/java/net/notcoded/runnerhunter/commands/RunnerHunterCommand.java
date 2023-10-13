package net.notcoded.runnerhunter.commands;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import me.lucko.fabric.api.permissions.v0.Permissions;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.commands.arguments.DimensionArgument;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.commands.arguments.coordinates.Vec3Argument;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.phys.Vec3;
import net.notcoded.codelib.players.AccuratePlayer;
import net.notcoded.codelib.util.pos.EntityPos;
import net.notcoded.codelib.util.world.WorldUtil;
import net.notcoded.runnerhunter.game.GameConfiguration;
import net.notcoded.runnerhunter.game.RunnerHunterGame;
import net.notcoded.runnerhunter.game.level.RunnerHunterLevel;
import net.notcoded.runnerhunter.utilities.player.PlayerDataManager;

import java.util.ArrayList;
import java.util.UUID;
import java.util.stream.Stream;

public class RunnerHunterCommand {

    private static Stream<String> getGames() {
        ArrayList<String> gameIDs = new ArrayList<>();
        RunnerHunterGame.games.forEach((game -> gameIDs.add(game.gameID.toString())));

        return gameIDs.stream();
    }

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher, boolean dedicated) {
        dispatcher.register(Commands.literal("runnerhunter")
                .requires(Permissions.require("runnerhunter.manage", 4))
                .executes(c -> {
                    c.getSource().sendFailure(new TextComponent("§cInvalid syntax! Use /runnerhunter help"));
                    return 0;
                })
                .then(Commands.literal("help")
                        .executes(c -> {
                            c.getSource().sendFailure(new TextComponent("§cCommands:"));
                            c.getSource().sendFailure(new TextComponent("§4 - §c/runnerhunter <game> <option> <onehit> <true/false>"));
                            c.getSource().sendFailure(new TextComponent("§4 - §c/runnerhunter <game> <option> <glow> <runner/hunters> <true/false>"));
                            c.getSource().sendFailure(new TextComponent("§4 - §c/runnerhunter <game> <add/remove> <runner/hunter> <player>"));
                            c.getSource().sendFailure(new TextComponent("§4 - §c/runnerhunter <game> <start/delete>"));

                            return Command.SINGLE_SUCCESS;
                }))
                .then(Commands.literal("create")
                        .executes(c -> {

                            ServerPlayer player = c.getSource().getPlayerOrException();
                            RunnerHunterGame game = new RunnerHunterGame(AccuratePlayer.create(player), new ArrayList<>(), new GameConfiguration(false, false, false, 300, new RunnerHunterLevel(player.getLevel(), new EntityPos(player.position()), new EntityPos(player.position().add(0, 0, 10)))));

                            c.getSource().sendFailure(new TextComponent("§cCreated game with id §4" + game.gameID));

                            return Command.SINGLE_SUCCESS;
                        }))
                .then(Commands.literal("game")
                        .then(Commands.argument("game", StringArgumentType.string())
                                .suggests(((context, builder) -> SharedSuggestionProvider.suggest((getGames()), builder)))
                                .then(Commands.literal("start")
                                        .executes(c -> {
                                            RunnerHunterGame game = RunnerHunterGame.getGame(UUID.fromString(StringArgumentType.getString(c, "game")));
                                            if(game == null) {
                                                c.getSource().sendFailure(new TextComponent("§cInvalid game!"));
                                                return 0;
                                            }

                                            if(game.hasStarted) {
                                                c.getSource().sendFailure(new TextComponent("§cYou cannot modify a game that's already started!"));
                                                return 0;
                                            }

                                            if(game.hunters.isEmpty()) {
                                                c.getSource().sendFailure(new TextComponent("§cThere must be atleast 1 hunter for the game to start!"));
                                                return 0;
                                            }

                                            game.startGame();
                                            c.getSource().sendSuccess(new TextComponent("§cGame §4" + game.gameID + " §chas been started!"), true);

                                            return 1;
                                        })
                                )
                                .then(Commands.literal("delete")
                                        .executes(c -> {
                                            RunnerHunterGame game = RunnerHunterGame.getGame(UUID.fromString(StringArgumentType.getString(c, "game")));
                                            if(game == null) {
                                                c.getSource().sendFailure(new TextComponent("§cInvalid game!"));
                                                return 0;
                                            }

                                            if(game.hasStarted) {
                                                c.getSource().sendFailure(new TextComponent("§cYou cannot modify a game that's already started!"));
                                                return 0;
                                            }

                                            game.shouldWait = false;
                                            game.endGame();
                                            RunnerHunterGame.games.remove(game);
                                            c.getSource().sendSuccess(new TextComponent("§cGame §4" + game.gameID + " §chas been deleted!"), true);

                                            return 1;
                                        })
                                )
                                .then(Commands.literal("reset")
                                        .then(Commands.literal("runner")
                                                .executes(c -> {
                                                    RunnerHunterGame game = RunnerHunterGame.getGame(UUID.fromString(StringArgumentType.getString(c, "game")));
                                                    if (game == null) {
                                                        c.getSource().sendFailure(new TextComponent("§cInvalid game!"));
                                                        return 0;
                                                    }

                                                    if (game.hasStarted) {
                                                        c.getSource().sendFailure(new TextComponent("§cYou cannot modify a game that's already started!"));
                                                        return 0;
                                                    }

                                                    if(game.runner == null) {
                                                        c.getSource().sendFailure(new TextComponent("§cThe runner is not set!"));
                                                        return 0;
                                                    }

                                                    game.runner = null;
                                                    c.getSource().sendSuccess(new TextComponent("§cGame §4" + game.gameID + "§c's runner has been reset!"), true);

                                                    return 1;
                                                })
                                        )
                                        .then(Commands.literal("hunters")
                                                .executes(c -> {
                                                    RunnerHunterGame game = RunnerHunterGame.getGame(UUID.fromString(StringArgumentType.getString(c, "game")));
                                                    if (game == null) {
                                                        c.getSource().sendFailure(new TextComponent("§cInvalid game!"));
                                                        return 0;
                                                    }

                                                    if (game.hasStarted) {
                                                        c.getSource().sendFailure(new TextComponent("§cYou cannot modify a game that's already started!"));
                                                        return 0;
                                                    }

                                                    if(game.hunters.isEmpty()) {
                                                        c.getSource().sendFailure(new TextComponent("§cThe hunters are empty!"));
                                                        return 0;
                                                    }

                                                    game.hunters = new ArrayList<>();
                                                    c.getSource().sendSuccess(new TextComponent("§cGame §4" + game.gameID + "§c's hunters has been reset!"), true);

                                                    return 1;
                                                })
                                        )
                                )
                                .then(Commands.literal("set")
                                        .then(Commands.literal("runner")
                                                .then(Commands.argument("player", EntityArgument.player())
                                                        .executes(c -> {
                                                            RunnerHunterGame game = RunnerHunterGame.getGame(UUID.fromString(StringArgumentType.getString(c, "game")));
                                                            if (game == null) {
                                                                c.getSource().sendFailure(new TextComponent("§cInvalid game!"));
                                                                return 0;
                                                            }

                                                            ServerPlayer player = EntityArgument.getPlayer(c, "player");
                                                            RunnerHunterGame playerGame = PlayerDataManager.get(player).runnerHunterGame;

                                                            if(playerGame != null && !playerGame.equals(game)) {
                                                                c.getSource().sendFailure(new TextComponent("§c" + player.getScoreboardName() + " is already in a game!"));
                                                                return 0;
                                                            }

                                                            AccuratePlayer accuratePlayer = AccuratePlayer.create(player);

                                                            if(!game.setRunner(accuratePlayer, game.hunters.contains(accuratePlayer), true)) {
                                                                c.getSource().sendFailure(new TextComponent("§c" + player.getScoreboardName() + " is already the runner!"));
                                                                return 0;
                                                            }

                                                            c.getSource().sendSuccess(new TextComponent("§cGame §4" + game.gameID + "§c's runner has been set to " + player.getScoreboardName()), true);

                                                            return 1;
                                                        })
                                                )
                                        )
                                        .then(Commands.literal("level")
                                                .then(Commands.literal("dimension")
                                                        .then(Commands.argument("dimension", DimensionArgument.dimension())
                                                                .executes(c -> {
                                                                    RunnerHunterGame game = RunnerHunterGame.getGame(UUID.fromString(StringArgumentType.getString(c, "game")));
                                                                    if (game == null) {
                                                                        c.getSource().sendFailure(new TextComponent("§cInvalid game!"));
                                                                        return 0;
                                                                    }

                                                                    if (game.hasStarted) {
                                                                        c.getSource().sendFailure(new TextComponent("§cYou cannot modify a game that's already started!"));
                                                                        return 0;
                                                                    }

                                                                    ServerLevel level = DimensionArgument.getDimension(c, "dimension");
                                                                    if(game.config.level.world.equals(level)) {
                                                                        c.getSource().sendFailure(new TextComponent("§cThat is already the dimension!"));
                                                                        return 0;
                                                                    }

                                                                    game.config.level.world = level;

                                                                    c.getSource().sendSuccess(new TextComponent("§cGame §4" + game.gameID + "§c's dimension has been set to " + WorldUtil.getWorldName(level)), true);

                                                                    return 1;
                                                                })
                                                        )
                                                )
                                                .then(Commands.literal("runnerPosition")
                                                        .then(Commands.argument("position", Vec3Argument.vec3())
                                                                .executes(c -> {
                                                                    RunnerHunterGame game = RunnerHunterGame.getGame(UUID.fromString(StringArgumentType.getString(c, "game")));
                                                                    if (game == null) {
                                                                        c.getSource().sendFailure(new TextComponent("§cInvalid game!"));
                                                                        return 0;
                                                                    }

                                                                    if (game.hasStarted) {
                                                                        c.getSource().sendFailure(new TextComponent("§cYou cannot modify a game that's already started!"));
                                                                        return 0;
                                                                    }

                                                                    Vec3 position = Vec3Argument.getVec3(c, "position");


                                                                    game.config.level.runnerPos = new EntityPos(position);
                                                                    c.getSource().sendSuccess(new TextComponent("§cGame §4" + game.gameID + "§c's runner spawn position has been set to " + position), true);

                                                                    return 1;
                                                                })
                                                        )
                                                )
                                                .then(Commands.literal("hunterPosition")
                                                        .then(Commands.argument("position", Vec3Argument.vec3())
                                                                .executes(c -> {
                                                                    RunnerHunterGame game = RunnerHunterGame.getGame(UUID.fromString(StringArgumentType.getString(c, "game")));
                                                                    if (game == null) {
                                                                        c.getSource().sendFailure(new TextComponent("§cInvalid game!"));
                                                                        return 0;
                                                                    }

                                                                    if (game.hasStarted) {
                                                                        c.getSource().sendFailure(new TextComponent("§cYou cannot modify a game that's already started!"));
                                                                        return 0;
                                                                    }

                                                                    Vec3 position = Vec3Argument.getVec3(c, "position");


                                                                    game.config.level.huntersPos = new EntityPos(position);
                                                                    c.getSource().sendSuccess(new TextComponent("§cGame §4" + game.gameID + "§c's hunter spawn position has been set to " + position), true);

                                                                    return 1;
                                                                })
                                                        )
                                                )
                                        )
                                )
                                .then(Commands.literal("add")
                                        .then(Commands.literal("hunter")
                                                .then(Commands.argument("player", EntityArgument.player())
                                                        .executes(c -> {
                                                            RunnerHunterGame game = RunnerHunterGame.getGame(UUID.fromString(StringArgumentType.getString(c, "game")));
                                                            if (game == null) {
                                                                c.getSource().sendFailure(new TextComponent("§cInvalid game!"));
                                                                return 0;
                                                            }

                                                            ServerPlayer player = EntityArgument.getPlayer(c, "player");
                                                            RunnerHunterGame playerGame = PlayerDataManager.get(player).runnerHunterGame;

                                                            if(playerGame != null && !playerGame.equals(game)) {
                                                                c.getSource().sendFailure(new TextComponent("§c" + player.getScoreboardName() + " is already in a game!"));
                                                                return 0;
                                                            }

                                                            AccuratePlayer accuratePlayer = AccuratePlayer.create(player);

                                                            if(!game.addHunter(accuratePlayer, game.runner.equals(accuratePlayer))) {
                                                                c.getSource().sendFailure(new TextComponent("§c" + player.getScoreboardName() + " is already a hunter!"));
                                                                return 0;
                                                            }

                                                            c.getSource().sendSuccess(new TextComponent("§cGame §4" + game.gameID + "§c's hunter has been added to " + player.getScoreboardName()), true);

                                                            return 1;
                                                        })
                                                )
                                        )
                                )
                                .then(Commands.literal("remove")
                                        .then(Commands.literal("runner")
                                                .then(Commands.argument("player", EntityArgument.player())
                                                        .executes(c -> {
                                                            RunnerHunterGame game = RunnerHunterGame.getGame(UUID.fromString(StringArgumentType.getString(c, "game")));
                                                            if (game == null) {
                                                                c.getSource().sendFailure(new TextComponent("§cInvalid game!"));
                                                                return 0;
                                                            }

                                                            ServerPlayer player = EntityArgument.getPlayer(c, "player");
                                                            RunnerHunterGame playerGame = PlayerDataManager.get(player).runnerHunterGame;

                                                            if(playerGame != null && !playerGame.equals(game)) {
                                                                c.getSource().sendFailure(new TextComponent("§c" + player.getScoreboardName() + " is already in a game!"));
                                                                return 0;
                                                            }

                                                            AccuratePlayer accuratePlayer = AccuratePlayer.create(player);

                                                            if(!game.runner.equals(accuratePlayer)) {
                                                                c.getSource().sendFailure(new TextComponent("§c" + player.getScoreboardName() + " is not the runner!"));
                                                                return 0;
                                                            }

                                                            game.runner = null;
                                                            RunnerHunterGame.leave(player);

                                                            c.getSource().sendSuccess(new TextComponent("§cGame §4" + game.gameID + "§c's runner has been set to no one"), true);

                                                            return 1;
                                                        })
                                                )
                                        )
                                        .then(Commands.literal("hunter")
                                                .then(Commands.argument("player", EntityArgument.player())
                                                        .executes(c -> {
                                                            RunnerHunterGame game = RunnerHunterGame.getGame(UUID.fromString(StringArgumentType.getString(c, "game")));
                                                            if (game == null) {
                                                                c.getSource().sendFailure(new TextComponent("§cInvalid game!"));
                                                                return 0;
                                                            }

                                                            ServerPlayer player = EntityArgument.getPlayer(c, "player");
                                                            RunnerHunterGame playerGame = PlayerDataManager.get(player).runnerHunterGame;

                                                            if(playerGame != null && !playerGame.equals(game)) {
                                                                c.getSource().sendFailure(new TextComponent("§c" + player.getScoreboardName() + " is already in a game!"));
                                                                return 0;
                                                            }

                                                            AccuratePlayer accuratePlayer = AccuratePlayer.create(player);

                                                            if(!game.hunters.contains(accuratePlayer)) {
                                                                c.getSource().sendFailure(new TextComponent("§c" + player.getScoreboardName() + " is not a hunter!"));
                                                                return 0;
                                                            }

                                                            game.hunters.remove(accuratePlayer);
                                                            RunnerHunterGame.leave(player);

                                                            c.getSource().sendSuccess(new TextComponent("§cGame §4" + game.gameID + "§c's hunter " + player.getScoreboardName() + " has been removed"), true);

                                                            return 1;
                                                        })
                                                )
                                        )
                                )
                                .then(Commands.literal("option")
                                        .then(Commands.literal("onehit")
                                                .then(Commands.argument("value", BoolArgumentType.bool())
                                                        .executes(c -> {
                                                            RunnerHunterGame game = RunnerHunterGame.getGame(UUID.fromString(StringArgumentType.getString(c, "game")));
                                                            if(game == null) {
                                                                c.getSource().sendFailure(new TextComponent("§cInvalid game!"));
                                                                return 0;
                                                            }

                                                            if(game.hasStarted) {
                                                                c.getSource().sendFailure(new TextComponent("§cYou cannot modify a game that's already started!"));
                                                                return 0;
                                                            }

                                                            boolean value = BoolArgumentType.getBool(c, "value");

                                                            game.config.isOneHit = value;

                                                            c.getSource().sendSuccess(new TextComponent("§cGame §4" + game.gameID + "§c's option OneHit was " + ((value) ? "Enabled" : "Disabled")), true);

                                                            return 1;
                                                        })
                                                )
                                        )
                                        .then(Commands.literal("glow")
                                                .then(Commands.literal("runner")
                                                        .then(Commands.argument("value", BoolArgumentType.bool())
                                                                .executes(c -> {
                                                                    RunnerHunterGame game = RunnerHunterGame.getGame(UUID.fromString(StringArgumentType.getString(c, "game")));
                                                                    if (game == null) {
                                                                        c.getSource().sendFailure(new TextComponent("§cInvalid game!"));
                                                                        return 0;
                                                                    }

                                                                    if (game.hasStarted) {
                                                                        c.getSource().sendFailure(new TextComponent("§cYou cannot modify a game that's already started!"));
                                                                        return 0;
                                                                    }

                                                                    boolean value = BoolArgumentType.getBool(c, "value");

                                                                    game.config.glowRunner = value;

                                                                    c.getSource().sendSuccess(new TextComponent("§cGame §4" + game.gameID + "§c's option Glow Runner was " + ((value) ? "Enabled" : "Disabled")), true);

                                                                    return 1;
                                                                })
                                                        )
                                                )
                                                .then(Commands.literal("hunters")
                                                        .then(Commands.argument("value", BoolArgumentType.bool())
                                                                .executes(c -> {
                                                                    RunnerHunterGame game = RunnerHunterGame.getGame(UUID.fromString(StringArgumentType.getString(c, "game")));
                                                                    if (game == null) {
                                                                        c.getSource().sendFailure(new TextComponent("§cInvalid game!"));
                                                                        return 0;
                                                                    }

                                                                    if (game.hasStarted) {
                                                                        c.getSource().sendFailure(new TextComponent("§cYou cannot modify a game that's already started!"));
                                                                        return 0;
                                                                    }

                                                                    boolean value = BoolArgumentType.getBool(c, "value");

                                                                    game.config.glowHunters = value;

                                                                    c.getSource().sendSuccess(new TextComponent("§cGame §4" + game.gameID + "§c's option Glow Hunters was " + ((value) ? "Enabled" : "Disabled")), true);

                                                                    return 1;
                                                                })
                                                        )
                                                )
                                        )
                                )
                        )
                )
        );
    }
}
