package com.satoshiquest.satoshiquest.commands;

import com.satoshiquest.satoshiquest.SatoshiQuest;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class TipCommand extends CommandAction {
  private SatoshiQuest satoshiQuest;

  public TipCommand(SatoshiQuest plugin) {
    satoshiQuest = plugin;
  }

  public boolean run(
      CommandSender sender, Command cmd, String label, String[] args, final Player player) {
if (args[0].equalsIgnoreCase("help")) {
	      player.sendMessage(ChatColor.GREEN + "/tip <amount> <playername> - Tip is used for player to player transactions.");
	return true;
	}

    //int MAX_SEND = 10000; // to be multiplied by DENOMINATION_FACTOR
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

        for (final Player onlinePlayer : Bukkit.getOnlinePlayers()) {
          if (onlinePlayer.getName().equalsIgnoreCase(args[1])) {
            if (!args[1].equalsIgnoreCase(player.getDisplayName())) {
              try {
                

                Long balance = satoshiQuest.getBalance(player.getUniqueId().toString(),1);

                if (balance >= sat) {
                  // TODO: Pay to user address
		  String didSend = satoshiQuest.sendToAddress(player.getUniqueId().toString(),satoshiQuest.REDIS.get("nodeAddress"+onlinePlayer.getUniqueId().toString()), sat);
                  if (didSend != "failed") {
                    satoshiQuest.updateScoreboard(onlinePlayer);
                    satoshiQuest.updateScoreboard(player);
                    player.sendMessage(
                        ChatColor.GREEN
                            + "You sent "
                            + ChatColor.LIGHT_PURPLE
                            + amount
                            + " "
                            + SatoshiQuest.DENOMINATION_NAME
                            + ChatColor.GREEN
                            + " to user "
                            + ChatColor.BLUE
                            + onlinePlayer.getName());
                    onlinePlayer.sendMessage(
                        ChatColor.GREEN
                            + "You got "
                            + ChatColor.LIGHT_PURPLE
                            + amount
                            + " "
                            + SatoshiQuest.DENOMINATION_NAME
                            + ChatColor.GREEN
                            + " from user "
                            + ChatColor.BLUE
                            + player.getName());
                  } else {
                    player.sendMessage(ChatColor.RED + "Tip failed.");
                  }
                } else {
                  player.sendMessage(ChatColor.DARK_RED + "Not enough balance");
                }
              } catch (Exception e) {
                player.sendMessage(ChatColor.DARK_RED + "Error. Please try again later.");
                System.out.println(e);
              }
            }
          }
        }
      } else {
        player.sendMessage(
            "error sending that amount.");
      }
    } else {
      return false;
    }
	try {
	      satoshiQuest.updateScoreboard(player);
	} catch(Exception e) {
					e.printStackTrace();
				}
    return true;
  }
}

