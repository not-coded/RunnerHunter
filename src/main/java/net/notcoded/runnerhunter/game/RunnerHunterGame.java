package net.notcoded.runnerhunter.game;

import com.natamus.collective_fabric.functions.PlayerFunctions;
import net.minecraft.Util;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.bossevents.CustomBossEvent;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.BossEvent;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.level.GameType;
import net.notcoded.codelib.players.AccuratePlayer;
import net.notcoded.codelib.util.TickUtil;
import net.notcoded.runnerhunter.RunnerHunter;
import net.notcoded.runnerhunter.utilities.player.PlayerData;
import net.notcoded.runnerhunter.utilities.player.PlayerDataManager;
import net.notcoded.runnerhunter.utilities.player.PlayerUtil;
import org.jetbrains.annotations.NotNull;

import java.util.*;

import static net.notcoded.runnerhunter.utilities.RunnerHunterUtil.mainColor;
import static net.notcoded.runnerhunter.utilities.RunnerHunterUtil.secondaryColor;

public class RunnerHunterGame {

    public static ArrayList<RunnerHunterGame> games = new ArrayList<>();

    public UUID gameID;

    public AccuratePlayer runner;

    public ArrayList<AccuratePlayer> hunters;
    public ArrayList<AccuratePlayer> spectators = new ArrayList<>();

    public boolean hasStarted = false;

    public GameConfiguration config;

    // Winner thingie
    public AccuratePlayer winner = null;

    public boolean isEnding;

    public int currentEndTime = 0;

    public boolean shouldWait = false;

    public CustomBossEvent bossbar;
    
    public RunnerHunterScoreboard scoreboard;

    public RunnerHunterGame(@NotNull AccuratePlayer runner, @NotNull ArrayList<AccuratePlayer> hunters, @NotNull GameConfiguration configuration) {
        this.runner = runner;
        this.hunters = hunters;
        this.config = configuration;

        this.gameID = UUID.randomUUID();
        this.bossbar = this.createBossbar();
        this.scoreboard = new RunnerHunterScoreboard(this);

        RunnerHunterGame.games.add(this);
    }

    public boolean failSafe() {
        return (this.hasStarted && this.hunters.isEmpty()) || config == null;
    }

    public static void leave(ServerPlayer player) {
        PlayerData data = PlayerDataManager.get(player);

        player.clearFire();
        player.clearSleepingPos();
        player.inventory.clearContent();
        player.getEnderChestInventory().clearContent();
        player.setGameMode(GameType.ADVENTURE);
        player.setHealth(player.getMaxHealth());
        player.setGlowing(false);
        PlayerUtil.sendBossbar(PlayerDataManager.get(player).runnerHunterGame.bossbar, player, true);
        data.runnerHunterGame.scoreboard.removeScoreboardFor(player);
        player.setRespawnPosition(RunnerHunter.server.overworld().dimension(), RunnerHunter.server.overworld().getSharedSpawnPos(), 0, true, false);
        player.teleportTo(RunnerHunter.server.overworld(), RunnerHunter.server.overworld().getSharedSpawnPos().getX(), RunnerHunter.server.overworld().getSharedSpawnPos().getY(), RunnerHunter.server.overworld().getSharedSpawnPos().getZ(), 0, 0);

        AccuratePlayer accuratePlayer = AccuratePlayer.create(player);
        if(data.runnerHunterGame.hunters.contains(accuratePlayer) || (data.runnerHunterGame.isEnding && data.runnerHunterGame.winner != accuratePlayer)) {
            data.savedData.losses++;
        }

        data.runnerHunterGame.hunters.remove(accuratePlayer);
        if(data.runnerHunterGame.runner.equals(accuratePlayer)) data.runnerHunterGame.runner = null;

        data.timeAsRunner = 0;
        data.runnerHunterGame = null;
    }

    public void second() {
        if(this.failSafe()) {
            this.shouldWait = false;
            this.hasStarted = true;
            if(!this.isEnding) {
                this.endGame();
            }
        }
        if(this.hasStarted){
            if(this.isEnding) {
                this.currentEndTime++;
                if(this.currentEndTime >= 5 || !this.shouldWait) {
                    this.isEnding = false;

                    this.removeBossbar();
                    this.scoreboard.removeScoreboard();

                    for(AccuratePlayer player : this.getViewers()) RunnerHunterGame.leave(player.get());

                    RunnerHunterGame.games.remove(this);
                    return;
                }
            } else {
                this.addRunnerTime();
                this.updateInfo();
            }
        }
    }

    private int addRunnerTime() {
        PlayerData data = PlayerDataManager.get(this.runner.get());
        data.timeAsRunner++;
        if(data.timeAsRunner >= this.config.maxSeconds && !this.isEnding) this.endGame();
        return data.timeAsRunner;
    }

