package net.notcoded.runnerhunter;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.minecraft.server.MinecraftServer;
import net.notcoded.runnerhunter.loader.CommandLoader;

public class Main implements ModInitializer {
	// ISSUES:
	// runners[] and hunters[] variables dont work if set to null (or not set to "Placeholder")
	// probably because runners[i] (i is 1) is null or cant be set

	public static boolean isRunning = false;
	public static boolean isOneHit = false;

	public static String prefix = "§8[§c§lRunner§4§lHunter§8] ";
	public static MinecraftServer server;

	@Override
	public void onInitialize() {

		ServerLifecycleEvents.SERVER_STARTED.register((minecraftServer) -> {
			server = minecraftServer;
		});

		CommandLoader.registerCommands();
	}
}
