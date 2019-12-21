package com.satoshiquest.satoshiquest.commands;

import com.satoshiquest.satoshiquest.SatoshiQuest;
import java.util.Set;
import java.util.UUID;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class SetLivesCommand extends CommandAction {
  private SatoshiQuest satoshiQuest;

  public SetLivesCommand(SatoshiQuest plugin) {
    satoshiQuest = plugin;
  }
  public boolean run(
      CommandSender sender, Command cmd, String label, String[] args, Player player) {
	if (args.length > 0) {
		if (args[0].equals("set")) {
		        if (args.length > 1) {
				if (SatoshiQuest.REDIS.exists("uuid:" + args[1])) {
					UUID uuid = UUID.fromString(SatoshiQuest.REDIS.get("uuid:" + args[1]));
					if (satoshiQuest.isStringInt(args[2])) { 
						SatoshiQuest.REDIS.set("LivesLeft" +uuid.toString(), args[2]);
						sender.sendMessage(ChatColor.GREEN + " given " + args[2] + " lives");
					} else {
						sender.sendMessage(ChatColor.GREEN + " needs to be a number of lives.");
						sender.sendMessage(ChatColor.RED + "Usage: /setlives set <player> <amount>");
					}

					return true;
				} else {
					sender.sendMessage(ChatColor.RED + "Cannot find player " + args[1]);
					return true;
				}
			} else {
				sender.sendMessage(ChatColor.RED + "Usage: /setlives set <player> <amount>");
				return true;
			}
		} else {
			sender.sendMessage(ChatColor.RED + "Usage: /setlives set <player> <amount>");
			return true;
		}
	} else {
		sender.sendMessage(ChatColor.RED + "Usage: /setlives set <player> <amount>");
		return true;
    }
  }
}
