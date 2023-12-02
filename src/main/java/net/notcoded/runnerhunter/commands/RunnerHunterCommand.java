package net.notcoded.runnerhunter.commands;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.natamus.saveandloadinventories.util.Util;
import me.lucko.fabric.api.permissions.v0.Permissions;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.commands.arguments.DimensionArgument;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.commands.arguments.coordinates.Vec3Argument;
import net.minecraft.network.chat.*;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.phys.Vec3;
import net.notcoded.codelib.players.AccuratePlayer;
import net.notcoded.codelib.util.pos.EntityPos;
import net.notcoded.codelib.util.world.WorldUtil;
import net.notcoded.runnerhunter.RunnerHunter;
import net.notcoded.runnerhunter.game.GameConfiguration;
import net.notcoded.runnerhunter.game.RunnerHunterGame;
import net.notcoded.runnerhunter.game.level.RunnerHunterLevel;
import net.notcoded.runnerhunter.utilities.RunnerHunterUtil;
import net.notcoded.runnerhunter.utilities.player.PlayerDataManager;

import java.util.ArrayList;
import java.util.UUID;
import java.util.stream.Stream;

import static net.notcoded.runnerhunter.utilities.RunnerHunterUtil.mainColor;
import static net.notcoded.runnerhunter.utilities.RunnerHunterUtil.secondaryColor;

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
                    c.getSource().sendFailure(new TextComponent(mainColor + "Invalid syntax!"));
                    return 0;
                })
                .then(Commands.literal("create")
                        .executes(c -> {

                            ServerPlayer player = c.getSource().getPlayerOrException();
                            RunnerHunterGame game = new RunnerHunterGame(AccuratePlayer.create(player), new ArrayList<>(), new GameConfiguration(false, false, false, false, 250, new RunnerHunterLevel(player.getLevel(), new EntityPos(player.position()), new EntityPos(player.position().add(0, 0, 10)))));

                            c.getSource().sendSuccess(new TextComponent(String.format("%sCreated game with id %s%s%s.", mainColor, secondaryColor, game.gameID, mainColor)), true);

                            return Command.SINGLE_SUCCESS;
                        }))
                .then(Commands.literal("game")
                        .then(Commands.argument("game", StringArgumentType.string())
                                .suggests(((context, builder) -> SharedSuggestionProvider.suggest((getGames()), builder)))
                                .then(Commands.literal("start")
                                        .executes(c -> {
                                            RunnerHunterGame game = RunnerHunterGame.getGame(UUID.fromString(StringArgumentType.getString(c, "game")));
                                            if(game == null) {
                                                c.getSource().sendFailure(RunnerHunterUtil.returnMessage(RunnerHunterUtil.ErrorType.INVALID_GAME, null));
                                                return 0;
                                            }

                                            if(game.hasStarted) {
                                                c.getSource().sendFailure(RunnerHunterUtil.returnMessage(RunnerHunterUtil.ErrorType.MODIFY_STARTED_GAME, null));
                                                return 0;
                                            }

                                            if(game.hunters.isEmpty()) {
                                                c.getSource().sendFailure(new TextComponent(mainColor + "There must be atleast 1 hunter for the game to start!"));
                                                return 0;
                                            }

                                            if(game.runner == null || game.runner.get() == null) {
                                                c.getSource().sendFailure(RunnerHunterUtil.returnMessage(RunnerHunterUtil.ErrorType.NO_RUNNER, null));
                                                return 0;
                                            }

                                            game.startGame();
                                            c.getSource().sendSuccess(new TextComponent(String.format("%sGame %s%s %shas been started!", mainColor, secondaryColor, game.gameID, mainColor)), true);

                                            return 1;
                                        })
                                )
                                .then(Commands.literal("delete")
                                        .executes(c -> {
                                            RunnerHunterGame game = RunnerHunterGame.getGame(UUID.fromString(StringArgumentType.getString(c, "game")));
                                            if(game == null) {
                                                c.getSource().sendFailure(RunnerHunterUtil.returnMessage(RunnerHunterUtil.ErrorType.INVALID_GAME, null));
                                                return 0;
                                            }

                                            if(game.hasStarted) {
                                                try {
                                                    game.shouldWait = false;
                                                    game.endGame();
                                                    game.second();
                                                } catch (Exception ignored) { } // whoops
                                            }

                                            game.removeBossbar();
                                            RunnerHunterGame.games.remove(game);
                                            c.getSource().sendSuccess(new TextComponent(String.format("%sGame %s%s %shas been deleted!", mainColor, secondaryColor, game.gameID, mainColor)), true);

                                            return 1;
                                        })
                                )
                                .then(Commands.literal("reset")
                                        .then(Commands.literal("runner")
                                                .executes(c -> {
                                                    RunnerHunterGame game = RunnerHunterGame.getGame(UUID.fromString(StringArgumentType.getString(c, "game")));
                                                    if (game == null) {
                                                        c.getSource().sendFailure(RunnerHunterUtil.returnMessage(RunnerHunterUtil.ErrorType.INVALID_GAME, null));
                                                        return 0;
                                                    }

                                                    if (game.hasStarted) {
                                                        c.getSource().sendFailure(RunnerHunterUtil.returnMessage(RunnerHunterUtil.ErrorType.MODIFY_STARTED_GAME, null));
                                                        return 0;
                                                    }

                                                    if(game.runner == null) {
                                                        c.getSource().sendFailure(RunnerHunterUtil.returnMessage(RunnerHunterUtil.ErrorType.NO_RUNNER, null));
                                                        return 0;
                                                    }

                                                    game.runner = null;
                                                    c.getSource().sendSuccess(new TextComponent(String.format("%sGame %s%s%s's runner has been reset.", mainColor, secondaryColor, game.gameID, mainColor)), true);


                                                    return 1;
                                                })
                                        )
                                        .then(Commands.literal("hunters")
                                                .executes(c -> {
                                                    RunnerHunterGame game = RunnerHunterGame.getGame(UUID.fromString(StringArgumentType.getString(c, "game")));
                                                    if (game == null) {
                                                        c.getSource().sendFailure(RunnerHunterUtil.returnMessage(RunnerHunterUtil.ErrorType.INVALID_GAME, null));
                                                        return 0;
                                                    }

                                                    if (game.hasStarted) {
                                                        c.getSource().sendFailure(RunnerHunterUtil.returnMessage(RunnerHunterUtil.ErrorType.MODIFY_STARTED_GAME, null));
                                                        return 0;
                                                    }

                                                    if(game.hunters.isEmpty()) {
                                                        c.getSource().sendFailure(RunnerHunterUtil.returnMessage(RunnerHunterUtil.ErrorType.NO_HUNTER, null));
                                                        return 0;
                                                    }

                                                    game.hunters = new ArrayList<>();
                                                    c.getSource().sendSuccess(new TextComponent(String.format("%sGame %s%s%s's hunters has been reset.", mainColor, secondaryColor, game.gameID, mainColor)), true);

                                                    return 1;
                                                })
                                        )
                                )
                                .then(Commands.literal("list")
                                        .then(Commands.literal("runner")
                                                .executes(c -> {
                                                    RunnerHunterGame game = RunnerHunterGame.getGame(UUID.fromString(StringArgumentType.getString(c, "game")));
                                                    if (game == null) {
                                                        c.getSource().sendFailure(RunnerHunterUtil.returnMessage(RunnerHunterUtil.ErrorType.INVALID_GAME, null));
                                                        return 0;
                                                    }


                                                    if(game.runner == null) {
                                                        c.getSource().sendFailure(RunnerHunterUtil.returnMessage(RunnerHunterUtil.ErrorType.NO_RUNNER, null));
                                                        return 0;
                                                    }

                                                    c.getSource().sendSuccess(new TextComponent(String.format("%sGame %s%s%s's runner is: %s%s", mainColor, secondaryColor, game.gameID, mainColor, secondaryColor, game.runner.get().getScoreboardName())), false);

                                                    return 1;
                                                })
                                        )
                                        .then(Commands.literal("hunters")
                                                .executes(c -> {
                                                    RunnerHunterGame game = RunnerHunterGame.getGame(UUID.fromString(StringArgumentType.getString(c, "game")));
                                                    if (game == null) {
                                                        c.getSource().sendFailure(RunnerHunterUtil.returnMessage(RunnerHunterUtil.ErrorType.INVALID_GAME, null));
                                                        return 0;
                                                    }

                                                    if(game.hunters.isEmpty()) {
                                                        c.getSource().sendFailure(RunnerHunterUtil.returnMessage(RunnerHunterUtil.ErrorType.NO_HUNTER, null));
                                                        return 0;
                                                    }

                                                    c.getSource().sendSuccess(new TextComponent(String.format("%sGame %s%s%s's hunters are:", mainColor, secondaryColor, game.gameID, mainColor)), false);
                                                    for (AccuratePlayer accuratePlayer : game.hunters) {
                                                        c.getSource().sendSuccess(new TextComponent(String.format("%s» %s%s", mainColor, secondaryColor, accuratePlayer.get().getScoreboardName())), false);
                                                    }


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
                                                                c.getSource().sendFailure(RunnerHunterUtil.returnMessage(RunnerHunterUtil.ErrorType.INVALID_GAME, null));
                                                                return 0;
                                                            }

                                                            ServerPlayer player = EntityArgument.getPlayer(c, "player");
                                                            RunnerHunterGame playerGame = PlayerDataManager.get(player).runnerHunterGame;

                                                            if(playerGame != null && !playerGame.equals(game)) {
                                                                c.getSource().sendFailure(RunnerHunterUtil.returnMessage(RunnerHunterUtil.ErrorType.ALREADY_IN_GAME, player));
                                                                return 0;
                                                            }

                                                            AccuratePlayer accuratePlayer = AccuratePlayer.create(player);

                                                            if(game.runner == null && !game.hasStarted) {
                                                                game.hunters.remove(accuratePlayer);
                                                                game.runner = accuratePlayer;
                                                            } else if(!game.setRunner(accuratePlayer, game.hunters.contains(accuratePlayer), true)) {
                                                                c.getSource().sendFailure(RunnerHunterUtil.returnMessage(RunnerHunterUtil.ErrorType.ALREADY_RUNNER, player));
                                                                return 0;
                                                            }

                                                            c.getSource().sendSuccess(new TextComponent(String.format("%sGame %s%s%s's runner has been set to %s%s", mainColor, secondaryColor, game.gameID, mainColor, secondaryColor, player.getScoreboardName())), true);

                                                            return 1;
                                                        })
                                                )
                                        )
                                        .then(Commands.literal("time")
                                                .then(Commands.argument("seconds", IntegerArgumentType.integer(10))
                                                        .executes(c -> {
                                                            RunnerHunterGame game = RunnerHunterGame.getGame(UUID.fromString(StringArgumentType.getString(c, "game")));
                                                            if (game == null) {
                                                                c.getSource().sendFailure(RunnerHunterUtil.returnMessage(RunnerHunterUtil.ErrorType.INVALID_GAME, null));
                                                                return 0;
                                                            }

                                                            if (game.hasStarted) {
                                                                c.getSource().sendFailure(RunnerHunterUtil.returnMessage(RunnerHunterUtil.ErrorType.MODIFY_STARTED_GAME, null));
                                                                return 0;
                                                            }

                                                            int time = IntegerArgumentType.getInteger(c, "seconds");

                                                            game.config.maxSeconds = time;

                                                            c.getSource().sendSuccess(new TextComponent(String.format("%sGame %s%s%s's max seconds has been set to %s%s %sseconds.", mainColor, secondaryColor, game.gameID, mainColor, secondaryColor, time, mainColor)), true);

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
                                                                        c.getSource().sendFailure(RunnerHunterUtil.returnMessage(RunnerHunterUtil.ErrorType.INVALID_GAME, null));
                                                                        return 0;
                                                                    }

                                                                    if (game.hasStarted) {
                                                                        c.getSource().sendFailure(RunnerHunterUtil.returnMessage(RunnerHunterUtil.ErrorType.MODIFY_STARTED_GAME, null));
                                                                        return 0;
                                                                    }

                                                                    ServerLevel level = DimensionArgument.getDimension(c, "dimension");
                                                                    if(game.config.level.world.equals(level)) {
                                                                        c.getSource().sendFailure(RunnerHunterUtil.returnMessage(RunnerHunterUtil.ErrorType.ALREADY_DIMENSION, level));
                                                                        return 0;
                                                                    }

                                                                    game.config.level.world = level;

                                                                    c.getSource().sendSuccess(new TextComponent(String.format("%sGame %s%s%s's dimension has been set to %s%s%s.", mainColor, secondaryColor, game.gameID, mainColor, secondaryColor, WorldUtil.getWorldName(level), mainColor)), true);

                                                                    return 1;
                                                                })
                                                        )
                                                )
                                                .then(Commands.literal("runnerPosition")
                                                        .then(Commands.argument("position", Vec3Argument.vec3())
                                                                .executes(c -> {
                                                                    RunnerHunterGame game = RunnerHunterGame.getGame(UUID.fromString(StringArgumentType.getString(c, "game")));
                                                                    if (game == null) {
                                                                        c.getSource().sendFailure(RunnerHunterUtil.returnMessage(RunnerHunterUtil.ErrorType.INVALID_GAME, null));
                                                                        return 0;
                                                                    }

                                                                    if (game.hasStarted) {
                                                                        c.getSource().sendFailure(RunnerHunterUtil.returnMessage(RunnerHunterUtil.ErrorType.MODIFY_STARTED_GAME, null));
                                                                        return 0;
                                                                    }

                                                                    Vec3 position = Vec3Argument.getVec3(c, "position");


                                                                    game.config.level.runnerPos = new EntityPos(position);
                                                                    c.getSource().sendSuccess(new TextComponent(String.format("%sGame %s%s%s's runner spawn position has been set to %s%s%s.", mainColor, secondaryColor, game.gameID, mainColor, secondaryColor, position, mainColor)), true);


                                                                    return 1;
                                                                })
                                                        )
                                                )
                                                .then(Commands.literal("hunterPosition")
                                                        .then(Commands.argument("position", Vec3Argument.vec3())
                                                                .executes(c -> {
                                                                    RunnerHunterGame game = RunnerHunterGame.getGame(UUID.fromString(StringArgumentType.getString(c, "game")));
                                                                    if (game == null) {
                                                                        c.getSource().sendFailure(RunnerHunterUtil.returnMessage(RunnerHunterUtil.ErrorType.INVALID_GAME, null));
                                                                        return 0;
                                                                    }

                                                                    if (game.hasStarted) {
                                                                        c.getSource().sendFailure(RunnerHunterUtil.returnMessage(RunnerHunterUtil.ErrorType.MODIFY_STARTED_GAME, null));
                                                                        return 0;
                                                                    }

                                                                    Vec3 position = Vec3Argument.getVec3(c, "position");


                                                                    game.config.level.huntersPos = new EntityPos(position);
                                                                    c.getSource().sendSuccess(new TextComponent(String.format("%sGame %s%s%s's hunter spawn position has been set to %s%s%s.", mainColor, secondaryColor, game.gameID, mainColor, secondaryColor, position, mainColor)), true);

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
                                                                c.getSource().sendFailure(RunnerHunterUtil.returnMessage(RunnerHunterUtil.ErrorType.INVALID_GAME, null));
                                                                return 0;
                                                            }

                                                            ServerPlayer player = EntityArgument.getPlayer(c, "player");
                                                            RunnerHunterGame playerGame = PlayerDataManager.get(player).runnerHunterGame;

                                                            if(playerGame != null && !playerGame.equals(game)) {
                                                                c.getSource().sendFailure(RunnerHunterUtil.returnMessage(RunnerHunterUtil.ErrorType.ALREADY_IN_GAME, player));
                                                                return 0;
                                                            }

                                                            if(game.runner == null) {
                                                                c.getSource().sendFailure(RunnerHunterUtil.returnMessage(RunnerHunterUtil.ErrorType.NO_RUNNER, player));
                                                                return 0;
                                                            }

                                                            AccuratePlayer accuratePlayer = AccuratePlayer.create(player);
                                                            if(!game.addHunter(accuratePlayer, game.runner.equals(accuratePlayer))) {
                                                                c.getSource().sendFailure(RunnerHunterUtil.returnMessage(RunnerHunterUtil.ErrorType.ALREADY_HUNTER, player));
                                                                return 0;
                                                            }

                                                            c.getSource().sendSuccess(new TextComponent(String.format("%s%s %shas been added to Game %s%s%s's hunters.", secondaryColor, player.getScoreboardName(), mainColor, secondaryColor, game.gameID, mainColor)), true);

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
                                                                c.getSource().sendFailure(RunnerHunterUtil.returnMessage(RunnerHunterUtil.ErrorType.INVALID_GAME, null));
                                                                return 0;
                                                            }

                                                            ServerPlayer player = EntityArgument.getPlayer(c, "player");
                                                            RunnerHunterGame playerGame = PlayerDataManager.get(player).runnerHunterGame;

                                                            if(playerGame != null && !playerGame.equals(game)) {
                                                                c.getSource().sendFailure(RunnerHunterUtil.returnMessage(RunnerHunterUtil.ErrorType.ALREADY_IN_GAME, player));
                                                                return 0;
                                                            }

                                                            AccuratePlayer accuratePlayer = AccuratePlayer.create(player);

                                                            if(!game.runner.equals(accuratePlayer)) {
                                                                c.getSource().sendFailure(RunnerHunterUtil.returnMessage(RunnerHunterUtil.ErrorType.NOT_RUNNER, player));
                                                                return 0;
                                                            }

                                                            game.runner = null;
                                                            if(game.hasStarted) RunnerHunterGame.leave(player);

                                                            c.getSource().sendSuccess(new TextComponent(String.format("%sGame %s%s%s's runner has been set to %sno one%s", mainColor, secondaryColor, game.gameID, mainColor, secondaryColor, mainColor)), true);


                                                            return 1;
                                                        })
                                                )
                                        )
                                        .then(Commands.literal("hunter")
                                                .then(Commands.argument("player", EntityArgument.player())
                                                        .executes(c -> {
                                                            RunnerHunterGame game = RunnerHunterGame.getGame(UUID.fromString(StringArgumentType.getString(c, "game")));
                                                            if (game == null) {
                                                                c.getSource().sendFailure(RunnerHunterUtil.returnMessage(RunnerHunterUtil.ErrorType.INVALID_GAME, null));
                                                                return 0;
                                                            }

                                                            ServerPlayer player = EntityArgument.getPlayer(c, "player");
                                                            RunnerHunterGame playerGame = PlayerDataManager.get(player).runnerHunterGame;

                                                            if(playerGame != null && !playerGame.equals(game)) {
                                                                c.getSource().sendFailure(RunnerHunterUtil.returnMessage(RunnerHunterUtil.ErrorType.ALREADY_IN_GAME, player));
                                                                return 0;
                                                            }

                                                            AccuratePlayer accuratePlayer = AccuratePlayer.create(player);

                                                            if(!game.hunters.contains(accuratePlayer)) {
                                                                c.getSource().sendFailure(RunnerHunterUtil.returnMessage(RunnerHunterUtil.ErrorType.NOT_HUNTER, player));
                                                                return 0;
                                                            }

                                                            game.hunters.remove(accuratePlayer);
                                                            if(game.hasStarted) RunnerHunterGame.leave(player);

                                                            c.getSource().sendSuccess(new TextComponent(String.format("%s%s %shas been removed from Game %s%s%s's hunters.", secondaryColor, player.getScoreboardName(), mainColor, secondaryColor, game.gameID, mainColor)), true);


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
                                                                c.getSource().sendFailure(RunnerHunterUtil.returnMessage(RunnerHunterUtil.ErrorType.INVALID_GAME, null));
                                                                return 0;
                                                            }

                                                            if(game.hasStarted) {
                                                                c.getSource().sendFailure(RunnerHunterUtil.returnMessage(RunnerHunterUtil.ErrorType.MODIFY_STARTED_GAME, null));
                                                                return 0;
                                                            }

                                                            boolean value = BoolArgumentType.getBool(c, "value");

                                                            game.config.isOneHit = value;

                                                            c.getSource().sendSuccess(new TextComponent(String.format("%sGame %s%s%s's option OneHit was %s%s%s.", mainColor, secondaryColor, game.gameID, mainColor, secondaryColor, ((value) ? "Enabled" : "Disabled"), mainColor)), true);

                                                            return 1;
                                                        })
                                                )
                                        )
                                        .then(Commands.literal("runner-actionbar-coords")
                                                .then(Commands.argument("value", BoolArgumentType.bool())
                                                        .executes(c -> {
                                                            RunnerHunterGame game = RunnerHunterGame.getGame(UUID.fromString(StringArgumentType.getString(c, "game")));
                                                            if(game == null) {
                                                                c.getSource().sendFailure(RunnerHunterUtil.returnMessage(RunnerHunterUtil.ErrorType.INVALID_GAME, null));
                                                                return 0;
                                                            }

                                                            if(game.hasStarted) {
                                                                c.getSource().sendFailure(RunnerHunterUtil.returnMessage(RunnerHunterUtil.ErrorType.MODIFY_STARTED_GAME, null));
                                                                return 0;
                                                            }

                                                            boolean value = BoolArgumentType.getBool(c, "value");

                                                            game.config.runnerActionbarCoords = value;

                                                            c.getSource().sendSuccess(new TextComponent(String.format("%sGame %s%s%s's option Showing Runner's Coordinates in the Actionbar was %s%s%s.", mainColor, secondaryColor, game.gameID, mainColor, secondaryColor, ((value) ? "Enabled" : "Disabled"), mainColor)), true);

                                                            return 1;
                                                        })
                                                )
                                        )
                                        .then(Commands.literal("inventory")
                                                .requires(commandSourceStack -> RunnerHunter.isInventoryLoadingLoaded)
                                                .then(Commands.argument("name", StringArgumentType.word())
                                                        .suggests(((context, builder) -> SharedSuggestionProvider.suggest((RunnerHunterUtil.getListOfInventories()), builder)))
                                                        .executes(c -> {
                                                            RunnerHunterGame game = RunnerHunterGame.getGame(UUID.fromString(StringArgumentType.getString(c, "game")));
                                                            if(game == null) {
                                                                c.getSource().sendFailure(RunnerHunterUtil.returnMessage(RunnerHunterUtil.ErrorType.INVALID_GAME, null));
                                                                return 0;
                                                            }

                                                            if(game.hasStarted) {
                                                                c.getSource().sendFailure(RunnerHunterUtil.returnMessage(RunnerHunterUtil.ErrorType.MODIFY_STARTED_GAME, null));
                                                                return 0;
                                                            }

                                                            String inventoryname = StringArgumentType.getString(c, "name");

                                                            if(inventoryname.equalsIgnoreCase("reset") || inventoryname.equalsIgnoreCase("none")) {
                                                                game.config.inventoryName = "";
                                                                c.getSource().sendSuccess(new TextComponent(String.format("%sGame %s%s%s's option Inventory Loading Name was %sreset%s.", mainColor, secondaryColor, game.gameID, mainColor, secondaryColor, mainColor)), true);

                                                                return 1;
                                                            }

                                                            if(inventoryname.trim().isEmpty() || Util.getGearStringFromFile(inventoryname).isEmpty()) {
                                                                c.getSource().sendFailure(new TextComponent(mainColor + "The inventory name '" + inventoryname + "' is invalid."));
                                                                return 0;
                                                            }

                                                            game.config.inventoryName = inventoryname;

                                                            c.getSource().sendSuccess(new TextComponent(String.format("%sGame %s%s%s's option Inventory Loading Name has been set to %s'%s'%s.", mainColor, secondaryColor, game.gameID, mainColor, secondaryColor, inventoryname, mainColor)), true);

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
                                                                        c.getSource().sendFailure(RunnerHunterUtil.returnMessage(RunnerHunterUtil.ErrorType.INVALID_GAME, null));
                                                                        return 0;
                                                                    }

                                                                    if (game.hasStarted) {
                                                                        c.getSource().sendFailure(RunnerHunterUtil.returnMessage(RunnerHunterUtil.ErrorType.MODIFY_STARTED_GAME, null));
                                                                        return 0;
                                                                    }

                                                                    boolean value = BoolArgumentType.getBool(c, "value");

                                                                    game.config.glowRunner = value;

                                                                    c.getSource().sendSuccess(new TextComponent(String.format("%sGame %s%s%s's option Glow Runner was %s%s%s.", mainColor, secondaryColor, game.gameID, mainColor, secondaryColor, ((value) ? "Enabled" : "Disabled"), mainColor)), true);

                                                                    return 1;
                                                                })
                                                        )
                                                )
                                                .then(Commands.literal("hunters")
                                                        .then(Commands.argument("value", BoolArgumentType.bool())
                                                                .executes(c -> {
                                                                    RunnerHunterGame game = RunnerHunterGame.getGame(UUID.fromString(StringArgumentType.getString(c, "game")));
                                                                    if (game == null) {
                                                                        c.getSource().sendFailure(RunnerHunterUtil.returnMessage(RunnerHunterUtil.ErrorType.INVALID_GAME, null));
                                                                        return 0;
                                                                    }

                                                                    if (game.hasStarted) {
                                                                        c.getSource().sendFailure(RunnerHunterUtil.returnMessage(RunnerHunterUtil.ErrorType.MODIFY_STARTED_GAME, null));
                                                                        return 0;
                                                                    }

                                                                    boolean value = BoolArgumentType.getBool(c, "value");

                                                                    game.config.glowHunters = value;

                                                                    c.getSource().sendSuccess(new TextComponent(String.format("%sGame %s%s%s's option Glow Hunters was %s%s%s.", mainColor, secondaryColor, game.gameID, mainColor, secondaryColor, ((value) ? "Enabled" : "Disabled"), mainColor)), true);

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
