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


public class FixLeaderBoardCommand extends CommandAction {
    private SatoshiQuest satoshiQuest;

    public FixLeaderBoardCommand(SatoshiQuest plugin) {
        this.satoshiQuest = plugin;
    }

    public boolean run(CommandSender sender, Command cmd, String label, String[] args, Player player) {
	int ArgsLength = args.length;
		if (args[0].equalsIgnoreCase("List")){
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
		}
		if (args[0].equalsIgnoreCase("Del")){
			Set<String> ownerList = SatoshiQuest.REDIS.keys("LeaderBoard *");
			int iter=1;
			for (String tempOwnerList : ownerList) {
				if (iter == Integer.parseInt(args[1])) {
			 		if ((SatoshiQuest.REDIS.get("LeaderBoard "+iter)) != null) {
						sender.sendMessage(ChatColor.YELLOW + "Removing: " + "LeaderBoard "+SatoshiQuest.REDIS.get("LeaderBoard "+iter));
          					SatoshiQuest.REDIS.del("LeaderBoard "+iter);
          					sender.sendMessage(ChatColor.GREEN + "Removed: " + "LeaderBoard "+args[1]);
					}
				}
				iter++;
			}
		}
		if (args[0].equalsIgnoreCase("DelAll")){
			Set<String> ownerList = SatoshiQuest.REDIS.keys("LeaderBoard *");
			int iter=1;
			for (String tempOwnerList : ownerList) {
			 		if ((SatoshiQuest.REDIS.get(tempOwnerList)) != null) {
						sender.sendMessage(ChatColor.YELLOW + "Removing: " + "LeaderBoard "+SatoshiQuest.REDIS.get(tempOwnerList));
          					SatoshiQuest.REDIS.del(tempOwnerList);
          					sender.sendMessage(ChatColor.GREEN + "Removed: " + tempOwnerList);
					}
				iter++;
			}
		}
		if (args[0].equalsIgnoreCase("fix")){
			Set<String> ownerList = SatoshiQuest.REDIS.keys("LeaderBoard *");
			int iter=1;
			for (String tempOwnerList : ownerList) {
				if (iter == Integer.parseInt(args[1])) {
			 		if ((SatoshiQuest.REDIS.get("LeaderBoard "+iter)) != null) {
						sender.sendMessage(ChatColor.YELLOW + "Changing: " + "LeaderBoard "+SatoshiQuest.REDIS.get("LeaderBoard "+iter));
						String toChange = "";
        					for (int x = 2; x<= args.length-1; x++) {
							toChange = toChange + " " + args[x];
						}
//REDIS.set("LeaderBoard " + iter,dateFormat.format(date) + " " + player.getName() + " $" + df.format(amtUSD) + " Satoshis: " + sendLoot);
       					SatoshiQuest.REDIS.del("LeaderBoard "+ args[2]);
					SatoshiQuest.REDIS.set("LeaderBoard "+ args[2], toChange);
        				sender.sendMessage(ChatColor.GREEN + "Changed: " + "LeaderBoard "+toChange);
					}
				}
			iter++;
			}
		}
		if (args[0].equalsIgnoreCase("add")){
			String toChange = "";
			for (int x = 1; x<= args.length-1; x++) {
				toChange = toChange + " " + args[x];
			}
			Set<String> ownerList = SatoshiQuest.REDIS.keys("LeaderBoard *");
			int iter=1;
			for (String tempOwnerList : ownerList) {
				if ((SatoshiQuest.REDIS.get("LeaderBoard "+iter)) != null) {
				iter++;
				} else {break;}
			}
			sender.sendMessage(ChatColor.YELLOW + "adding: " + "LeaderBoard " +iter + " " + toChange);
			SatoshiQuest.REDIS.set("LeaderBoard " +iter, toChange);
			sender.sendMessage(ChatColor.GREEN + "added: " + "LeaderBoard " +iter + " " +toChange);
		}
		consolidateLeaderBoard();
		return true;
 }
 public void consolidateLeaderBoard() {
Set<String> ownerList = SatoshiQuest.REDIS.keys("LeaderBoard *");
			int iter=1;
			int tempIter=iter+1;
				for (String tempOwnerList : ownerList) {
		if ((SatoshiQuest.REDIS.get("LeaderBoard "+tempIter)) != null && ((SatoshiQuest.REDIS.get("LeaderBoard "+iter)) == null)) {
					SatoshiQuest.REDIS.set("LeaderBoard " +iter, SatoshiQuest.REDIS.get("LeaderBoard " +tempIter));
					SatoshiQuest.REDIS.del("LeaderBoard "+tempIter);
		}
					iter++;
					tempIter++;
				}
	}
}
