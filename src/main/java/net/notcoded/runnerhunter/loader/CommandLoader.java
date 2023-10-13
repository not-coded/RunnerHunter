package net.notcoded.runnerhunter.loader;

import net.notcoded.runnerhunter.commands.*;

import net.fabricmc.fabric.api.command.v1.CommandRegistrationCallback;


public class CommandLoader {
    public static void registerCommands() {
        CommandRegistrationCallback.EVENT.register(RunnerHunterCommand::register);
        CommandRegistrationCallback.EVENT.register(MapCommand::register);
    }
}
