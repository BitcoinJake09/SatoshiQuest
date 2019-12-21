package com.satoshiquest.satoshiquest.commands;

import com.satoshiquest.satoshiquest.SatoshiQuest;
import com.satoshiquest.satoshiquest.User;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class SendCommand extends CommandAction {
  private SatoshiQuest satoshiQuest;

  public SendCommand(SatoshiQuest plugin) {
    satoshiQuest = plugin;
  }

  public boolean run(
      CommandSender sender, Command cmd, String label, String[] args, final Player player) {
    if (args.length == 2) {
      for (char c : args[0].toCharArray()) {
        if (!Character.isDigit(c)) return false;
      }
      if (args[0].length() > 8) {
        // maximum send is 8 digits
        return false;
      }
      final Long amount = Long.parseLong(args[0]);
      final Long sat = amount * SatoshiQuest.DENOMINATION_FACTOR;

      if (amount != 0) {


            if (!args[1].equalsIgnoreCase(player.getDisplayName())) {
              try {

                Long balance = satoshiQuest.getBalance(player.getUniqueId().toString(),1);

                if (balance >= sat) {
                  // TODO: Pay to user address
		  String didSend = satoshiQuest.sendToAddress(player.getUniqueId().toString(),args[1].toString(), sat);
                  if (didSend != "failed") {
                    satoshiQuest.updateScoreboard(player);
                    player.sendMessage(
                        ChatColor.GREEN
                            + "You sent "
                            + ChatColor.LIGHT_PURPLE
                            + sat
                            + " "
                            + SatoshiQuest.DENOMINATION_NAME
                            + ChatColor.GREEN
                            + " to address "
                            + ChatColor.BLUE
                            + args[1].toString()
			    + " with txid: " + didSend);
                  } else {
                    player.sendMessage(ChatColor.RED + "send failed.");
                  }
                } else {
                  player.sendMessage(ChatColor.DARK_RED + "Not enough balance");
                }
              } catch (Exception e) {
                player.sendMessage(ChatColor.DARK_RED + "Error. Please try again later.");
                System.out.println(e);
              }
        }
      } else {
        player.sendMessage(
            "error with that amount.");
      }
    } else {
      return false;
    }
    return true;
  }
}

