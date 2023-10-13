package net.notcoded.runnerhunter.utilities;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.notcoded.codelib.players.AccuratePlayer;
import net.notcoded.runnerhunter.game.RunnerHunterGame;
import net.notcoded.runnerhunter.utilities.player.PlayerDataManager;

import java.util.ArrayList;
import java.util.Random;

public class RunnerHunterUtil {
    public static boolean isRunnerHunter(ServerPlayer player) {
        RunnerHunterGame game = PlayerDataManager.get(player).runnerHunterGame;
        return game != null && game.hasStarted && game.time > 0;
    }
}
