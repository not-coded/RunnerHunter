package net.notcoded.runnerhunter.utilities;

import net.minecraft.server.MinecraftServer;
import net.notcoded.runnerhunter.RunnerHunter;
import net.notcoded.runnerhunter.game.RunnerHunterGame;
import xyz.nucleoid.fantasy.Fantasy;

public class ServerUtil {
    public static int totalTickCount = -1;
    public static int totalSecondCount = -1;
    public static void firstTick(MinecraftServer server) {
        RunnerHunter.server = server;
        RunnerHunter.fantasy = Fantasy.get(server);
    }

    public static void everyTick() {
        totalTickCount++;
        if (totalTickCount % 20 == 0) everySecond();
    }
    static void everySecond() {
        totalSecondCount++;
        try {
            for (RunnerHunterGame game : RunnerHunterGame.games) {
                if (game == null) return;
                game.second();
            }
        } catch (Exception ignored) { }
    }
}
