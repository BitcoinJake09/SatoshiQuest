package com.satoshiquest.satoshiquest.commands;

import com.satoshiquest.satoshiquest.SatoshiQuest;
import org.bukkit.Bukkit;
import org.bukkit.*;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.attribute.Attribute;
import org.bukkit.advancement.*;
import java.util.*;


public class ResetCommand extends CommandAction {
    private SatoshiQuest satoshiQuest;

    public ResetCommand(SatoshiQuest plugin) {
        this.satoshiQuest = plugin;
    }
    public boolean run(CommandSender sender, Command cmd, String label, String[] args, Player player) {
	if (args.length == 0) {
		satoshiQuest.REDIS.del("lootSpawnY");
		satoshiQuest.REDIS.del("spawnCreated");
		satoshiQuest.REDIS.set("gameRound","1");
		satoshiQuest.REDIS.set("winner","true");
	    for(OfflinePlayer offlinePlayer : Bukkit.getOfflinePlayers()) {
		satoshiQuest.REDIS.set("ClearInv" +offlinePlayer.getUniqueId().toString(), "true");
		}

	for(Player p : Bukkit.getServer().getWorld(satoshiQuest.SERVERDISPLAY_NAME).getPlayers()) {
                p.teleport(Bukkit.getWorlds().get(0).getSpawnLocation());
		satoshiQuest.REDIS.del("ClearInv" +p.getUniqueId().toString());
		PlayerInventory pli1= p.getInventory();
		pli1.clear(); //delete player world datas
		pli1.setArmorContents(new ItemStack[4]);
		Inventory pe1  = p.getEnderChest();
		pe1.clear();
		p.setLevel(0);
		p.setExp(0);
		p.setExhaustion(0);
		p.setFoodLevel(20);
		p.setHealth(p.getAttribute(Attribute.GENERIC_MAX_HEALTH).getValue());
	}
	    for(OfflinePlayer offlinePlayer : Bukkit.getOfflinePlayers()) {
		if (satoshiQuest.REDIS.exists("LootAnnounced" +offlinePlayer.getUniqueId().toString())) {
		satoshiQuest.REDIS.del("LootAnnounced" +offlinePlayer.getUniqueId().toString());
		}
		}
            	Bukkit.getServer().unloadWorld(Bukkit.getServer().getWorld(satoshiQuest.SERVERDISPLAY_NAME), false);
Bukkit.getServer().unloadWorld(Bukkit.getServer().getWorld(satoshiQuest.SERVERDISPLAY_NAME+"_the_end"), false);
Bukkit.getServer().unloadWorld(Bukkit.getServer().getWorld(satoshiQuest.SERVERDISPLAY_NAME+"_nether"), false);
		satoshiQuest.deleteLootWorlds();
		satoshiQuest.onEnable();
		satoshiQuest.REDIS.del("winner");
		return true;	
	} else {
        	return false;
        }
    }
}
