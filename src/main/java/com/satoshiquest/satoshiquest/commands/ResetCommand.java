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
import java.util.*;


public class ResetCommand extends CommandAction {
    private SatoshiQuest satoshiQuest;

    public ResetCommand(SatoshiQuest plugin) {
        this.satoshiQuest = plugin;
    }
    public boolean run(CommandSender sender, Command cmd, String label, String[] args, Player player) {
	if (args.length == 0) {
		satoshiQuest.REDIS.del("lootSpawnY");
		satoshiQuest.REDIS.set("gameRound","1");
	    for(OfflinePlayer offlinePlayer : Bukkit.getOfflinePlayers()) {
		if (satoshiQuest.REDIS.exists("LootAnnounced" +offlinePlayer.getUniqueId().toString())) {
		satoshiQuest.REDIS.del("LootAnnounced" +offlinePlayer.getUniqueId().toString());
		}
		}
		return true;	
	} else {
        	return false;
        }
    }
}
