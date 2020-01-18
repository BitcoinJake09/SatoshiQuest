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
import java.text.*;


public class LeaderBoardCommand extends CommandAction {
    private SatoshiQuest satoshiQuest;

    public LeaderBoardCommand(SatoshiQuest plugin) {
        this.satoshiQuest = plugin;
    }
    public boolean run(CommandSender sender, Command cmd, String label, String[] args, Player player) {
	Set<String> ownerList = SatoshiQuest.REDIS.keys("LeaderBoard *");
			int iter=1;
				for (String tempOwnerList : ownerList) {
		if ((SatoshiQuest.REDIS.get("LeaderBoard "+iter)) != null) {					
			String tempString =  SatoshiQuest.REDIS.get("LeaderBoard "+iter);
			String lastWord = tempString.substring(tempString.lastIndexOf(" ")+1);
		double amtUSD = (double)(satoshiQuest.exRate * (Long.parseLong(lastWord) * 0.00000001));
		DecimalFormat df = new DecimalFormat("#.##");
					sender.sendMessage(ChatColor.GREEN +" "+iter+") "+ SatoshiQuest.REDIS.get("LeaderBoard "+iter) + " now $" + df.format(amtUSD));
		}
					iter++;
				}
	return true;	
    }
}
