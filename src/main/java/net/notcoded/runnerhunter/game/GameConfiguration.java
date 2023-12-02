package net.notcoded.runnerhunter.game;

import net.notcoded.runnerhunter.game.level.RunnerHunterLevel;
import org.jetbrains.annotations.NotNull;

public class GameConfiguration {
    public boolean isOneHit;

    public RunnerHunterLevel level;
    public int maxSeconds;

    public boolean glowHunters;

    public boolean glowRunner;

    public boolean runnerActionbarCoords;

    public String inventoryName = "";

    public GameConfiguration(boolean glowHunters, boolean glowRunner, boolean runnerActionbarCoords, boolean isOneHit, int maxSeconds, @NotNull RunnerHunterLevel level) {
        this.isOneHit = isOneHit;
        this.level = level;
        this.maxSeconds = maxSeconds;
        this.runnerActionbarCoords = runnerActionbarCoords;
        this.glowHunters = glowHunters;
        this.glowRunner = glowRunner;
    }
}
