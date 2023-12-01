package net.notcoded.runnerhunter.game;

import com.natamus.collective_fabric.functions.PlayerFunctions;
import net.minecraft.Util;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.bossevents.CustomBossEvent;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.BossEvent;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.level.GameType;
import net.notcoded.codelib.players.AccuratePlayer;
import net.notcoded.codelib.util.TickUtil;
import net.notcoded.codelib.util.pos.EntityPos;
import net.notcoded.runnerhunter.RunnerHunter;
import net.notcoded.runnerhunter.utilities.player.PlayerData;
import net.notcoded.runnerhunter.utilities.player.PlayerDataManager;
import net.notcoded.runnerhunter.utilities.player.PlayerUtil;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Random;
import java.util.UUID;

import static net.notcoded.runnerhunter.utilities.RunnerHunterUtil.mainColor;
import static net.notcoded.runnerhunter.utilities.RunnerHunterUtil.secondaryColor;

public class RunnerHunterGame {

    public static ArrayList<RunnerHunterGame> games = new ArrayList<>();

    public UUID gameID;

    public AccuratePlayer runner;

    public ArrayList<AccuratePlayer> hunters;
    public ArrayList<AccuratePlayer> spectators = new ArrayList<>();

    public boolean hasStarted = false;

    public boolean shouldStart = false;

    public GameConfiguration config;

    public int time;


    // Winner thingie
    public AccuratePlayer winner = null;

    public boolean isEnding;

    public int currentEndTime = 0;

    public int currentStartTime = 5;
    public boolean shouldWait = false;

    public CustomBossEvent BOSSBAR;

