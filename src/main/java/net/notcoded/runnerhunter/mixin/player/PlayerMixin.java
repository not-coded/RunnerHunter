package net.notcoded.runnerhunter.mixin.player;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemCooldowns;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.notcoded.codelib.util.pos.EntityPos;
import net.notcoded.runnerhunter.utilities.RunnerHunterUtil;
import net.notcoded.runnerhunter.utilities.player.PlayerUtil;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Player.class)
public abstract class PlayerMixin extends LivingEntity {
    @Shadow public abstract ItemCooldowns getCooldowns();

    protected PlayerMixin(EntityType<? extends LivingEntity> entityType, Level level) {
        super(entityType, level);
    }

    /**
     * @author NotCoded
     * @reason Make shield break actually play the sound to other players.
     */
    @Overwrite
    public boolean disableShield(float f) {
        this.getCooldowns().addCooldown(Items.SHIELD, (int)(f * 20.0F));
        this.stopUsingItem();
        this.level.broadcastEntityEvent(this, (byte)30);
        if((this.getLastDamageSource() != null && this.getLastDamageSource().getEntity() != null && PlayerUtil.getPlayerAttacker((Player) (Object) this, this.getLastDamageSource().getEntity()) != null)){
            //this.level.broadcastEntityEvent(attacker, (byte)30);
            ServerPlayer attacker = PlayerUtil.getPlayerAttacker((Player) (Object) this, this.getLastDamageSource().getEntity());

            SoundSource soundSource = null;
            for (SoundSource source : SoundSource.values()) {
                soundSource = source;
            }
            PlayerUtil.sendSound(attacker, new EntityPos(attacker.position()), SoundEvents.SHIELD_BREAK, soundSource, 2, 1);
        }
        return true;
    }

    @Inject(method = "drop(Z)Z", cancellable = true, at = @At("HEAD"))
    private void drop(boolean dropAll, CallbackInfoReturnable<Boolean> cir) {
        if (!((Object) this instanceof ServerPlayer)) return;

        if(RunnerHunterUtil.isRunnerHunter((ServerPlayer) (Object) this)) cir.setReturnValue(false);
    }
}
