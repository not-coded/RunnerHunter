package net.notcoded.runnerhunter.mixin.player;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageSource;
import net.notcoded.codelib.players.AccuratePlayer;
import net.notcoded.runnerhunter.game.RunnerHunterGame;
import net.notcoded.runnerhunter.utilities.RunnerHunterUtil;
import net.notcoded.runnerhunter.utilities.player.PlayerDataManager;
import net.notcoded.runnerhunter.utilities.player.PlayerUtil;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Random;

@Mixin(ServerPlayer.class)
public class ServerPlayerMixin {
    @Inject(at = @At(value = "HEAD"), method = "hurt")
    private void oneHit(DamageSource damageSource, float f, CallbackInfoReturnable<Boolean> cir){
        ServerPlayer player = (ServerPlayer) (Object) this;
        if(damageSource == null || damageSource.getEntity() == null || PlayerUtil.getPlayerAttacker(player, damageSource.getEntity()) == null) return;

        ServerPlayer attacker = PlayerUtil.getPlayerAttacker(player, damageSource.getEntity());
        if(!RunnerHunterUtil.isRunnerHunter(player) || !RunnerHunterUtil.isRunnerHunter(attacker) || !PlayerDataManager.get(player).runnerHunterGame.equals(PlayerDataManager.get(attacker).runnerHunterGame)) return;

        RunnerHunterGame game = PlayerDataManager.get(player).runnerHunterGame;
        if(!game.config.isOneHit) return;

        if(!game.setRunner(AccuratePlayer.create(attacker), true, true)) return;
    }

    @Inject(at = @At(value = "HEAD"), method = "disconnect")
    private void onPlayerLeave(CallbackInfo ci) {
        ServerPlayer player = (ServerPlayer) (Object) this;
        if(!RunnerHunterUtil.isRunnerHunter(player)) {
            PlayerDataManager.removePlayerData(player);
            return;
        }
        AccuratePlayer accuratePlayer = AccuratePlayer.create(player);

        RunnerHunterGame game = PlayerDataManager.get(player).runnerHunterGame;

        if(game.hunters.contains(accuratePlayer)) {
            RunnerHunterGame.leave(player);
            PlayerDataManager.removePlayerData(player);
            return;
        }

        if(game.runner.equals(accuratePlayer)) {
            if(!game.hunters.isEmpty()) {
                AccuratePlayer accurateHunter = game.hunters.get(new Random().nextInt(game.hunters.size()));

                if(!game.setRunner(AccuratePlayer.create(accurateHunter.get()), true, true)) return;
            } else if (!game.isEnding){
                game.endGame();
                System.out.println("welp, ending onPlayerLeave()");
            }
        }
        PlayerDataManager.removePlayerData(player);
    }

    @Inject(at = @At(value = "HEAD"), method = "die")
    private void onEntityKilledPlayer(DamageSource damageSource, CallbackInfo ci) {
        ServerPlayer player = (ServerPlayer) (Object) this;
        if(!RunnerHunterUtil.isRunnerHunter(player)) return;
        RunnerHunterGame game = PlayerDataManager.get(player).runnerHunterGame;
        if(damageSource == null || damageSource.getEntity() == null || PlayerUtil.getPlayerAttacker(player, damageSource.getEntity()) == null) {
            AccuratePlayer accurateHunter = game.hunters.get(new Random().nextInt(game.hunters.size()));

            if(!game.setRunner(AccuratePlayer.create(accurateHunter.get()), true, true)) return;
            return;
        }

        ServerPlayer attacker = PlayerUtil.getPlayerAttacker(player, damageSource.getEntity());
        if(!RunnerHunterUtil.isRunnerHunter(attacker) || !game.equals(PlayerDataManager.get(attacker).runnerHunterGame)) return;

        if(!game.setRunner(AccuratePlayer.create(attacker), true, true)) return;
    }
}
