package com.satoshiquest.satoshiquest.commands;

import com.satoshiquest.satoshiquest.SatoshiQuest;
import com.satoshiquest.satoshiquest.User;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class WalletCommand extends CommandAction {
  private SatoshiQuest satoshiQuest;

  public WalletCommand(SatoshiQuest plugin) {
    satoshiQuest = plugin;
  }

  public boolean run(
      CommandSender sender, Command cmd, String label, String[] args, Player player) {

    try {
      User user = new User(player);
      satoshiQuest.sendWalletInfo(player, user);
      satoshiQuest.updateScoreboard(player);
    } catch (Exception e) {
      e.printStackTrace();
      player.sendMessage(ChatColor.RED + "There was a problem reading your wallet.");
    }

    return true;
  }
}
