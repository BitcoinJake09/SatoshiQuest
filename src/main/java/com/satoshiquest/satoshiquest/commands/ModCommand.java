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


public class ModCommand extends CommandAction {
    private SatoshiQuest satoshiQuest;

    public ModCommand(SatoshiQuest plugin) {
        this.satoshiQuest = plugin;
    }
    public boolean run(CommandSender sender, Command cmd, String label, String[] args, Player player) {
        if(args[0].equalsIgnoreCase("add")) {
            // Sub-command: /mod add

            if(SatoshiQuest.REDIS.exists("uuid:"+args[1])) {
                UUID uuid=UUID.fromString(SatoshiQuest.REDIS.get("uuid:"+args[1]));
                SatoshiQuest.REDIS.sadd("moderators",uuid.toString());
                sender.sendMessage(ChatColor.GREEN+SatoshiQuest.REDIS.get("name:"+uuid)+" added to moderators group");
                return true;
            } else {
                sender.sendMessage(ChatColor.RED+"Cannot find player "+args[1]);
                return true;
            }
        } else if(args[0].equalsIgnoreCase("remove")) {
            // Sub-command: /mod del
            if(SatoshiQuest.REDIS.exists("uuid:"+args[1])) {
                UUID uuid=UUID.fromString(SatoshiQuest.REDIS.get("uuid:"+args[1]));
                SatoshiQuest.REDIS.srem("moderators",uuid.toString());
                return true;
            }
            return false;
        } else if(args[0].equalsIgnoreCase("list")) {
            // Sub-command: /mod list
            Set<String> moderators=SatoshiQuest.REDIS.smembers("moderators");
            for(String uuid:moderators) {
                sender.sendMessage(ChatColor.YELLOW+SatoshiQuest.REDIS.get("name:"+uuid));
            }
            return true;
        } else if(args[0].equalsIgnoreCase("flag")) {
	try{
	if (!(SatoshiQuest.REDIS.exists("ModFlag"))){
		SatoshiQuest.REDIS.set("ModFlag","true");
		player.sendMessage(ChatColor.RED + "ModFlag is ON");
	 }         
	else if (SatoshiQuest.REDIS.get("ModFlag").equals("false")){
		SatoshiQuest.REDIS.set("ModFlag","true");
		player.sendMessage(ChatColor.RED + "ModFlag is ON");
           }
	 else {
		SatoshiQuest.REDIS.set("ModFlag","false");
		player.sendMessage(ChatColor.RED + "ModFlag is OFF");
           }
		} catch (NullPointerException nullPointer)
		{
                	System.out.println("modflag: "+nullPointer);
		}

	return true;	
	} else if(args[0].equalsIgnoreCase("beta")) {
	try{
	if (!(SatoshiQuest.REDIS.exists("BetaTest"))){
		SatoshiQuest.REDIS.set("BetaTest","true");
		player.sendMessage(ChatColor.RED + "BetaTest is ON");
	 } else {
		SatoshiQuest.REDIS.del("BetaTest");
		player.sendMessage(ChatColor.RED + "BetaTest is OFF");
           }
		} catch (NullPointerException nullPointer)
		{
                	System.out.println("BetaTest: "+nullPointer);
		}

	return true;	
	} else {
            return false;
        }
    }
}
