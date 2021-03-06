
package com.satoshiquest.satoshiquest.commands;

import com.satoshiquest.satoshiquest.SatoshiQuest;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.Bukkit;
import java.math.BigDecimal;

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
		balance = satoshiQuest.getBalance(player.getUniqueId().toString(),satoshiQuest.CONFS_TARGET);
		player.sendMessage(ChatColor.GREEN + "wallet balance: " + balance);
	} catch (Exception e) {
		e.printStackTrace();
		player.sendMessage(ChatColor.RED + "There was a problem reading your wallet.");
    	}
	if (satoshiQuest.didVote(player.getName()) == 0) {
	player.sendMessage(ChatColor.GREEN + "Lives are $" + satoshiQuest.BUYIN_AMOUNT + " USD for " + satoshiQuest.LIVES_PERBUYIN + " in "+SatoshiQuest.CRYPTO_TICKER+". Most goes into Loot wallet which everyone searches for the treasure, once found that player will recive funds to their player wallet if no external wallet set and the world will reset for a new hunt. A little bit is set aside for further developent and hosting.");
	player.sendMessage(ChatColor.AQUA + "You can also get 10% off lives here: " + satoshiQuest.VOTE_URL);
	} else if (satoshiQuest.didVote(player.getName()) == 1) {
player.sendMessage(ChatColor.AQUA + "THANK YOU FOR VOTING!");
player.sendMessage(ChatColor.GREEN + "Lives are $" + (satoshiQuest.BUYIN_AMOUNT*0.9) + " USD for " + satoshiQuest.LIVES_PERBUYIN + " in "+SatoshiQuest.CRYPTO_TICKER+". Most goes into Loot wallet which everyone searches for the treasure, once found that player will recive funds to their player wallet if no external wallet set and the world will reset for a new hunt. A little bit is set aside for further developent and hosting.");
	}
	if (balance == 0) {
		player.sendMessage(ChatColor.RED + "Looks like you dont have enough funds, try the (/wallet) command to check your balance and deposit address.");
	}
	player.sendMessage(ChatColor.YELLOW + "You can use the command (/Lives [Number]) like (/Lives 1) or (/Lives 2)");
	if (satoshiQuest.didVote(player.getName()) == 0) {
	player.sendMessage(ChatColor.YELLOW + "But to complete it you need to say buy to confirm so (/Lives 3 buy) will get you "+(satoshiQuest.LIVES_PERBUYIN * 3)+" lives for " + (3 * satoshiQuest.totalLifeRate));
	} else if (satoshiQuest.didVote(player.getName()) == 1) {
	player.sendMessage(ChatColor.AQUA + "THANK YOU FOR VOTING!");
	player.sendMessage(ChatColor.YELLOW + "But to complete it you need to say buy to confirm so (/Lives 3 buy) will get you "+(satoshiQuest.LIVES_PERBUYIN * 3)+" lives for " + (3 * (satoshiQuest.totalLifeRate*0.9)));
	}
	player.sendMessage(ChatColor.GREEN + "Lives are transferable between players with (/Lives [Number] send [playername])");
     } else if ((satoshiQuest.isStringInt(args[0])) && (args.length <= 3)) {  //end help
	if (Integer.parseInt(args[0]) > 0) {
		try {
     			balance = satoshiQuest.getBalance(player.getUniqueId().toString(),satoshiQuest.CONFS_TARGET);
		} catch (Exception e) {
			e.printStackTrace();
		}
		int livesAmount = Integer.valueOf(args[0]);
		Long sendLoot = satoshiQuest.livesRate * livesAmount;
		Long sendAdmin = satoshiQuest.adminRate * livesAmount;
		Long sendAdmin1 = sendAdmin / 2;
		Long sendAdmin2 = sendAdmin - sendAdmin1;
		Long totalBuyingBTC = satoshiQuest.totalLifeRate * livesAmount;
		if (satoshiQuest.didVote(player.getName()) == 1) {
		sendLoot = (long)(satoshiQuest.livesRate*0.9) * livesAmount;
		sendAdmin = (long)(satoshiQuest.adminRate*0.9) * livesAmount;
		sendAdmin1 = sendAdmin / 2;
		sendAdmin2 = sendAdmin - sendAdmin1;
		totalBuyingBTC = (long)(satoshiQuest.totalLifeRate*0.9) * livesAmount;
		}
		if (args.length == 1) {
		player.sendMessage(ChatColor.YELLOW + "Buy " + (satoshiQuest.LIVES_PERBUYIN * livesAmount) + " Lives for " + satoshiQuest.globalDecimalFormat.format(((Double)(BigDecimal.valueOf(totalBuyingBTC).doubleValue() * satoshiQuest.baseSat))) + " with " + satoshiQuest.globalDecimalFormat.format(((Double)(BigDecimal.valueOf(sendLoot).doubleValue() * satoshiQuest.baseSat))) + " going into the loot treasure and " + satoshiQuest.globalDecimalFormat.format(((Double)(BigDecimal.valueOf(sendAdmin).doubleValue() * satoshiQuest.baseSat))) + " going to the admin");
		}
		if (args.length == 2) {
			if (args[1].equalsIgnoreCase("buy")) {
				String result = "failed";
				try {
					if (satoshiQuest.ADMIN2_ADDRESS != "noSet") {
						result = satoshiQuest.sendMany2(player.getUniqueId().toString(), 							satoshiQuest.REDIS.get("nodeAddress"+satoshiQuest.SERVERDISPLAY_NAME), satoshiQuest.ADMIN_ADDRESS, satoshiQuest.ADMIN2_ADDRESS, sendLoot, sendAdmin1, sendAdmin2);				
					} else {
						result = satoshiQuest.sendMany(player.getUniqueId().toString(), 							satoshiQuest.REDIS.get("nodeAddress"+satoshiQuest.SERVERDISPLAY_NAME), satoshiQuest.ADMIN_ADDRESS, sendLoot, sendAdmin);
					}

					//end if multidev
		      			Long newBalance = satoshiQuest.getBalance(player.getUniqueId().toString(),satoshiQuest.CONFS_TARGET);
			    		boolean setFee = satoshiQuest.setSatByte(player.getUniqueId().toString(), Double.parseDouble(SatoshiQuest.REDIS.get("txFee" + player.getUniqueId().toString())));
					if ((result != "failed") && (balance > newBalance)) {
						String setLives = Integer.toString(((Integer.valueOf(satoshiQuest.REDIS.get("LivesLeft" +player.getUniqueId().toString()))) + (satoshiQuest.LIVES_PERBUYIN * livesAmount)));
						satoshiQuest.REDIS.set("LivesLeft" +player.getUniqueId().toString(), setLives);
					player.sendMessage(ChatColor.GREEN + "You just got " + (satoshiQuest.LIVES_PERBUYIN * livesAmount) + " lives! "+ ChatColor.BLUE + satoshiQuest.TX_URL + result);
					} else if (result == "failed") {
						player.sendMessage(ChatColor.RED + "Buy lives failed, may be due to not enough confirmed balance. try /wallet to check.");
					}
					System.out.println("[LivesBuy] " + result);
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
							String minusLives = Integer.toString((Integer.valueOf(satoshiQuest.REDIS.get("LivesLeft" +player.getUniqueId().toString())) - Integer.valueOf(args[0])));
							String plusLives = Integer.toString((Integer.valueOf(satoshiQuest.REDIS.get("LivesLeft" +Bukkit.getServer().getPlayer(sendWho).getUniqueId())) + Integer.valueOf(args[0])));
							satoshiQuest.REDIS.set("LivesLeft" +player.getUniqueId().toString(), minusLives);
							satoshiQuest.REDIS.set("LivesLeft" + Bukkit.getServer().getPlayer(sendWho).getUniqueId(),plusLives);
							onlinePlayer.sendMessage(ChatColor.GREEN +""+player.getDisplayName()+ " Sent " + args[0] + " lives to you!");
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
	try {
	satoshiQuest.updateScoreboard(player);
	} catch(Exception e) {
		e.printStackTrace();
	}
    return true;
  }


}
