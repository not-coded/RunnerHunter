package net.notcoded.runnerhunter.mixin.player;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.food.FoodData;
import net.notcoded.runnerhunter.utilities.RunnerHunterUtil;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(FoodData.class)
public class FoodDataMixin {
    @Unique
    Player player;
    @Inject(at = @At("HEAD"), method = "tick")
    private void tick(Player player, CallbackInfo ci) {
        this.player = player;
    }

    @Inject(method = "tick", at = @At("TAIL"))
    private void modifyHunger(Player player, CallbackInfo ci) {
        if (!(player instanceof ServerPlayer)) return;

        FoodData data = (FoodData)(Object)this;
        if(!RunnerHunterUtil.isRunnerHunter(player)) return;

        data.setFoodLevel(20);
    }

}
