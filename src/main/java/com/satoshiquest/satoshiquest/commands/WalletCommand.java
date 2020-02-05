package com.satoshiquest.satoshiquest.commands;

import com.satoshiquest.satoshiquest.SatoshiQuest;
//import com.satoshiquest.satoshiquest.User;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import java.text.*;

public class WalletCommand extends CommandAction {
  private SatoshiQuest satoshiQuest;

  public WalletCommand(SatoshiQuest plugin) {
    satoshiQuest = plugin;
  }

  public boolean run(
      CommandSender sender, Command cmd, String label, String[] args, Player player) {
if (args[0].equalsIgnoreCase("help")) {
	      player.sendMessage(ChatColor.GREEN + "/wallet - Displays your SatoshiQuest wallet info.");
              player.sendMessage(ChatColor.GREEN + "/wallet <set> <address> - will set your own win address to an address of your choosing instead of the ingame wallet. ");
	      player.sendMessage(ChatColor.GREEN + "/tip <amount> <playername> - Tip is used for player to player transactions.");
	      player.sendMessage(ChatColor.GREEN + "/send <amount> <address> - Send is used for External transactions to an address.");
try {
	      player.sendMessage(ChatColor.GREEN + "Your Deposit address on this server: " + satoshiQuest.getAccountAddress(player.getUniqueId().toString()));
String url = satoshiQuest.ADDRESS_URL + satoshiQuest.REDIS.get("nodeAddress"+ player.getUniqueId().toString());
      	      player.sendMessage(ChatColor.WHITE + "" + ChatColor.UNDERLINE + url);
    } catch (Exception e) {
      e.printStackTrace();
      player.sendMessage(ChatColor.RED + "There was a problem reading your wallet. try again soon.");
    }
	return true;
	}

    try {
      //User user = new User(player);
      //satoshiQuest.getWalletInfo(player.getUniqueId().toString());
player.sendMessage(ChatColor.GREEN + "Your Deposit address on this server: " + satoshiQuest.getAccountAddress(player.getUniqueId().toString()));
String url = satoshiQuest.ADDRESS_URL + satoshiQuest.REDIS.get("nodeAddress"+ player.getUniqueId().toString());
      player.sendMessage(ChatColor.WHITE + "" + ChatColor.UNDERLINE + url);
      Long balance1 = satoshiQuest.getBalance(player.getUniqueId().toString(),1);
      Long balance6 = satoshiQuest.getBalance(player.getUniqueId().toString(),6);
      Long unconfirmedBalance = satoshiQuest.getUnconfirmedBalance(player.getUniqueId().toString());
      player.sendMessage(ChatColor.GREEN + "wallet balance with 1-conf+: " + balance1);
      player.sendMessage(ChatColor.GREEN + "wallet balance with 6-conf+: " + balance6);
      player.sendMessage(ChatColor.DARK_GREEN + "wallet unconfirmed: " + unconfirmedBalance);
			DecimalFormat df = new DecimalFormat("#.##");
	        	//System.out.print(df.format(exRate));
	player.sendMessage(ChatColor.GREEN + "1 btc = $" + df.format(satoshiQuest.exRate));
	if (satoshiQuest.REDIS.exists("ExternalAddress" +player.getUniqueId().toString())) {
	player.sendMessage(ChatColor.GREEN + "On win address set to: " + satoshiQuest.REDIS.get("ExternalAddress" +player.getUniqueId().toString()));
	} else {
	player.sendMessage(ChatColor.YELLOW + "On win address not set, default is ingame wallet.");
	player.sendMessage(ChatColor.YELLOW + "to set your On Win address to send funds to if won instead of your ingame wallet use command: (/wallet set <wallet address>) ");
	}

      	if (args.length > 0) {
	if (args[0].equalsIgnoreCase("del")) {
	satoshiQuest.REDIS.del("ExternalAddress" +player.getUniqueId().toString());
	player.sendMessage(ChatColor.RED + "Your On Win address has been deleted");
	} else if ((args[0].equalsIgnoreCase("set")) && (args.length > 1)) {
	satoshiQuest.REDIS.set("ExternalAddress" +player.getUniqueId().toString(), args[1]);
	player.sendMessage(ChatColor.GREEN + "Set your On Win address to: " + satoshiQuest.REDIS.get("ExternalAddress" +player.getUniqueId().toString()));
	
	} else {
	player.sendMessage(ChatColor.YELLOW + "to set your On Win address to send funds to if won instead of your ingame wallet use command: (/wallet set <wallet address>) ");
	}
	}

      satoshiQuest.updateScoreboard(player);
    } catch (Exception e) {
      e.printStackTrace();
      player.sendMessage(ChatColor.RED + "There was a problem reading your wallet.");
    }

    return true;
  }
}
