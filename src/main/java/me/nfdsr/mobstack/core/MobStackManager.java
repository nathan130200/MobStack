package me.nfdsr.mobstack.core;

import me.nfdsr.mobstack.MobStackPlugin;
import me.nfdsr.mobstack.data.MobStack;
import org.bukkit.entity.Entity;

import java.util.HashMap;

public class MobStackManager {
	private HashMap<String, MobStack> stacks;
	private MobStackPlugin plugin;
	
	public MobStackManager(MobStackPlugin plugin) {
		this.plugin = plugin;
		this.stacks = new HashMap<>();
	}
	
	public void addStack(MobStack stack) {
		this.stacks.put(stack.getId(), stack);
		stack.initialize(this.plugin);
	}
	
	public void removeStack(String id) {
		var mstack = this.stacks.remove(id);
		
		if (mstack != null) {
			mstack.shutdown();
		}
	}
	
	public MobStack findStackById(String id) {
		return this.stacks.get(id);
	}
	
	public MobStack findStackByEntity(Entity entity) {
		if (!entity.hasMetadata("mobstack:uuid"))
			return null;
		
		return this.findStackById(entity.getMetadata("mobstack:uuid").get(0).asString());
	}
}
