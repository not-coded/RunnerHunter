package net.notcoded.runnerhunter.mixin.entity;

import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.decoration.ItemFrame;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.notcoded.runnerhunter.utilities.RunnerHunterUtil;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ItemFrame.class)
public abstract class ItemFrameMixin extends Entity {
    public ItemFrameMixin(EntityType<?> entityType, Level level) {
        super(entityType, level);
    }
    @Inject(method = "interact", at = @At("HEAD"), cancellable = true)
    private void onUse(Player player, InteractionHand interactionHand, CallbackInfoReturnable<InteractionResult> cir) {
        if(RunnerHunterUtil.isRunnerHunter(player)) cir.setReturnValue(InteractionResult.FAIL);
    }

    @Inject(method = "removeFramedMap", at = @At("HEAD"), cancellable = true)
    private void onUse(ItemStack itemStack, CallbackInfo ci) {
        if(!getTags().contains("removeFrameMap")) ci.cancel();
    }
}