    public boolean setRunner(AccuratePlayer accuratePlayer, boolean inGame, boolean announce) {
        AccuratePlayer oldRunner = this.runner;
        if(!this.switchRoles(this.runner, accuratePlayer)) return false;
        oldRunner.get().setGlowing(this.config.glowHunters);
        ServerPlayer player = accuratePlayer.get();

        if(!inGame) {
            player.setGameMode(GameType.ADVENTURE);
            player.clearFire();
            player.clearSleepingPos();
            player.inventory.clearContent();
            player.getEnderChestInventory().clearContent();
            player.setHealth(player.getMaxHealth());
            player.setGameMode(GameType.ADVENTURE);
            player.setGlowing(this.config.glowRunner);
            player.removeAllEffects();
            player.getFoodData().setFoodLevel(20);
            this.scoreboard.sendScoreboard(player);
            PlayerDataManager.get(player).runnerHunterGame = this;
            this.config.level.runnerPos.teleportPlayer(this.config.level.world, player);
            PlayerDataManager.get(player).timeAsRunner = 0;
            if(RunnerHunter.isInventoryLoadingLoaded && !this.config.inventoryName.isEmpty()) PlayerFunctions.setPlayerGearFromString(player, com.natamus.saveandloadinventories.util.Util.getGearStringFromFile(this.config.inventoryName));
            PlayerUtil.sendBossbar(this.bossbar, player, false);
        } else {
            player.setGlowing(this.config.glowRunner);
            player.setHealth(player.getMaxHealth());
            player.removeAllEffects();
            player.clearFire();
        }

        PlayerDataManager.get(player).savedData.swaps++;


        if(announce) {
            String message = String.format("%s§l%s %sis now the runner! %s[%s%s %s %s%s]", secondaryColor, player.getScoreboardName(), mainColor, secondaryColor, mainColor, player.blockPosition().getX(), player.blockPosition().getY(), player.blockPosition().getZ(), secondaryColor);
            for(AccuratePlayer ap : this.getViewers()) {
                ap.get().sendMessage(new TextComponent(message), Util.NIL_UUID);
            }
        }

        return true;
    }

