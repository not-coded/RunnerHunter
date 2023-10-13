package net.notcoded.runnerhunter.mixin.server;

import net.minecraft.server.MinecraftServer;
import net.notcoded.runnerhunter.utilities.ServerUtil;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(MinecraftServer.class)
public class MinecraftServerMixin {
    @Unique
    boolean firstTickPassed = false;

    @Inject(at = @At("HEAD"), method = "tickChildren")
    private void tickHead(CallbackInfo ci) {
        if (!this.firstTickPassed) {
            this.firstTickPassed = true;
            ServerUtil.firstTick((MinecraftServer)(Object)this);
        }

    }

    @Inject(at = @At("TAIL"), method = "tickChildren")
    private void tickTail(CallbackInfo ci) {
        ServerUtil.everyTick();
    }
}
