package me.nfdsr.mobstack.data;

import me.nfdsr.mobstack.MobStackPlugin;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitTask;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.stream.Collectors;

public class MobStack {
	private String id;
	private int size;
	private int experience;
	private ArrayList<ItemStack> drops;
	private Entity entity;
	private boolean respawned;
	
	private BukkitTask keepAliveTask;
	private int keepAliveStep;
	
	public MobStack(String id) {
		this.id = id;
		this.drops = new ArrayList<>();
		this.experience = 0;
	}
	
	private MobStackPlugin plugin;
	
	public void initialize(MobStackPlugin p) {
		this.plugin = p;
		
		this.keepAliveTask = p.getServer().getScheduler().runTaskTimerAsynchronously(p,
				this::keepAliveTask, 20L, 20L * 5);
	}
	
	protected void keepAliveTask() {
		if(this.keepAliveTask == null) {
			this.shutdown();
		}
		else {
			if (this.entity == null)
				this.shutdown();
			else {
				if (!this.entity.isDead()) {
					if (this.keepAliveStep < 3)
						this.keepAliveStep++;
					else
						this.shutdown();
				}
			}
		}
	}
	
	public void shutdown() {
		if (this.keepAliveTask != null) {
			this.keepAliveTask.cancel();
			this.keepAliveTask = null;
		}
		
		this.entity = null;
	}
	
	public boolean isRespawned() {
		return respawned;
	}
	
	public void setRespawned(boolean respawned) {
		this.respawned = respawned;
	}
	
	public int getExperience() {
		return experience;
	}
	
	public void setExperience(int experience) {
		this.experience = experience;
	}
	
	public void addDrops(Collection<ItemStack> drops) {
		this.drops.addAll(drops);
	}
	
	public int getSize() {
		return size;
	}
	
	public void setSize(int size) {
		this.size = size;
	}
	
	public ArrayList<ItemStack> getDrops() {
		return drops;
	}
	
	public String getId() {
		return id;
	}
	
	public Entity getEntity() {
		return entity;
	}
	
	public void setEntity(Entity entity) {
		this.entity = entity;
	}
	
	public void update() {
		if (this.entity != null) {
			this.entity.setCustomNameVisible(true);
			this.entity.setCustomName("§7" + pascalizeEntityTypeName(this.entity.getType()) + " §3x" + this.size);
		}
	}
	
	public void destroy() {
		this.size = 0;
		this.experience = 0;
		this.drops.clear();
		this.drops = null;
	}
	
	public static String pascalizeEntityTypeName(EntityType type) {
		var nameParts = Arrays.stream(type.name().split("\\_"))
				.map(x -> x.toLowerCase())
				.collect(Collectors.toList());
		
		for (var i = 0; i < nameParts.size(); i++) {
			var token = nameParts.get(i).toCharArray();
			token[0] = Character.toUpperCase(token[0]);
			nameParts.set(i, new String(token));
		}
		
		return String.join(" ", nameParts);
	}
}
