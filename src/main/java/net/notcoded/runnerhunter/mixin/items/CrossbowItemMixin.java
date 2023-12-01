package net.notcoded.runnerhunter.mixin.items;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.CrossbowItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.notcoded.runnerhunter.utilities.player.PlayerDataManager;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(CrossbowItem.class)
public class CrossbowItemMixin {
    @Inject(method = "loadProjectile", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/player/Inventory;removeItem(Lnet/minecraft/world/item/ItemStack;)V"))
    private static void giveBackPlayerArrow(LivingEntity livingEntity, ItemStack itemStack, ItemStack itemStack2, boolean bl, boolean bl2, CallbackInfoReturnable<Boolean> cir) {
        if(!(livingEntity instanceof ServerPlayer)) return;
        ServerPlayer player = (ServerPlayer) livingEntity;

        if(PlayerDataManager.get(player).runnerHunterGame != null) player.addItem(new ItemStack(Items.ARROW));
    }
}
