package net.notcoded.runnerhunter.mixin.entity;

import net.minecraft.world.entity.vehicle.MinecartTNT;
import net.minecraft.world.level.Explosion;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(MinecartTNT.class)
public class MinecartTNTMixin {
    @Redirect(method = "explode", at = @At(value = "FIELD", target = "Lnet/minecraft/world/level/Explosion$BlockInteraction;BREAK:Lnet/minecraft/world/level/Explosion$BlockInteraction;"))
    public Explosion.BlockInteraction interact() {
        return Explosion.BlockInteraction.NONE;
    }
}