    public boolean addHunter(AccuratePlayer accuratePlayer, boolean inGame) {
        if(this.hunters.contains(accuratePlayer)) return false;
        int size = this.hunters.size();
        if(size > 0) size = new Random().nextInt(this.hunters.size());

        if(this.runner.equals(accuratePlayer) && !this.switchRoles(runner, this.hunters.get(size))) return false;

        ServerPlayer player = accuratePlayer.get();


        if(!inGame) {
            this.hunters.add(accuratePlayer);
            if(!this.hasStarted) return true;

            player.setGameMode(GameType.ADVENTURE);
            player.clearFire();
            player.clearSleepingPos();
            player.inventory.clearContent();
            player.getEnderChestInventory().clearContent();
            player.setHealth(player.getMaxHealth());
            player.setGameMode(GameType.ADVENTURE);
            player.setGlowing(this.config.glowHunters);
            player.removeAllEffects();
            player.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED, 1000000000, 1, false, false, false));
            this.scoreboard.sendScoreboard(player);
            player.getFoodData().setFoodLevel(20);
            PlayerDataManager.get(player).timeAsRunner = 0;
            PlayerDataManager.get(player).runnerHunterGame = this;
            this.config.level.huntersPos.teleportPlayer(this.config.level.world, player);
            player.setRespawnPosition(this.config.level.world.dimension(), this.config.level.huntersPos.toBlockPos(), 0, true, false);
            if(RunnerHunter.isInventoryLoadingLoaded && !this.config.inventoryName.isEmpty()) PlayerFunctions.setPlayerGearFromString(player, com.natamus.saveandloadinventories.util.Util.getGearStringFromFile(this.config.inventoryName));
            PlayerUtil.sendBossbar(this.bossbar, player, false);
        } else {
            player.setGlowing(this.config.glowHunters);
            player.setHealth(player.getMaxHealth());
            player.clearFire();
            player.removeAllEffects();
            player.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED, 1000000000, 1, false, false, false));
        }

        return true;
    }


    public RunnerHunterGame startGame() {
        this.hasStarted = true;

        ServerPlayer runner = this.runner.get();

        runner.setGameMode(GameType.ADVENTURE);
        runner.clearFire();
        runner.clearSleepingPos();
        runner.inventory.clearContent();
        runner.getEnderChestInventory().clearContent();
        runner.setHealth(runner.getMaxHealth());
        runner.getFoodData().setFoodLevel(20);
        runner.removeAllEffects();
        runner.setGlowing(this.config.glowRunner);
        PlayerDataManager.get(runner).timeAsRunner = 0;
        runner.setGameMode(GameType.ADVENTURE);
        PlayerDataManager.get(runner).runnerHunterGame = this;
        this.config.level.runnerPos.teleportPlayer(this.config.level.world, runner);
        runner.setRespawnPosition(this.config.level.world.dimension(), this.config.level.huntersPos.toBlockPos(), 0, true, false);
        if(RunnerHunter.isInventoryLoadingLoaded && !this.config.inventoryName.isEmpty()) PlayerFunctions.setPlayerGearFromString(runner, com.natamus.saveandloadinventories.util.Util.getGearStringFromFile(this.config.inventoryName));

        for(AccuratePlayer accurateHunter : this.hunters) {
            ServerPlayer hunter = accurateHunter.get();
            hunter.setGameMode(GameType.ADVENTURE);
            hunter.clearFire();
            hunter.clearSleepingPos();
            hunter.inventory.clearContent();
            hunter.setGameMode(GameType.ADVENTURE);
            hunter.getEnderChestInventory().clearContent();
            hunter.setHealth(hunter.getMaxHealth());
            PlayerDataManager.get(hunter).timeAsRunner = 0;
            hunter.removeAllEffects();
            hunter.setGlowing(this.config.glowHunters);
            hunter.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED, 1000000000, 1, false, false, false));
            hunter.getFoodData().setFoodLevel(20);
            PlayerDataManager.get(hunter).runnerHunterGame = this;
            this.config.level.huntersPos.teleportPlayer(this.config.level.world, hunter);
            hunter.setRespawnPosition(this.config.level.world.dimension(), this.config.level.huntersPos.toBlockPos(), 0, true, false);
            if(RunnerHunter.isInventoryLoadingLoaded && !this.config.inventoryName.isEmpty()) PlayerFunctions.setPlayerGearFromString(hunter, com.natamus.saveandloadinventories.util.Util.getGearStringFromFile(this.config.inventoryName));
        }

        for(AccuratePlayer viewer : this.getViewers()) {
            PlayerUtil.sendBossbar(this.bossbar, viewer.get(), false);
            this.scoreboard.sendScoreboard(viewer.get());
        }
        this.scoreboard.updateScoreboard();

        return this;
    }

    public void endGame() {

        this.isEnding = true;
        this.removeBossbar();

        // Yes, I know I am a dumbass.
        List<Integer> intTime = new ArrayList<>();
        HashMap<Integer, AccuratePlayer> time = new HashMap<>();

        List<AccuratePlayer> ap = this.hunters;
        ap.add(this.runner);

        for(AccuratePlayer player : ap) {
            intTime.add(PlayerDataManager.get(player.get()).timeAsRunner);
            time.put(PlayerDataManager.get(player.get()).timeAsRunner, player);
        }

        AccuratePlayer player = time.get(Collections.max(intTime));
        this.winner = player;

        PlayerDataManager.get(player.get()).savedData.wins++;

        for(AccuratePlayer viewer : this.getViewers()) {
            PlayerUtil.sendTitle(viewer.get(), secondaryColor + player.get().getScoreboardName(), mainColor + "has won the game!", 1, 100, 1);
        }
    }

    public void updateInfo() {

        String coords = "";

        if(this.config.runnerActionbarCoords) coords = String.format(" §8| %sRunner Coordinates §7» %s%s [x], %s [z]", secondaryColor, mainColor, this.runner.get().blockPosition().getX(), this.runner.get().blockPosition().getZ());

        for(AccuratePlayer player : this.getViewers()) {
            PlayerUtil.sendActionbar(player.get(), String.format("%sRunner §7» %s%s §8| %sHunters: §7» %s%s %s", secondaryColor, mainColor, this.runner.get().getScoreboardName(), secondaryColor, mainColor, this.hunters.size(), coords));
        }

        CustomBossEvent bossbar = this.bossbar;
        if(!this.isEnding) {
            this.scoreboard.updateScoreboard();
            TextComponent updatedTime = new TextComponent(String.format("§7Runner %s%s§7's points: %s%s§7/%s%s", mainColor, this.runner.get().getScoreboardName(), mainColor, PlayerDataManager.get(this.runner.get()).timeAsRunner, secondaryColor, this.config.maxSeconds));

            bossbar.setValue(PlayerDataManager.get(this.runner.get()).timeAsRunner);
            bossbar.setName(updatedTime);
        }
    }



    public CustomBossEvent createBossbar() {
        CustomBossEvent bossbar = RunnerHunter.server.getCustomBossEvents().create(new ResourceLocation("runnerhunter", this.gameID.toString()), new TextComponent(""));

        TextComponent updatedTime = new TextComponent("Placeholder Text");
        bossbar.setName(updatedTime);

        bossbar.setValue(0);
        bossbar.setMax(this.config.maxSeconds);
        bossbar.setColor(BossEvent.BossBarColor.BLUE);

        return bossbar;
    }

    public boolean removeBossbar() {
        CustomBossEvent bossbar = this.bossbar;
        if(bossbar == null) return false;

        RunnerHunter.server.getCustomBossEvents().remove(this.bossbar);
        return true;
    }


    public boolean switchRoles(AccuratePlayer runner, AccuratePlayer hunter) {
        if(this.runner != runner || !this.hunters.contains(hunter)) return false;

        this.runner = hunter;
        this.hunters.remove(hunter);
        this.hunters.add(runner);

        return true;
    }
    public static RunnerHunterGame getGame(UUID gameID) {
        for(RunnerHunterGame game : RunnerHunterGame.games) {
            if(gameID.equals(game.gameID)) return game;
        }
        return null;
    }

    public ArrayList<AccuratePlayer> getViewers() {
        ArrayList<AccuratePlayer> list = new ArrayList<>();
        list.add(this.runner);
        list.addAll(this.hunters);
        list.addAll(this.spectators);
        return list;
    }
}
