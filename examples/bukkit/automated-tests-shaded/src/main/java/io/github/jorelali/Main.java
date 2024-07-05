package io.github.jorelali;

import dev.jorel.commandapi.CommandAPI;
import dev.jorel.commandapi.CommandAPIBukkitConfig;
import dev.jorel.commandapi.CommandAPICommand;
import org.bukkit.plugin.java.JavaPlugin;

public class Main extends JavaPlugin {
	@Override
	public void onLoad() {
		CommandAPI.onLoad(new CommandAPIBukkitConfig(this));
	}

	@Override
	public void onEnable() {
		CommandAPI.onEnable();

		// TODO: Test arguments, suggestions
		new CommandAPICommand("ping").executes((sender, args) -> {
			sender.sendMessage("pong!");
		}).register();
	}
}
