package net.notcoded.runnerhunter.mixin.player;

import net.minecraft.network.Connection;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.players.PlayerList;
import net.notcoded.runnerhunter.game.RunnerHunterGame;
import net.notcoded.runnerhunter.utilities.player.PlayerDataManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(PlayerList.class)
public class PlayerListMixin {
    // Create player data on join
    @Inject(at = @At("HEAD"), method = "placeNewPlayer")
    private void placeNewPlayerHead(Connection connection, ServerPlayer serverPlayer, CallbackInfo ci) {
        PlayerDataManager.addPlayerData(serverPlayer);
        RunnerHunterGame.leave(serverPlayer);
    }
}
