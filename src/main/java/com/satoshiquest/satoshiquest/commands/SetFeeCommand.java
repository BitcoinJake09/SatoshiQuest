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
		if (satoshiQuest.isStringInt(args[0])) {
			satoshiQuest.REDIS.set("FEE_BLOCK_CONF", args[0]);
			satoshiQuest.FEE_BLOCK_CONF = Integer.parseInt(args[0]);
			sender.sendMessage(ChatColor.GREEN + "fee set to " + args[0] + " blocks to confirm.");
			return true;	
		} else {
			sender.sendMessage(ChatColor.GREEN + "failed, please use an integer.");
			return false;
		}
	}
				return true;	
    }
}
