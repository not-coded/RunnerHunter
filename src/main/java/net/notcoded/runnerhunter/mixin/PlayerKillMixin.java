package net.notcoded.runnerhunter.mixin;

import net.minecraft.entity.Entity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.server.network.ServerPlayerEntity;
import net.notcoded.runnerhunter.utilities.RunnerHunter;
import net.notcoded.runnerhunter.utilities.Utilities;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ServerPlayerEntity.class)
public class PlayerKillMixin {
    @Inject(at = @At(value = "INVOKE", target = "Lnet/minecraft/server/network/ServerPlayerEntity;getPrimeAdversary()Lnet/minecraft/entity/LivingEntity;"), method = "onDeath")
    private void onEntityKilledPlayer(DamageSource source, CallbackInfo ci) {
        Entity attacker = null;
        Entity victim;
        if(source.getAttacker() != null){
            attacker = source.getAttacker();
        }
        victim = (Entity) (Object) this;

        ServerPlayerEntity serverVictim = null;
        ServerPlayerEntity serverAttacker = null;



        // get attacker/runner entity and check if it is null (player id doesnt get stored) then it is the player
        if(attacker == null || attacker.getEntityName() == null) {
            if(RunnerHunter.isRunner(victim.getEntityName())){
                if(RunnerHunter.amounthunters > 0){
                    PlayerEntity playerEntity = Utilities.getServerPlayerfromName(RunnerHunter.getRandomHunter(victim.getEntityName()));
                    RunnerHunter.switchRoles(playerEntity.getEntityName(), victim.getEntityName());
                    RunnerHunter.PlayRunnerEffect(playerEntity.getEntityName());
                    Utilities.broadcastMessage("§c§l" + playerEntity.getEntityName() + " §cis now the runner! §4[§c" + (int) playerEntity.getX() +  " " + (int) playerEntity.getY() + " " + (int) playerEntity.getZ() + "§4]");
                }
            }
            return;
        }
        if(!(attacker instanceof PlayerEntity)){
            if(RunnerHunter.isRunner(victim.getEntityName())){
                if(RunnerHunter.amounthunters > 0){
                    PlayerEntity playerEntity = Utilities.getServerPlayerfromName(RunnerHunter.getRandomHunter(victim.getEntityName()));
                    RunnerHunter.switchRoles(playerEntity.getEntityName(), victim.getEntityName());
                    RunnerHunter.PlayRunnerEffect(playerEntity.getEntityName());
                    Utilities.broadcastMessage("§c§l" + playerEntity.getEntityName() + " §cis now the runner! §4[§c" + (int) playerEntity.getX() +  " " + (int) playerEntity.getY() + " " + (int) playerEntity.getZ() + "§4]");
                }
            }
            return;
        }

        if(!(victim instanceof PlayerEntity)){
            return;
        }

        serverVictim = (ServerPlayerEntity) victim;
        serverAttacker = (ServerPlayerEntity) attacker;

        if(attacker.getEntityName() == victim.getEntityName()) { return; }
        if(victim.getEntityName() == null) { return; }

        if(RunnerHunter.isHunter(attacker.getEntityName()) || RunnerHunter.isRunner(attacker.getEntityName())){{
            serverAttacker.setHealth(serverAttacker.getMaxHealth());
            serverAttacker.giveItemStack(new ItemStack(Items.GOLDEN_APPLE));
            serverAttacker.giveItemStack(new ItemStack(Items.ARROW, 5));
        }}

        if(RunnerHunter.isRunner(attacker.getEntityName())) { return; }

        if(RunnerHunter.isRunner(victim.getEntityName()) && RunnerHunter.isHunter(attacker.getEntityName())){
            RunnerHunter.switchRoles(attacker.getEntityName(), victim.getEntityName());
            RunnerHunter.PlayRunnerEffect(victim.getEntityName());
            Utilities.broadcastMessage("§c§l" + attacker.getEntityName() + " §cis now the runner! §4[§c" + (int) attacker.getX() +  " " + (int) attacker.getY() + " " + (int) attacker.getZ() + "§4]");
        }
    }
}
