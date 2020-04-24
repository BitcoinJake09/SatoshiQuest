package com.satoshiquest.satoshiquest.commands;

import com.satoshiquest.satoshiquest.SatoshiQuest;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Set;


public class VoteCommand extends CommandAction {
  private SatoshiQuest satoshiQuest;

  public VoteCommand(SatoshiQuest plugin) {
    satoshiQuest = plugin;
  }
    public boolean run(CommandSender sender, Command cmd, String label, String[] args, Player player) {
	String playerName = player.getName(); //(player.getName()
	if (satoshiQuest.didVote(playerName) == 0) {
		player.sendMessage(ChatColor.GREEN + "Please Vote here for 10% off lives! " + satoshiQuest.VOTE_URL);
		//player.sendMessage(ChatColor.AQUA + "Run command again after you vote for reward!");
	} else if (satoshiQuest.didVote(playerName) == 1) {
		player.sendMessage(ChatColor.GREEN + "You've already voted, try again later please!");

	} else if (satoshiQuest.didVote(playerName) == 2) {
		player.sendMessage(ChatColor.GREEN + "You've already voted, try again later please!");
	} 
        return true;
    }
}
