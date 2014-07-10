package net.kylemc.kadmin;

import org.bukkit.EntityEffect;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Chicken;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.inventory.ItemStack;

public final class FeatherClip implements Listener
{
	@EventHandler
	public void ClipWing(PlayerInteractEntityEvent event){
		//Clip wings from chickens with shears
		Player player = event.getPlayer();
		Entity e = event.getRightClicked();
		Location eloc = e.getLocation();
		if(player.getItemInHand().getType() == Material.SHEARS && e.getType() == EntityType.CHICKEN){
			//If holding shears and clicked entity is a chicken
			Chicken c = (Chicken) e;
			ItemStack i = new ItemStack(Material.FEATHER, 1);
			eloc.getWorld().dropItemNaturally(eloc, i);
			if(!(player.getGameMode() == GameMode.CREATIVE)){
				//If player is Survival, damage shears
				player.getItemInHand().setDurability((short)(player.getItemInHand().getDurability()+1));
				if(player.getItemInHand().getDurability() >= 238){
					//If shears are broken
					player.setItemInHand(null);
				}
			}
			c.setHealth((c.getHealth())-1.0);
			c.playEffect(EntityEffect.HURT);
			if(c.getAge() >= 0){
				//If chicken is mature
				player.getWorld().playSound(eloc, Sound.CHICKEN_HURT, 10, 1);
			}
			else{
				//Play chick hurt sound
				player.getWorld().playSound(eloc, Sound.CHICKEN_HURT, 10, 2);
			}
			if((c.getHealth()) <= 0.0){
				//If Chicken is dead, remove it
				e.remove();
				e.playEffect(EntityEffect.DEATH);
			}
		}
	}
}