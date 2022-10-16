package net.notcoded.runnerhunter.mixin;

import net.minecraft.network.ClientConnection;
import net.minecraft.server.PlayerManager;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.stat.Stats;
import net.notcoded.runnerhunter.Main;
import net.notcoded.runnerhunter.utilities.RunnerHunter;
import net.notcoded.runnerhunter.utilities.Utilities;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(PlayerManager.class)
public class PlayerJoinMixin {
    @Inject(at = @At(value = "TAIL"), method = "onPlayerConnect")
    private void onPlayerJoin(ClientConnection connection, ServerPlayerEntity player, CallbackInfo info) {
        if(RunnerHunter.isHunter(player.getEntityName())) {
            if(RunnerHunter.isEnded()){
                RunnerHunter.removeHunter(player.getEntityName());
            }
        }
        if(RunnerHunter.isRunner(player.getEntityName())) {
            if(!RunnerHunter.isEnded()){
                player.kill();
            }else{
                RunnerHunter.removeRunner(player.getEntityName());
            }
        }
    }
}
