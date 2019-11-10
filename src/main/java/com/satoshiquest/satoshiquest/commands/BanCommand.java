package com.satoshiquest.satoshiquest.commands;

import com.satoshiquest.satoshiquest.SatoshiQuest;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class BanCommand extends CommandAction {
  public boolean run(
      CommandSender sender, Command cmd, String label, String[] args, Player player) {
    if (args.length == 1) {
      String playerName = args[0];

      if (SatoshiQuest.REDIS.exists("uuid:" + playerName)) {
        String uuid = SatoshiQuest.REDIS.get("uuid:" + playerName);
        SatoshiQuest.REDIS.sadd("banlist", uuid);
        Player kickedout = Bukkit.getPlayer(playerName);

        if (kickedout != null) {
          kickedout.kickPlayer("Sorry.");
        }

        sender.sendMessage(ChatColor.GREEN + "Player " + playerName + " is now banned.");

        return true;

      } else {
        sender.sendMessage(ChatColor.RED + "Can't find player " + playerName);
        return true;
      }
    } else {
      return false;
    }
  }
}
