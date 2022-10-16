package net.notcoded.runnerhunter.mixin;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.notcoded.runnerhunter.Main;
import net.notcoded.runnerhunter.utilities.RunnerHunter;
import net.notcoded.runnerhunter.utilities.Utilities;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ServerPlayNetworkHandler.class)
public class PlayerLeaveMixin {
    @Shadow
    public ServerPlayerEntity player;

    @Inject(at = @At(value = "INVOKE", target = "Lnet/minecraft/server/network/ServerPlayerEntity;onDisconnect()V"), method = "onDisconnected")
    private void onPlayerLeave(Text reason, CallbackInfo info) {
        if(RunnerHunter.isHunter(player.getEntityName())) {
            if(RunnerHunter.isEnded()){
                player.kill();
                RunnerHunter.removeHunter(player.getEntityName());
            }
        }
        if(RunnerHunter.isRunner(player.getEntityName())) {
            if(!RunnerHunter.isEnded()){
                if(RunnerHunter.amounthunters > 0){
                    PlayerEntity playerEntity = Utilities.getServerPlayerfromName(RunnerHunter.getRandomHunter(player.getEntityName()));
                    RunnerHunter.switchRoles(playerEntity.getEntityName(), player.getEntityName());
                    RunnerHunter.PlayRunnerEffect(playerEntity.getEntityName());
                    Utilities.broadcastMessage("§c§l" + playerEntity.getEntityName() + " §cis now the runner! §4[§c" + (int) playerEntity.getX() +  " " + (int) playerEntity.getY() + " " + (int) playerEntity.getZ() + "§4]");
                }
            }else{
                RunnerHunter.removeRunner(player.getEntityName());
            }
        }
    }
}