    public RunnerHunterGame(@NotNull AccuratePlayer runner, @NotNull ArrayList<AccuratePlayer> hunters, @NotNull GameConfiguration configuration) {
        this.runner = runner;
        this.hunters = hunters;
        this.config = configuration;
        this.time = configuration.time;

        this.gameID = UUID.randomUUID();
        this.BOSSBAR = this.createBossbar();

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
        player.setHealth(player.getMaxHealth());
        player.setGlowing(false);
        PlayerUtil.sendBossbar(PlayerDataManager.get(player).runnerHunterGame.BOSSBAR, player, true);

        player.setRespawnPosition(RunnerHunter.server.overworld().dimension(), RunnerHunter.server.overworld().getSharedSpawnPos(), 0, true, false);

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
                System.out.println("welp, ending second() failsafe()");
            }
        }
        if(!this.hasStarted && this.shouldStart) {
            this.currentStartTime--;

            if (5 - this.currentStartTime >= 5) {
                for(AccuratePlayer player : this.getViewers()) {
                    PlayerUtil.sendSound(player.get(), new EntityPos(player.get()), SoundEvents.NOTE_BLOCK_PLING, SoundSource.BLOCKS, 10, 2);
                }
                this.hasStarted = true;
                return;
            }

            String title;
            String color = "§a";

            if(this.currentStartTime <= 3 && this.currentStartTime > 1) {
                color = "§e";
            } else if(this.currentStartTime <= 1) {
                color = "§c";
            }

            title = color + this.currentStartTime;

            for(AccuratePlayer player : this.getViewers()) {
                PlayerUtil.sendSound(player.get(), new EntityPos(player.get()), SoundEvents.NOTE_BLOCK_HAT, SoundSource.BLOCKS, 10, 2);
                PlayerUtil.sendTitle(player.get(), title, "", 1, 20, 1);
            }
        } else if(this.hasStarted){
            if(this.isEnding) {
                this.currentEndTime++;
                if(this.currentEndTime >= 5 || !this.shouldWait) {
                    this.isEnding = false;

                    for(AccuratePlayer player : this.getViewers()) RunnerHunterGame.leave(player.get());

                    this.removeBossbar();
                    RunnerHunterGame.games.remove(this);
                    this.shouldStart = false;
                    return;
                }
            } else {
                this.updateInfo();

                PlayerDataManager.get(this.runner.get()).timeAsRunner++;

                if(this.time > 0) this.time--;
                if(this.time <= 0 && !this.isEnding) {
                    System.out.println("welp, ending second() started ending");
                    this.endGame();
                }
            }
        }
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
            player.setGlowing(this.config.glowRunner);
            PlayerDataManager.get(player).runnerHunterGame = this;
            this.config.level.runnerPos.teleportPlayer(this.config.level.world, player);
            if(RunnerHunter.isInventoryLoadingLoaded && !this.config.inventoryName.isEmpty()) PlayerFunctions.setPlayerGearFromString(player, com.natamus.saveandloadinventories.util.Util.getGearStringFromFile(this.config.inventoryName));
            PlayerUtil.sendBossbar(this.BOSSBAR, player, false);
        } else {
            player.setGlowing(this.config.glowRunner);
            player.setHealth(player.getMaxHealth());
            player.clearFire();
        }

        PlayerDataManager.get(player).savedData.swaps++;

        if(announce) {
            String message = String.format("%s§l%s %sis now the runner! %s[%s%s %s %s%s]", secondaryColor, player.getScoreboardName(), mainColor, secondaryColor, mainColor, player.getX(), player.getY(), player.getZ(), secondaryColor);
            for(AccuratePlayer ap : this.getViewers()) {
                ap.get().sendMessage(new TextComponent(message), Util.NIL_UUID);
            }
        }

        return true;
    }

    public boolean addHunter(AccuratePlayer accuratePlayer, boolean inGame) {
        if(this.hunters.contains(accuratePlayer)) return false;
        int size = this.hunters.size();
        System.out.println(size);
        if(size > 0) size = new Random().nextInt(this.hunters.size());
        System.out.println(size);

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
            player.setGlowing(this.config.glowHunters);
            PlayerDataManager.get(player).runnerHunterGame = this;
            this.config.level.huntersPos.teleportPlayer(this.config.level.world, player);
            player.setRespawnPosition(this.config.level.world.dimension(), this.config.level.huntersPos.toBlockPos(), 0, true, false);
            if(RunnerHunter.isInventoryLoadingLoaded && !this.config.inventoryName.isEmpty()) PlayerFunctions.setPlayerGearFromString(player, com.natamus.saveandloadinventories.util.Util.getGearStringFromFile(this.config.inventoryName));
            PlayerUtil.sendBossbar(this.BOSSBAR, player, false);
        } else {
            player.setGlowing(this.config.glowHunters);
            player.setHealth(player.getMaxHealth());
            player.clearFire();
        }

        return true;
    }

    public RunnerHunterGame startGame() {
        //this.hasStarted = true;
        this.shouldStart = true;

        ServerPlayer runner = this.runner.get();

        runner.setGameMode(GameType.ADVENTURE);
        runner.clearFire();
        runner.clearSleepingPos();
        runner.inventory.clearContent();
        runner.getEnderChestInventory().clearContent();
        runner.setHealth(runner.getMaxHealth());
        runner.setGlowing(this.config.glowRunner);
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
            hunter.getEnderChestInventory().clearContent();
            hunter.setHealth(hunter.getMaxHealth());
            hunter.setGlowing(this.config.glowHunters);
            PlayerDataManager.get(hunter).runnerHunterGame = this;
            this.config.level.huntersPos.teleportPlayer(this.config.level.world, hunter);
            hunter.setRespawnPosition(this.config.level.world.dimension(), this.config.level.huntersPos.toBlockPos(), 0, true, false);
            if(RunnerHunter.isInventoryLoadingLoaded && !this.config.inventoryName.isEmpty()) PlayerFunctions.setPlayerGearFromString(hunter, com.natamus.saveandloadinventories.util.Util.getGearStringFromFile(this.config.inventoryName));
            hunter.addEffect(new MobEffectInstance(MobEffects.BLINDNESS, 100, 1, true, true));
            hunter.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 100, 255, true, true));
            hunter.addEffect(new MobEffectInstance(MobEffects.JUMP, 100, 255, true, true));
            hunter.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, 100, 1255, true, true));
        }

        for(AccuratePlayer viewer : this.getViewers()) {
            PlayerUtil.sendBossbar(this.BOSSBAR, viewer.get(), false);
        }

        return this;
    }

    public void endGame() {

        this.isEnding = true;
        this.removeBossbar();

        AccuratePlayer player = this.runner;
        this.winner = player;

        PlayerDataManager.get(player.get()).savedData.wins++;

        for(AccuratePlayer viewer : this.getViewers()) {
            PlayerUtil.sendTitle(viewer.get(), secondaryColor + player.get().getScoreboardName(), mainColor + "has won the game!", 1, 100, 1);
        }
    }

    public void updateInfo() {
        for(AccuratePlayer player : this.getViewers()) {
            PlayerUtil.sendActionbar(player.get(), String.format("%sRunner §7» %s%s §8| %sHunters: §7» %s%s", secondaryColor, mainColor, this.runner.get().getScoreboardName(), secondaryColor, mainColor, this.hunters.size()));
        }

        CustomBossEvent bossbar = this.BOSSBAR;
        if(this.time > 0) {
            String[] parsedTime = TickUtil.minuteTimeStamp(time * 20);
            TextComponent updatedTime = new TextComponent("§7Game end in " + mainColor + parsedTime[0] + secondaryColor + ":" + mainColor + parsedTime[1] + "§7...");

            bossbar.setValue(this.time);
            bossbar.setName(updatedTime);
            return;
        }
    }

    public CustomBossEvent createBossbar() {
        CustomBossEvent bossbar = RunnerHunter.server.getCustomBossEvents().create(new ResourceLocation("runnerhunter", this.gameID.toString()), new TextComponent(""));

        String[] parsedTime = TickUtil.minuteTimeStamp(time * 20);
        TextComponent updatedTime = new TextComponent("§7Game end in " + mainColor + parsedTime[0] + secondaryColor + ":" + mainColor + parsedTime[1] + "§7...");
        bossbar.setName(updatedTime);

        bossbar.setValue(this.time);
        bossbar.setMax(this.config.time);
        bossbar.setColor(BossEvent.BossBarColor.BLUE);

        return bossbar;
    }

    public boolean removeBossbar() {
        CustomBossEvent bossbar = this.BOSSBAR;
        if(bossbar == null) return false;

        RunnerHunter.server.getCustomBossEvents().remove(this.BOSSBAR);
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
        ArrayList<AccuratePlayer> list = this.spectators;
        list.addAll(this.hunters);
        list.add(this.runner);
        return list;
    }
}
