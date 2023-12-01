package net.notcoded.runnerhunter.utilities;

import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.notcoded.codelib.util.world.WorldUtil;
import net.notcoded.runnerhunter.game.RunnerHunterGame;
import net.notcoded.runnerhunter.utilities.player.PlayerDataManager;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;

public class RunnerHunterUtil {

    public static String mainColor = "§3"; // cyan
    public static String secondaryColor = "§6"; // gold

    public static boolean isRunnerHunter(ServerPlayer player) {
        RunnerHunterGame game = PlayerDataManager.get(player).runnerHunterGame;
        return game != null && game.hasStarted && game.time > 0;
    }

    public static ArrayList<String> getListOfInventories() {
        String inventories = "";
        File folder = new File(FabricLoader.getInstance().getConfigDir() + File.separator + "saveandloadinventories");
        ArrayList<String> list = new ArrayList<>();
        if (!folder.isDirectory()) {
            return new ArrayList<>(Collections.singletonList(inventories));
        } else {
            File[] listOfFiles = folder.listFiles();
            if (listOfFiles.length == 0) {
                return new ArrayList<>(Collections.singletonList(inventories));
            } else {
                for (File listOfFile : listOfFiles) {
                    if (listOfFile.isFile()) list.add(listOfFile.getName().replace(".txt", ""));
                }
                return list;
            }
        }
    }


    public enum ErrorType {
        NO_HUNTER,
        ALREADY_HUNTER,
        ALREADY_RUNNER,
        NO_RUNNER,
        ALREADY_IN_GAME,
        ALREADY_DIMENSION,
        INVALID_GAME,
        MODIFY_STARTED_GAME,

        NOT_RUNNER,

        NOT_HUNTER
    }

    public static TextComponent returnMessage(ErrorType type, @Nullable Object object) {
        if(type.equals(ErrorType.ALREADY_HUNTER)) return new TextComponent(String.format("%s%s %sis already a hunter!", secondaryColor, ((ServerPlayer) object).getScoreboardName(), mainColor));
        if(type.equals(ErrorType.ALREADY_RUNNER)) return new TextComponent(String.format("%s%s %sis already the runner!", secondaryColor, ((ServerPlayer) object).getScoreboardName(), mainColor));

        if(type.equals(ErrorType.NO_RUNNER)) return new TextComponent(String.format("%sThe runner is not set!", mainColor));
        if(type.equals(ErrorType.NO_HUNTER)) return new TextComponent(String.format("%sThere are no hunters!", mainColor));

        if(type.equals(ErrorType.NOT_RUNNER)) return new TextComponent(String.format("%s%s %sis not the runner!", secondaryColor, ((ServerPlayer) object).getScoreboardName(), mainColor));
        if(type.equals(ErrorType.NOT_HUNTER)) return new TextComponent(String.format("%s%s %sis not one of the hunters!", secondaryColor, ((ServerPlayer) object).getScoreboardName(), mainColor));

        if(type.equals(ErrorType.ALREADY_IN_GAME)) return new TextComponent(String.format("%s%s %sis already in a game!", secondaryColor, ((ServerPlayer) object).getScoreboardName(), mainColor));
        if(type.equals(ErrorType.ALREADY_DIMENSION)) return new TextComponent(String.format("%s%s %sis already the dimension!", secondaryColor, WorldUtil.getWorldName((ServerLevel) object), mainColor));

        if(type.equals(ErrorType.INVALID_GAME)) return new TextComponent(String.format("%sInvalid game! Create a game using /runnerhunter create.", mainColor));
        if(type.equals(ErrorType.MODIFY_STARTED_GAME)) return new TextComponent(String.format("%sYou cannot modify a game that's already started!", mainColor));


        return null;
    }
}
