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


public class SetFeeCommand extends CommandAction {
    private SatoshiQuest satoshiQuest;

    public SetFeeCommand(SatoshiQuest plugin) {
        this.satoshiQuest = plugin;
    }

    public boolean run(CommandSender sender, Command cmd, String label, String[] args, Player player) {
	if (args.length == 1) {
		if (args[0].equalsIgnoreCase("help")) {
			sender.sendMessage(ChatColor.GREEN + "please use a "+satoshiQuest.DENOMINATION_NAME+" amount between "+satoshiQuest.MIN_FEE+" - "+satoshiQuest.MAX_FEE+" "+satoshiQuest.DENOMINATION_NAME+"s/byte.");
			sender.sendMessage(ChatColor.GREEN + "/setfee <#>");
			return false;
		}
		try {
		if ((satoshiQuest.isStringDouble(args[0])) || (satoshiQuest.isStringInt(args[0]))) {
			if ((Double.parseDouble(args[0]) <= satoshiQuest.MAX_FEE) && (Double.parseDouble(args[0]) >= 1.2)) {
				boolean setFee = satoshiQuest.setSatByte(player.getUniqueId().toString(), Double.parseDouble(args[0]));
				System.out.println("set to " + args[0] + ""+satoshiQuest.DENOMINATION_NAME+"s/byte: "+setFee);
				SatoshiQuest.REDIS.set("txFee" + player.getUniqueId().toString(),args[0]);
				sender.sendMessage(ChatColor.GREEN + "Your wallet fee has been set to " + args[0] + ""+satoshiQuest.DENOMINATION_NAME+"s/b");
				return true;
			} else {
				sender.sendMessage(ChatColor.RED + "failed, please use a "+satoshiQuest.DENOMINATION_NAME+" amount between "+satoshiQuest.MIN_FEE+" - "+satoshiQuest.MAX_FEE+" "+satoshiQuest.DENOMINATION_NAME+"s/byte.");
				sender.sendMessage(ChatColor.GREEN + "/setfee <#>");
				return false;
			}
		} else {
			sender.sendMessage(ChatColor.RED + "failed, please use a "+satoshiQuest.DENOMINATION_NAME+" amount between "+satoshiQuest.MIN_FEE+" - "+satoshiQuest.MAX_FEE+" "+satoshiQuest.DENOMINATION_NAME+"s/byte.");
			sender.sendMessage(ChatColor.GREEN + "/setfee <#>");
			return false;
		}
		} catch (Exception e) {
		//e.printStackTrace();
		player.sendMessage(ChatColor.RED + "There was a problem updating your fee.");
    	}
	} else if (args.length == 0) {
		sender.sendMessage(ChatColor.GREEN + "please use a "+satoshiQuest.DENOMINATION_NAME+" amount between "+satoshiQuest.MIN_FEE+" - "+satoshiQuest.MAX_FEE+" "+satoshiQuest.DENOMINATION_NAME+"s/byte.");
		sender.sendMessage(ChatColor.GREEN + "/setfee <#>");
		return false;
	}

				return true;	
    }
}
