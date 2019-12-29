package com.satoshiquest.satoshiquest.commands;

import com.satoshiquest.satoshiquest.SatoshiQuest;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.Bukkit;
import java.util.UUID;

public class LivesCommand extends CommandAction {
  private SatoshiQuest satoshiQuest;

  public LivesCommand(SatoshiQuest plugin) {
    satoshiQuest = plugin;
  }

  public boolean run(
      CommandSender sender, Command cmd, String label, String[] args, Player player) {
	//satoshiQuest.REDIS.get("LivesLeft" + player.getUniqueId().toString())
	Long balance = 0L;
   if (args.length > 0) {

      if (args[0].equals("help")) {

    try {
      satoshiQuest.getWalletInfo(player.getUniqueId().toString());
      balance = satoshiQuest.getBalance(player.getUniqueId().toString(),1);
      player.sendMessage(ChatColor.GREEN + "wallet balance: " + balance);
    } catch (Exception e) {
      e.printStackTrace();
      player.sendMessage(ChatColor.RED + "There was a problem reading your wallet.");
    }
	player.sendMessage(ChatColor.GREEN + "Lives are $" + satoshiQuest.BUYIN_AMOUNT + " USD each in BTC. Most goes into Loot wallet which everyone searches for the treasure, once found that player will recive funds to their player wallet if no external wallet set and the world will reset for a new hunt. A little bit is set aside for further developent and hosting.");
	if (balance == 0) {
	player.sendMessage(ChatColor.RED + "Looks like you dont have enough funds, try the (/wallet) command to check your balance and deposit address.");
	} else if (balance > 0) {
	player.sendMessage(ChatColor.YELLOW + "You can use the command (/Lives [Number]) like (/Lives 1) or (/Lives 2)");
	player.sendMessage(ChatColor.YELLOW + "But to complete it you need to say buy to confirm so (/Lives 3 buy) will get you 3 lives for " + (3 * satoshiQuest.totalLifeRate));
	player.sendMessage(ChatColor.GREEN + "Lives are transferable between players with (/Lives [Number] send [playername])");
	}

	} else if ((satoshiQuest.isStringInt(args[0])) && (args.length <= 2)) {  //end help
		if (Integer.parseInt(args[0]) > 0) {
			try {
     				balance = satoshiQuest.getBalance(player.getUniqueId().toString(),1);
			} catch (Exception e) {
				e.printStackTrace();
			}
			int livesAmount = Integer.valueOf(args[0]);
			Long sendLoot = satoshiQuest.livesRate * livesAmount;
			Long sendAdmin = satoshiQuest.adminRate * livesAmount;
			Long totalBuyingBTC = satoshiQuest.totalLifeRate * livesAmount;
			if (args.length == 3) {
				player.sendMessage(ChatColor.YELLOW + "Bought " + livesAmount + " Lives for " + totalBuyingBTC + " with " + sendLoot + " going into the loot treasure and " + sendAdmin + " going to the admin");
			} else {
				player.sendMessage(ChatColor.YELLOW + "Buy " + livesAmount + " Lives for " + totalBuyingBTC + " with " + sendLoot + " going into the loot treasure and " + sendAdmin + " going to the admin");
			}
			if (args.length == 2) {
				if (args[1].equalsIgnoreCase("buy")) {
				try {
					String result = satoshiQuest.sendMany(player.getUniqueId().toString(), satoshiQuest.REDIS.get("nodeAddress"+satoshiQuest.SERVERDISPLAY_NAME), satoshiQuest.ADMIN_ADDRESS, sendLoot, sendAdmin, 24);
      			//Long newBalance = satoshiQuest.getBalance(player.getUniqueId().toString(),1);
				if (result != "failed") {
					String setLives = Integer.toString(((Integer.valueOf(satoshiQuest.REDIS.get("LivesLeft" +player.getUniqueId().toString()))) + (satoshiQuest.LIVES_PERBUYIN * livesAmount)));
					satoshiQuest.REDIS.set("LivesLeft" +player.getUniqueId().toString(), setLives);
					player.sendMessage(ChatColor.GREEN + "You just got " + (satoshiQuest.LIVES_PERBUYIN * livesAmount) + " lives!");
				} else if (result == "failed") {
					player.sendMessage(ChatColor.RED + "Buy lives failed, may be due to not enough confirmed balance or enought to pay tx fee. try /wallet to check balance and confirmations.");
				}
				System.out.println("[LivesBuy] " + result);
     				satoshiQuest.updateScoreboard(player);
				} catch(Exception e) {
					e.printStackTrace();
				}
			}//end buy
			}//end args.length == 2
			
		}//end is args[0] > 0
	} //end isStringInt

		if (args.length == 3) {
			if (args[1].equalsIgnoreCase("send")) {
				String sendWho = args[2];
	            			//if (!sendWho.equalsIgnoreCase(player.getDisplayName())) {
						if (Integer.valueOf(satoshiQuest.REDIS.get("LivesLeft" +player.getUniqueId().toString())) >= 2) {
							if (Integer.valueOf(args[0]) <= Integer.valueOf(satoshiQuest.REDIS.get("LivesLeft" +player.getUniqueId().toString()))) {
							int livesAmount = Integer.valueOf(args[0]);
							UUID uuid = UUID.fromString(SatoshiQuest.REDIS.get("uuid:" + args[2]));
							player.sendMessage(ChatColor.GREEN + "Sending " + args[0] + " lives to " + sendWho);
							String minusLives = Integer.toString((Integer.valueOf(satoshiQuest.REDIS.get("LivesLeft" +player.getUniqueId().toString())) - livesAmount));
							satoshiQuest.REDIS.set("LivesLeft" +player.getUniqueId().toString(), minusLives);
							String plusLives = Integer.toString((Integer.valueOf(satoshiQuest.REDIS.get("LivesLeft" +Bukkit.getServer().getPlayer(sendWho).getUniqueId().toString())) + livesAmount));
							satoshiQuest.REDIS.set("LivesLeft" + uuid.toString(),plusLives);
							}
						} else {
							player.sendMessage(ChatColor.RED + "You need 2 or more lives to be able to send to another player");
						}
					//}

			}//if send
		}//end (args.length == 3)
	}//end args length >0

    return true;
  }


}
