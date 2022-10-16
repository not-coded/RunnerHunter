package net.notcoded.runnerhunter.mixin;

import net.minecraft.entity.Entity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.notcoded.runnerhunter.Main;
import net.notcoded.runnerhunter.utilities.RunnerHunter;
import net.notcoded.runnerhunter.utilities.Utilities;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ServerPlayerEntity.class)
public class PlayerHitMixin {
    @Inject(at = @At(value = "INVOKE"), method = "damage")
    private void OneHit(DamageSource source, float amount, CallbackInfoReturnable<Boolean> cir){
        if(!Main.isOneHit) { return; }
        Entity victim = null;
        Entity attacker = null;
        if((Entity) (Object) this instanceof PlayerEntity){
            victim = (Entity) (Object) this;
        } else {
            return;
        }
        if(source.getAttacker() != null && source.getAttacker() instanceof PlayerEntity){
            attacker = source.getAttacker();
        } else if(source.getAttacker() == null || !(source.getAttacker() instanceof PlayerEntity)){
            return;
        }

        if(RunnerHunter.isHunter(attacker.getEntityName()) && RunnerHunter.isRunner(victim.getEntityName())){
            if(RunnerHunter.amounthunters > 0 && RunnerHunter.amountrunners > 0 ){
                RunnerHunter.switchRoles(attacker.getEntityName(), victim.getEntityName());
                RunnerHunter.PlayRunnerEffect(attacker.getEntityName());
                Utilities.broadcastMessage("§c§l" + attacker.getEntityName() + " §cis now the runner! §4[§c" + (int) attacker.getX() +  " " + (int) attacker.getY() + " " + (int) attacker.getZ() + "§4]");
            }
        }
    }

}
