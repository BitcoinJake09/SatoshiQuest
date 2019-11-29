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
      satoshiQuest.getWalletInfo(player.getUniqueId().toString());
      Long balance1 = satoshiQuest.getBalance(player.getUniqueId().toString(),1);
      Long balance6 = satoshiQuest.getBalance(player.getUniqueId().toString(),6);
      Long unconfirmedBalance = satoshiQuest.getUnconfirmedBalance(player.getUniqueId().toString());
      player.sendMessage(ChatColor.GREEN + "wallet balance with 1-conf+: " + balance1);
      player.sendMessage(ChatColor.GREEN + "wallet balance with 6-conf+: " + balance6);
      player.sendMessage(ChatColor.DARK_GREEN + "wallet unconfirmed: " + unconfirmedBalance);


      satoshiQuest.updateScoreboard(player);
    } catch (Exception e) {
      e.printStackTrace();
      player.sendMessage(ChatColor.RED + "There was a problem reading your wallet.");
    }

    return true;
  }
}
