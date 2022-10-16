package net.notcoded.runnerhunter.utilities;

import net.minecraft.server.command.ServerCommandSource;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public final class Permissions {
    public static boolean hasPermission(@NotNull ServerCommandSource permission, @NotNull String command, @Nullable int level) {
        return me.lucko.fabric.api.permissions.v0.Permissions.check(permission, command, level);

    }
}
