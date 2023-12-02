package net.notcoded.runnerhunter.game;

import net.blumbo.blfscheduler.BlfRunnable;
import net.blumbo.blfscheduler.BlfScheduler;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.protocol.game.ClientboundSetDisplayObjectivePacket;
import net.minecraft.network.protocol.game.ClientboundSetObjectivePacket;
import net.minecraft.network.protocol.game.ClientboundSetScorePacket;
import net.minecraft.server.ServerScoreboard;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.scores.Objective;
import net.minecraft.world.scores.Score;
import net.minecraft.world.scores.Scoreboard;
import net.minecraft.world.scores.criteria.ObjectiveCriteria;
import net.notcoded.codelib.players.AccuratePlayer;
import net.notcoded.runnerhunter.utilities.player.PlayerDataManager;

import static net.notcoded.runnerhunter.utilities.RunnerHunterUtil.mainColor;
import static net.notcoded.runnerhunter.utilities.RunnerHunterUtil.secondaryColor;

public class RunnerHunterScoreboard {
    private String objectiveName;

    public RunnerHunterGame game;
    private static final TextComponent title = new TextComponent(String.format(" %sTime as %s§lRunner ", mainColor, secondaryColor));
    private Scoreboard scoreboard;
    private Objective objective;

    public RunnerHunterScoreboard(RunnerHunterGame game) {
        this.objectiveName = String.valueOf(game.gameID).substring(1, 16);
        this.game = game;

        this.scoreboard = new Scoreboard();
        this.objective = new Objective(
                this.scoreboard, this.objectiveName, ObjectiveCriteria.DUMMY,
                title, ObjectiveCriteria.RenderType.INTEGER);
    }

    public void setUpScoreboard() {
        for (AccuratePlayer player : this.game.getViewers()) {
            sendScoreboard(player.get());
        }
        updateScoreboard();
    }

    public void sendScoreboard(ServerPlayer player) {
        player.connection.send(new ClientboundSetObjectivePacket(this.objective, 0));
        player.connection.send(new ClientboundSetDisplayObjectivePacket(1, this.objective));
    }

    public void updateScoreboard() {
        clearScoreboard();

        try {
            for (AccuratePlayer player : this.game.getViewers()) {
                this.updateTime(player.get(), PlayerDataManager.get(player.get()).timeAsRunner);
                sendLines(player.get());
            }
        } catch (Exception ignored) { } // im lazy and you'll have to deal with it

    }

    public void updateTime(ServerPlayer player, int time) {
        this.scoreboard.getOrCreatePlayerScore(player.getScoreboardName(), this.objective).setScore(time);

        // this fixes the de-sync apparently :+1:
        BlfScheduler.delay(1, new BlfRunnable() {
            @Override
            public void run() {
                sendLines(player);
            }
        });
    }

    private void clearScoreboard() {
        try {
            for (Score score : this.scoreboard.getPlayerScores(this.objective)) {
                for (AccuratePlayer player : this.game.getViewers()) {
                    player.get().connection.send(new ClientboundSetScorePacket(
                            ServerScoreboard.Method.REMOVE, objectiveName, score.getOwner(), score.getScore()));
                }
                this.scoreboard.resetPlayerScore(score.getOwner(), this.objective);
            }
        } catch (Exception ignored) { } // im lazy and you'll have to deal with it
    }

    public void sendLines(ServerPlayer player) {
        for (Score score : this.scoreboard.getPlayerScores(this.objective)) {
            player.connection.send(new ClientboundSetScorePacket(
                    ServerScoreboard.Method.CHANGE, objectiveName, score.getOwner(), score.getScore()));
        }
    }

    public void removeScoreboardFor(ServerPlayer player) {
        player.connection.send(new ClientboundSetDisplayObjectivePacket(1, null));
        this.scoreboard.resetPlayerScore(player.getScoreboardName(), this.objective);
        try {
            BlfScheduler.delay(1, new BlfRunnable() {
                @Override
                public void run() {
                    updateScoreboard();
                }
            });
        } catch (Exception ignored) { } // im lazy and you'll have to deal with it

    }

    public void removeScoreboard() {
        this.scoreboard.removeObjective(this.objective);
        // that easy?
    }
}
