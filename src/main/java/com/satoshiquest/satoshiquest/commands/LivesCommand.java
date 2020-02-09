
package com.satoshiquest.satoshiquest.commands;

import com.satoshiquest.satoshiquest.SatoshiQuest;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.Bukkit;

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
      if ((args[0].equalsIgnoreCase("help"))||(args.length == 0)) {
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
	}
	player.sendMessage(ChatColor.YELLOW + "You can use the command (/Lives [Number]) like (/Lives 1) or (/Lives 2)");
	player.sendMessage(ChatColor.YELLOW + "But to complete it you need to say buy to confirm so (/Lives 3 buy) will get you 3 lives for " + (3 * satoshiQuest.totalLifeRate));
	player.sendMessage(ChatColor.GREEN + "Lives are transferable between players with (/Lives [Number] send [playername])");
     } else if ((satoshiQuest.isStringInt(args[0])) && (args.length <= 3)) {  //end help
	if (Integer.parseInt(args[0]) > 0) {
		try {
     			balance = satoshiQuest.getBalance(player.getUniqueId().toString(),6);
		} catch (Exception e) {
			e.printStackTrace();
		}
		int livesAmount = Integer.valueOf(args[0]);
		Long sendLoot = satoshiQuest.livesRate * livesAmount;
		Long sendAdmin = satoshiQuest.adminRate * livesAmount;
		Long totalBuyingBTC = satoshiQuest.totalLifeRate * livesAmount;
		if (args.length == 1) {
		player.sendMessage(ChatColor.YELLOW + "Buy " + livesAmount + " Lives for " + totalBuyingBTC + " with " + sendLoot + " going into the loot treasure and " + sendAdmin + " going to the admin");
		}
		if (args.length == 2) {
			if (args[1].equalsIgnoreCase("buy")) {
				try {
					String result = satoshiQuest.sendMany(player.getUniqueId().toString(), 						satoshiQuest.REDIS.get("nodeAddress"+satoshiQuest.SERVERDISPLAY_NAME), satoshiQuest.ADMIN_ADDRESS, sendLoot, sendAdmin);
		      			Long newBalance = satoshiQuest.getBalance(player.getUniqueId().toString(),6);
					if ((result != "failed") && (balance > newBalance)) {
						String setLives = Integer.toString(((Integer.valueOf(satoshiQuest.REDIS.get("LivesLeft" +player.getUniqueId().toString()))) + (satoshiQuest.LIVES_PERBUYIN * livesAmount)));
						satoshiQuest.REDIS.set("LivesLeft" +player.getUniqueId().toString(), setLives);
					player.sendMessage(ChatColor.GREEN + "You just got " + (satoshiQuest.LIVES_PERBUYIN * livesAmount) + " lives!");
					} else if (result == "failed") {
						player.sendMessage(ChatColor.RED + "Buy lives failed, may be due to not enough confirmed balance. try /wallet to check.");
					}
					System.out.println("[LivesBuy] " + result);
     					satoshiQuest.updateScoreboard(player);
				} catch(Exception e) {
					e.printStackTrace();
				}
			}//end true
		}//end args.length == 2
	
			if (args.length == 3) {

			if (args[1].equalsIgnoreCase("send")) {

				String sendWho = args[2];
				for (final Player onlinePlayer : Bukkit.getOnlinePlayers()) {
          			if (onlinePlayer.getName().equalsIgnoreCase(sendWho)) {

	            			if (!sendWho.equalsIgnoreCase(player.getDisplayName())) {
						if (Integer.valueOf(satoshiQuest.REDIS.get("LivesLeft" +player.getUniqueId().toString())) >= 2) {

							if (Integer.valueOf(args[0]) <= Integer.valueOf(satoshiQuest.REDIS.get("LivesLeft" +player.getUniqueId().toString()))) {
						       
														player.sendMessage(ChatColor.GREEN + "Sending " + args[0] + " lives to " + sendWho);
							String minusLives = Integer.toString((Integer.valueOf(satoshiQuest.REDIS.get("LivesLeft" +player.getUniqueId().toString())) - livesAmount));
							String plusLives = Integer.toString((Integer.valueOf(satoshiQuest.REDIS.get("LivesLeft" +Bukkit.getServer().getPlayer(sendWho).getUniqueId())) + livesAmount));
							satoshiQuest.REDIS.set("LivesLeft" +player.getUniqueId().toString(), minusLives);
							satoshiQuest.REDIS.set("LivesLeft" + Bukkit.getServer().getPlayer(sendWho).getUniqueId(),plusLives);
							}
						} else {
							player.sendMessage(ChatColor.RED + "You need 2 or more lives to be able to send to another player");
						}
					}
				}

				}//end for
			}//if send
		}//end (args.length == 3)
		}//end is args[0] > 0
	 } //end isStringInt
	}//end args length >0

    return true;
  }


}
