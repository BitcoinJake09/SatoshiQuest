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


public class LeaderBoardCommand extends CommandAction {
    private SatoshiQuest satoshiQuest;

    public LeaderBoardCommand(SatoshiQuest plugin) {
        this.satoshiQuest = plugin;
    }
    public boolean run(CommandSender sender, Command cmd, String label, String[] args, Player player) {
	Set<String> leaderBoardList = satoshiQuest.REDIS.keys("LeaderBoard *");
	sender.sendMessage(
                        ChatColor.BLUE
                            + "**List of Winners**");
	int iter=1;
		for (String templeaderBoardList : leaderBoardList) {
		sender.sendMessage(ChatColor.GREEN + "" + iter + ") " + satoshiQuest.REDIS.get(templeaderBoardList));
		iter++;
		}
	return true;	
    }
}
