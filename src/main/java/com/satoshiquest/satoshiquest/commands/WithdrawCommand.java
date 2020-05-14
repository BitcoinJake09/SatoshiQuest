package com.satoshiquest.satoshiquest.commands;

import com.satoshiquest.satoshiquest.SatoshiQuest;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class WithdrawCommand extends CommandAction {
  private SatoshiQuest satoshiQuest;

  public WithdrawCommand(SatoshiQuest plugin) {
    satoshiQuest = plugin;
  }

  public boolean run(
      CommandSender sender, Command cmd, String label, String[] args, final Player player) {
try {
if (args[1].equalsIgnoreCase("help") || !(args.length >= 1)) {
	      player.sendMessage(ChatColor.GREEN + "/withdraw <address> <amount> - withdraw is used for External transactions to an address.");
	}
} catch (Exception e) {
      //e.printStackTrace();
      player.sendMessage(ChatColor.GREEN + "/withdraw <address> <amount> - withdraw is used for External transactions to an address.");
    }
    if (args.length == 2) {
      final Long sat = satoshiQuest.convertCoinToSats(Double.parseDouble(args[1]));
      for (char c : sat.toString().toCharArray()) {
        if (!Character.isDigit(c)) return false;
      }
      if (args[1].length() > 10) {
        // maximum send is 10 digits
        return false;
      }



      if (sat != 0) {


            if (!args[0].equalsIgnoreCase(player.getDisplayName())) {
              try {

                Long balance = satoshiQuest.getBalance(player.getUniqueId().toString(),satoshiQuest.CONFS_TARGET);

                if (balance >= sat) {
                  // TODO: Pay to user address
    		boolean setFee = satoshiQuest.setSatByte(player.getUniqueId().toString(), Double.parseDouble(SatoshiQuest.REDIS.get("txFee" + player.getUniqueId().toString())));
		  String didSend = satoshiQuest.sendToAddress(player.getUniqueId().toString(),args[0].toString(), sat);
                  if (didSend != "failed") {
                    satoshiQuest.updateScoreboard(player);
                    player.sendMessage(
                        ChatColor.GREEN
                            + "Your withdraw "
                            + ChatColor.LIGHT_PURPLE
                            + satoshiQuest.globalDecimalFormat.format(satoshiQuest.convertSatsToCoin(sat))
                            + " "
                            + SatoshiQuest.CRYPTO_TICKER
                            + ChatColor.GREEN
                            + " to address "
                            + ChatColor.YELLOW
                            + args[0].toString()
			    + ChatColor.BLUE + " "+ satoshiQuest.TX_URL + didSend);
                  } else {
                    player.sendMessage(ChatColor.RED + "withdraw failed.");
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
	try {
	      satoshiQuest.updateScoreboard(player);
	} catch(Exception e) {
					e.printStackTrace();
				}
    return true;
  }
}

