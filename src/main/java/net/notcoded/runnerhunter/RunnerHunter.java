package net.notcoded.runnerhunter;

import net.fabricmc.api.ModInitializer;
import net.minecraft.server.MinecraftServer;
import net.notcoded.runnerhunter.loader.CommandLoader;
import xyz.nucleoid.fantasy.Fantasy;

public class RunnerHunter implements ModInitializer {

	public static MinecraftServer server;
	public static boolean isInventoryLoadingLoaded;
	public static Fantasy fantasy;
	@Override
	public void onInitialize() {
		CommandLoader.registerCommands();
	}
}
