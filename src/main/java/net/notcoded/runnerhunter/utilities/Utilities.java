package net.notcoded.runnerhunter.utilities;

import net.minecraft.network.MessageType;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.TranslatableText;
import net.notcoded.runnerhunter.Main;

import java.util.UUID;

public class Utilities {
    public static void applyGlow(ServerPlayerEntity player, boolean selector){
        player.setGlowing(selector);
    }

    public static void broadcastMessage(String message){
        Main.server.getPlayerManager().broadcastChatMessage(new TranslatableText(message), MessageType.CHAT, UUID.randomUUID());
    }

    public static void sendPlayerMessage(ServerPlayerEntity player, String message){
        player.sendMessage(new TranslatableText(message), false);
    }

    public static ServerPlayerEntity getServerPlayerfromName(String player){
        return Main.server.getPlayerManager().getPlayer(player);
    }

    public static void executeCommand(String command){
        if(Main.server != null){
            Main.server.getCommandManager().execute(Main.server.getCommandSource(), command);
        }
    }
}
