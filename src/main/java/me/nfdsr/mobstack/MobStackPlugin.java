package me.nfdsr.mobstack;

import me.nfdsr.mobstack.core.MobStackManager;
import me.nfdsr.mobstack.events.EntityEvents;
import org.bukkit.event.HandlerList;
import org.bukkit.plugin.java.JavaPlugin;

public class MobStackPlugin extends JavaPlugin {
	private MobStackManager stackManager;
	
	public void onEnable() {
		this.stackManager = new MobStackManager(this);
		getServer().getPluginManager().registerEvents(new EntityEvents(this), this);
	}
	
	public void onDisable() {
		getServer().getScheduler().cancelTasks(this);
		HandlerList.unregisterAll(this);
	}
	
	public MobStackManager getStackManager() {
		return this.stackManager;
	}
}
