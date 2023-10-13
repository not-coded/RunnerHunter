package net.notcoded.runnerhunter.utilities.player;

import net.notcoded.runnerhunter.game.RunnerHunterGame;

public class PlayerData {

    // Stuff saved into files
    public SavedPlayerData savedData;

    // Stuff not saved in files
    public RunnerHunterGame runnerHunterGame;

    public PlayerData(SavedPlayerData savedData) {
        this.savedData = savedData;

        this.runnerHunterGame = null;
    }

}
