package net.notcoded.runnerhunter.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import me.lucko.fabric.api.permissions.v0.Permissions;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.core.Registry;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.Difficulty;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.dimension.DimensionType;
import net.notcoded.runnerhunter.RunnerHunter;
import net.notcoded.runnerhunter.utilities.RunnerHunterUtil;
import org.apache.commons.io.FileUtils;
import xyz.nucleoid.fantasy.RuntimeWorldConfig;

import java.io.File;

public class MapCommand {
    public static void register(CommandDispatcher<CommandSourceStack> dispatcher, boolean bl) {
        dispatcher.register((Commands.literal("map")
                .requires(Permissions.require("runnerhunter.manage.map", 4))
                .then(Commands.argument("type", StringArgumentType.string())
                        .suggests(((context, builder) -> SharedSuggestionProvider.suggest((new String[]{"delete", "create", "tp"}), builder)))
                        .then(Commands.argument("map", StringArgumentType.greedyString())
                                .executes(MapCommand::run)
                        )
                )
        ));
    }

    private static int run(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        String type = StringArgumentType.getString(context, "type");
        String map = StringArgumentType.getString(context, "map");

        if(type.trim().isEmpty() || map.trim().isEmpty()) {
            context.getSource().sendFailure(new TextComponent(RunnerHunterUtil.mainColor + "Invalid name!"));
            return 1;
        }

        String[] mapname = map.split(":");

        if(type.equalsIgnoreCase("create")){

            ServerLevel level = RunnerHunter.fantasy.getOrOpenPersistentWorld(ResourceKey.create(Registry.DIMENSION_REGISTRY, new ResourceLocation(mapname[0], mapname[1])).location(), (
                    new RuntimeWorldConfig()
                            .setDimensionType(DimensionType.OVERWORLD_LOCATION)
                            .setGenerator(RunnerHunter.server.overworld().getChunkSource().getGenerator())
                            .setDifficulty(Difficulty.EASY)
                            .setGameRule(GameRules.RULE_KEEPINVENTORY, true)
                            .setGameRule(GameRules.RULE_MOBGRIEFING, false)
                            .setGameRule(GameRules.RULE_WEATHER_CYCLE, false)
                            .setGameRule(GameRules.RULE_DAYLIGHT, false)
                            .setGameRule(GameRules.RULE_DO_IMMEDIATE_RESPAWN, true)
                            .setGameRule(GameRules.RULE_DOMOBSPAWNING, false)
                            .setGameRule(GameRules.RULE_SHOWDEATHMESSAGES, true)
                            .setGameRule(GameRules.RULE_FALL_DAMAGE, false)
                            .setGameRule(GameRules.RULE_SPAWN_RADIUS, 0))).asWorld();

            context.getSource().sendSuccess(new TextComponent(RunnerHunterUtil.mainColor + "Created map called " + map), true);

            try {
                context.getSource().getPlayerOrException().teleportTo(level, 0, 80, 0, 0, 0);
            } catch (Exception ignored) { }

            return 1;
        }

        if (type.equalsIgnoreCase("delete")) {
            RunnerHunter.fantasy.getOrOpenPersistentWorld(ResourceKey.create(Registry.DIMENSION_REGISTRY, new ResourceLocation(mapname[0], mapname[1])).location(), null).delete();

            context.getSource().sendSuccess(new TextComponent(RunnerHunterUtil.mainColor + "Deleted map called " + map), true);
            return 1;
        }

        if(type.equalsIgnoreCase("tp")) {
            ServerLevel level = RunnerHunter.fantasy.getOrOpenPersistentWorld(ResourceKey.create(Registry.DIMENSION_REGISTRY, new ResourceLocation(mapname[0], mapname[1])).location(), null).asWorld();
            context.getSource().getPlayerOrException().teleportTo(level, 0, 80, 0, 0, 0);

            context.getSource().sendSuccess(new TextComponent(RunnerHunterUtil.mainColor + "Teleported to map called: " + map), false);

            return 1;
        }
        return 1;
    }
}