package me.nfdsr.mobstack.events;

import me.nfdsr.mobstack.MobStackPlugin;
import me.nfdsr.mobstack.core.MobStackManager;
import me.nfdsr.mobstack.data.MobStack;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.EntitySpawnEvent;
import org.bukkit.metadata.FixedMetadataValue;

import java.util.UUID;

public class EntityEvents implements Listener {
	private MobStackPlugin plugin;
	private MobStackManager manager;
	
	public EntityEvents(MobStackPlugin plugin) {
		this.plugin = plugin;
		this.manager = this.plugin.getStackManager();
	}
	
	@EventHandler
	void onEntitySpawn(EntitySpawnEvent e) {
		var entity = e.getEntity();
		
		if (!(entity instanceof LivingEntity))
			return;
		
		if (entity instanceof Player)
			return;
		
		var mstack = this.manager.findStackByEntity(entity);
		
		if (mstack != null && mstack.isRespawned()) {
			mstack.setRespawned(false);
			mstack.update();
			return;
		}
		else {
			var owningEntity = entity.getNearbyEntities(10, 10, 10)
					.stream().filter(xe -> xe.getType().equals(entity.getType())
							&& xe.hasMetadata("mobstack:uuid")).findFirst().orElse(null);
			
			if (owningEntity != null) {
				String owningStackId = owningEntity.getMetadata("mobstack:uuid")
						.get(0).asString();
				
				mstack = this.manager.findStackById(owningStackId);
				
				if (mstack != null && !mstack.isRespawned()) {
					e.setCancelled(true);
					mstack.setSize(mstack.getSize() + 1);
					mstack.update();
					return;
				}
			}
		}
		
		String stackId;
		
		if (entity.hasMetadata("mobstack:uuid"))
			stackId = entity.getMetadata("mobstack:uuid").get(0).asString();
		else {
			stackId = UUID.randomUUID().toString();
			entity.setMetadata("mobstack:uuid", new FixedMetadataValue(this.plugin, stackId));
		}
		
		mstack = this.manager.findStackById(stackId);
		
		if (mstack == null) {
			mstack = new MobStack(stackId);
			mstack.setEntity(entity);
			mstack.setSize(1);
			mstack.update();
			this.manager.addStack(mstack);
		}
	}
	
	@EventHandler
	void onEntityDeath(EntityDeathEvent e) {
		var entity = e.getEntity();
		
		if (!(entity instanceof LivingEntity))
			return;
		
		if (entity instanceof Player)
			return;
		
		entity.setCustomName(null);
		entity.setCustomNameVisible(false);
		
		var mstack = this.manager.findStackByEntity(entity);
		
		if (mstack != null) {
			var newSize = mstack.getSize() - 1;
			
			if (newSize <= 0) {
				e.getDrops().clear();
				e.getDrops().addAll(mstack.getDrops());
				e.setDroppedExp(mstack.getExperience());
				this.manager.removeStack(mstack.getId());
			}
			else {
				var newExperience = e.getDroppedExp() + mstack.getExperience();
				e.setDroppedExp(0);
				
				var location = entity.getLocation().clone();
				var world = location.getWorld();
				
				mstack.setRespawned(true);
				mstack.setExperience(newExperience);
				mstack.setSize(newSize);
				
				mstack.addDrops(e.getDrops());
				e.getDrops().clear();
				
				var newEntity = world.spawn(location, entity.getType().getEntityClass());
				newEntity.setMetadata("mobstack:uuid", new FixedMetadataValue(this.plugin, mstack.getId()));
				
				mstack.setEntity(newEntity);
				mstack.update();
			}
		}
	}
}