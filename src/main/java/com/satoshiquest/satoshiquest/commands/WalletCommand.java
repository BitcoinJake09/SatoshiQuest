package com.satoshiquest.satoshiquest.commands;

import com.satoshiquest.satoshiquest.SatoshiQuest;
//import com.satoshiquest.satoshiquest.User;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import java.text.*;
import java.math.BigDecimal;

public class WalletCommand extends CommandAction {
  private SatoshiQuest satoshiQuest;

  public WalletCommand(SatoshiQuest plugin) {
    satoshiQuest = plugin;
  }

  public boolean run(
      CommandSender sender, Command cmd, String label, String[] args, Player player) {
try {
if (args[0].equalsIgnoreCase("help") || !(args.length >= 1)) {
	      player.sendMessage(ChatColor.GREEN + "/wallet - Displays your wallet info.");
              player.sendMessage(ChatColor.GREEN + "/wallet <set> <address> - will set your own win address to an address of your choosing instead of the ingame wallet. ");
	      player.sendMessage(ChatColor.GREEN + "/tip <amount> <playername> - Tip is used for player to player transactions.");
	      player.sendMessage(ChatColor.GREEN + "/withdraw <amount> <address> - withdraw is used for External transactions to an address.");

	      player.sendMessage(ChatColor.GREEN + "Your Deposit address on this server: " + satoshiQuest.getAccountAddress(player.getUniqueId().toString()));
String url = satoshiQuest.ADDRESS_URL + satoshiQuest.REDIS.get("nodeAddress"+ player.getUniqueId().toString());
      	      player.sendMessage(ChatColor.WHITE + "" + ChatColor.UNDERLINE + url);
	}
} catch (Exception e) {
      //e.printStackTrace();
      player.sendMessage(ChatColor.GREEN + "/wallet - Displays your wallet info.");
              player.sendMessage(ChatColor.GREEN + "/wallet <set> <address> - will set your own win address to an address of your choosing instead of the ingame wallet. ");
	      player.sendMessage(ChatColor.GREEN + "/tip <amount> <playername> - Tip is used for player to player transactions.");
	      player.sendMessage(ChatColor.GREEN + "/withdraw <amount> <address> - withdraw is used for External transactions to an address.");
    }

    try {
      //User user = new User(player);
      //satoshiQuest.getWalletInfo(player.getUniqueId().toString());
player.sendMessage(ChatColor.GREEN + "Your Deposit address on this server: " + satoshiQuest.getAccountAddress(player.getUniqueId().toString()));
String url = satoshiQuest.ADDRESS_URL + satoshiQuest.REDIS.get("nodeAddress"+ player.getUniqueId().toString());
      player.sendMessage(ChatColor.WHITE + "" + ChatColor.UNDERLINE + url);
      //Long balance1 = satoshiQuest.getBalance(player.getUniqueId().toString(),1);
      //Double playerCoinBalance1 = ((Double)(BigDecimal.valueOf(satoshiQuest.getBalance(player.getUniqueId().toString(),1)).doubleValue() * satoshiQuest.baseSat));
      //Long balance6 = satoshiQuest.getBalance(player.getUniqueId().toString(),6);
      Double playerCoinBalance6 = (Double)(BigDecimal.valueOf(satoshiQuest.getBalance(player.getUniqueId().toString(),satoshiQuest.CONFS_TARGET)).doubleValue() * satoshiQuest.baseSat);
      
      
      //Long unconfirmedBalance = satoshiQuest.getUnconfirmedBalance(player.getUniqueId().toString());
      Double playerCoinBalanceUnconfirmed = (Double)(BigDecimal.valueOf(satoshiQuest.getUnconfirmedBalance(player.getUniqueId().toString())).doubleValue() * satoshiQuest.baseSat);





      player.sendMessage(ChatColor.GREEN + "wallet balance with "+satoshiQuest.CONFS_TARGET+"-conf+: " + satoshiQuest.globalDecimalFormat.format(playerCoinBalance6));
      //player.sendMessage(ChatColor.GREEN + "wallet balance with 1-conf+: " + satoshiQuest.globalDecimalFormat.format(playerCoinBalance1));
      player.sendMessage(ChatColor.DARK_GREEN + "wallet unconfirmed: " + satoshiQuest.globalDecimalFormat.format(playerCoinBalanceUnconfirmed));
	if (SatoshiQuest.REDIS.exists("txFee" + player.getUniqueId().toString())) {
		player.sendMessage(ChatColor.GREEN + "player fee is set to " + SatoshiQuest.REDIS.get("txFee" + player.getUniqueId().toString()) + ""+ satoshiQuest.DENOMINATION_NAME +"/byte.");
	}
			DecimalFormat df = new DecimalFormat(SatoshiQuest.USD_DECIMALS);
	        	//System.out.print(df.format(exRate));
	player.sendMessage(ChatColor.GREEN + "1 " + satoshiQuest.COINGECKO_CRYPTO + " = $" + df.format(satoshiQuest.exRate));
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
