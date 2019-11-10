package com.satoshiquest.satoshiquest.commands;

import com.satoshiquest.satoshiquest.SatoshiQuest;
import java.util.Set;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class BanlistCommand extends CommandAction {
  public boolean run(
      CommandSender sender, Command cmd, String label, String[] args, Player player) {
    Set<String> banlist = SatoshiQuest.REDIS.smembers("banlist");
    for (String uuid : banlist) {
      sender.sendMessage(ChatColor.YELLOW + SatoshiQuest.REDIS.get("name:" + uuid));
    }
    return true;
  }
}
