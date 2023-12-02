package net.notcoded.runnerhunter.mixin.player;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(ServerGamePacketListenerImpl.class)
public class ServerGamePacketListenerMixin {

    // Thank you, our lord and saviour
    //   _____  _          _____            _
    // |  __ \(_)        / ____|          | |
    // | |__) |_ _______| |     ___   ___ | | _____ _   _
    // |  _  /| |_  / _ \ |    / _ \ / _ \| |/ / _ \ | | |
    // | | \ \| |/ /  __/ |___| (_) | (_) |   <  __/ |_| |
    // |_|  \_\_/___\___|\_____\___/ \___/|_|\_\___|\__, |
    //                                               __/ |
    //                                              |___/
    @Redirect(method = "handleInteract", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/level/ServerPlayer;getCurrentAttackReach(F)F"))
    public float redirectReachLonger(ServerPlayer playerEntity, float f) {
        return playerEntity.getCurrentAttackReach(f) + 0.75F;
    }

}
