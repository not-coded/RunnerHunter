package net.notcoded.runnerhunter.utilities;

import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.notcoded.runnerhunter.Main;

import java.util.Arrays;
import java.util.Objects;

public class RunnerHunter {

    public static String[] runners = {"Placeholder", "Placeholder", "Placeholder", "Placeholder", "Placeholder", "Placeholder", "Placeholder", "Placeholder", "Placeholder", "Placeholder", "Placeholder", "Placeholder", "Placeholder", "Placeholder", "Placeholder"};
    public static int amountrunners = 0;

    public static String[] hunters = {"Placeholder", "Placeholder", "Placeholder", "Placeholder", "Placeholder", "Placeholder", "Placeholder", "Placeholder", "Placeholder", "Placeholder", "Placeholder", "Placeholder", "Placeholder", "Placeholder", "Placeholder"};
    public static int amounthunters = 0;

    public static boolean shouldGlowRunners = false;

    public static boolean shouldGlowHunters = false;

    public static boolean isEnded(){
        return amountrunners == 0 && runners.length == 0 && amounthunters == 0 && hunters.length == 0;
    }

    public static void reset(ServerCommandSource source){
        for(int i = 0; i < Main.server.getPlayerNames().length; i++){
            Utilities.applyGlow(Objects.requireNonNull(Main.server.getPlayerManager().getPlayer(Main.server.getPlayerNames()[i])), false);
        }

        Utilities.executeCommand("/team leave @a");

        amounthunters = 0;
        amountrunners = 0;
        runners = new String[]{"Placeholder", "Placeholder", "Placeholder", "Placeholder", "Placeholder", "Placeholder", "Placeholder", "Placeholder", "Placeholder", "Placeholder", "Placeholder", "Placeholder", "Placeholder", "Placeholder", "Placeholder"};
        hunters = new String[]{"Placeholder", "Placeholder", "Placeholder", "Placeholder", "Placeholder", "Placeholder", "Placeholder", "Placeholder", "Placeholder", "Placeholder", "Placeholder", "Placeholder", "Placeholder", "Placeholder", "Placeholder"};
        Utilities.broadcastMessage(Main.prefix + "§cThe game has been reset.");
        Main.isRunning = false;
        Main.isOneHit = false;
    }
    public static void setHunterToRunner(String player){
        if(isRunner(player)){
            return;
        }
       if(isHunter(player)){
           removeHunter(player);
           setRunner(player, shouldGlowRunners);
       }
    }

    public static void switchRoles(String hunter, String runner){
        if(isRunner(runner) && isHunter(hunter)){
            removeRunner(runner);
            removeHunter(hunter);
            setHunter(runner, shouldGlowHunters);
            setRunner(hunter, shouldGlowRunners);
        }
    }

    public static void setRunnerToHunter(String player){
        if(isHunter(player)){
            return;
        }
        if(isRunner(player)){
            removeRunner(player);
            setHunter(player, shouldGlowHunters);
        }
    }

    public static boolean isHunter(String player){
        for(int i = 0; i < amounthunters; i++){
            if(hunters[i].toLowerCase().contains(player.toLowerCase())){
                return true;
            }
        }
        return false;
    }

    public static boolean isRunner(String player){
        for(int i = 0; i < amountrunners; i++){
            if(runners[i].toLowerCase().contains(player.toLowerCase())){
                return true;
            }
        }
        return false;
    }

    public static PlayerEntity getNearestPlayer(String player){
        return Utilities.getServerPlayerfromName(player).getServerWorld().getClosestPlayer(Utilities.getServerPlayerfromName(player).getX() + 1, Utilities.getServerPlayerfromName(player).getY() + 1, Utilities.getServerPlayerfromName(player).getZ() + 1, 20, true);
    }

    public static PlayerEntity getRandomPlayer(String player){
        return Utilities.getServerPlayerfromName(player).getServerWorld().getRandomAlivePlayer();
    }

    public static void PlayRunnerEffect(String runner){
        Utilities.executeCommand("/execute at @a run playsound minecraft:item.totem.use ambient @a ~ ~ ~ 100000 0.5");
        Utilities.executeCommand("/execute at " + runner + " run particle totem_of_undying ~ ~ ~ 0 0 0 1 100 normal");
    }

    public static String getRandomHunter(String player){
        return hunters[(int)(Math.random() * amounthunters)];
    }

    public static void setHunter(String player, boolean shouldGlow){
        hunters[amounthunters] = player;
        amounthunters++;

        Utilities.executeCommand("/team join hunter " + player);

        if(shouldGlow){
            Utilities.applyGlow(Utilities.getServerPlayerfromName(player), true);
        }
        Main.isRunning = true;

    }

    public static void removeHunter(String player){
        for(int i = 0; i < amounthunters; i++){
            if(hunters[i].toLowerCase().contains(player.toLowerCase())){
                hunters[i] = "Placeholder";
                amounthunters--;
                Utilities.executeCommand("/team leave " + player);
            }
            Utilities.applyGlow(Utilities.getServerPlayerfromName(player), false);
        }
    }

    public static void setRunner(String player, boolean shouldGlow){
        runners[amountrunners] = player;
        amountrunners++;
        Utilities.executeCommand("/team join runner " + player);

        if(shouldGlow){
            Utilities.applyGlow(Utilities.getServerPlayerfromName(player), true);
        }
        Main.isRunning = true;
    }

    public static void removeRunner(String player){
        for(int i = 0; i < amountrunners; i++){
            if(runners[i].toLowerCase().contains(player.toLowerCase())){
                runners[i] = "Placeholder";
                amountrunners--;
                Utilities.executeCommand("/team leave " + player);
            }
            Utilities.applyGlow(Utilities.getServerPlayerfromName(player), false);
        }
    }
}
