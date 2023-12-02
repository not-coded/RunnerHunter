package net.notcoded.runnerhunter.mixin.player;

import com.natamus.collective_fabric.functions.PlayerFunctions;
import net.blumbo.blfscheduler.BlfRunnable;
import net.blumbo.blfscheduler.BlfScheduler;
import net.minecraft.network.Connection;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.players.PlayerList;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.notcoded.codelib.players.AccuratePlayer;
import net.notcoded.runnerhunter.RunnerHunter;
import net.notcoded.runnerhunter.game.RunnerHunterGame;
import net.notcoded.runnerhunter.utilities.RunnerHunterUtil;
import net.notcoded.runnerhunter.utilities.player.PlayerDataManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(PlayerList.class)
public class PlayerListMixin {
    // Create player data on join
    @Inject(at = @At("HEAD"), method = "placeNewPlayer")
    private void placeNewPlayerHead(Connection connection, ServerPlayer serverPlayer, CallbackInfo ci) {
        PlayerDataManager.addPlayerData(serverPlayer);
        if(RunnerHunterUtil.isRunnerHunter(serverPlayer)) RunnerHunterGame.leave(serverPlayer);
    }

    @Inject(at = @At(value = "HEAD"), method = "respawn")
    private void hunterRespawn(ServerPlayer player, boolean bl, CallbackInfoReturnable<ServerPlayer> cir) {
        if(!RunnerHunterUtil.isRunnerHunter(player)) return;
        RunnerHunterGame game = PlayerDataManager.get(player).runnerHunterGame;

        if(RunnerHunter.isInventoryLoadingLoaded && !game.config.inventoryName.isEmpty()) PlayerFunctions.setPlayerGearFromString(player, com.natamus.saveandloadinventories.util.Util.getGearStringFromFile(game.config.inventoryName));

        if(game.runner == AccuratePlayer.create(player) || !game.hunters.contains(AccuratePlayer.create(player))) return;

        BlfScheduler.delay(100, new BlfRunnable() {
            @Override
            public void run() {
                player.removeAllEffects();
                player.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED, 1000000000, 1, false, false, false));
            }
        });
    }
}
