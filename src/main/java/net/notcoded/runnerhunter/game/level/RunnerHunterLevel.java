package net.notcoded.runnerhunter.game.level;

import net.minecraft.server.level.ServerLevel;
import net.notcoded.codelib.util.pos.EntityPos;
import org.jetbrains.annotations.NotNull;

public class RunnerHunterLevel {

    public ServerLevel world;

    public EntityPos runnerPos;

    public EntityPos huntersPos;

    public RunnerHunterLevel(@NotNull ServerLevel world, @NotNull EntityPos runnerPos, @NotNull EntityPos huntersPos) {
        this.world = world;
        this.runnerPos = runnerPos;
        this.huntersPos = huntersPos;
    }
}
