package net.notcoded.runnerhunter.game;

import net.notcoded.runnerhunter.game.level.RunnerHunterLevel;
import org.jetbrains.annotations.NotNull;

public class GameConfiguration {
    public boolean isOneHit;
    public RunnerHunterLevel level;
    public int time;

    public boolean glowHunters;

    public boolean glowRunner;

    public GameConfiguration(boolean glowHunters, boolean glowRunner, boolean isOneHit, int time, @NotNull RunnerHunterLevel level) {
        this.isOneHit = isOneHit;
        this.level = level;
        this.time = time;
        this.glowHunters = glowHunters;
        this.glowRunner = glowRunner;
    }
}
