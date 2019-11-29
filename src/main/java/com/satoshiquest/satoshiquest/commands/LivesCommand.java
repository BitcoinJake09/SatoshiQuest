package com.satoshiquest.satoshiquest.commands;

import com.satoshiquest.satoshiquest.SatoshiQuest;
import com.satoshiquest.satoshiquest.User;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

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
      User user = new User(player);
      satoshiQuest.getWalletInfo(player.getUniqueId().toString());
      balance = satoshiQuest.getBalance(player.getUniqueId().toString(),1);
      player.sendMessage(ChatColor.RED + "wallet balance: " + balance);
    } catch (Exception e) {
      e.printStackTrace();
      player.sendMessage(ChatColor.RED + "There was a problem reading your wallet.");
    }
	player.sendMessage(ChatColor.GREEN + "Lives are " + satoshiQuest.BUYIN_AMOUNT + " each. Most goes into Loot wallet which everyone searches for the treasure, once found that player will recive funds to their player wallet and the world will reset for a new hunt. A little bit is set aside for further developent and hosting.");
	if (balance == 0) {
	player.sendMessage(ChatColor.RED + "Looks like you dont have enough funds, try the /wallet command to check your balance and deposit address.");
	} else if (balance > 0) {
	player.sendMessage(ChatColor.YELLOW + "You can use the command /Lives [Number] like /Lives 1 or /Lives 2");
	player.sendMessage(ChatColor.YELLOW + "But to complete it you need to say true to confirm so /Lives 3 true will get you 3 lives for " + (3 * satoshiQuest.BUYIN_AMOUNT));
	player.sendMessage(ChatColor.GREEN + "Lives are transferable between players with /Lives [Number] give [playername]");
	}

	} else if (isStringInt(args[0])) {  //end help
		if (Integer.parseInt(args[0]) > 0) {
			try {
     			balance = satoshiQuest.getBalance(player.getUniqueId().toString(),1);
			} catch (Exception e) {
				e.printStackTrace();
			}
			int livesAmount = Integer.valueOf(args[0]);
			Long totalBuyingBTC = (satoshiQuest.BUYIN_AMOUNT * livesAmount);
			Long sendLoot = (totalBuyingBTC-(totalBuyingBTC/10));
			Long sendAdmin = (totalBuyingBTC/10);
			player.sendMessage(ChatColor.YELLOW + "Buy " + livesAmount + " package of " + (satoshiQuest.LIVES_PERBUYIN * livesAmount) + " Lives for " + totalBuyingBTC + " with " + sendLoot + " going into the loot treasure and " + sendAdmin + " going to the admin");
			if (args.length == 2) {
			if (args[1].equals("buy")) {
			try {
			String result = satoshiQuest.sendMany(player.getUniqueId().toString(), satoshiQuest.REDIS.get("nodeAddress"+satoshiQuest.SERVERDISPLAY_NAME), satoshiQuest.ADMIN_ADDRESS, sendLoot, sendAdmin, 6);
      			Long newBalance = satoshiQuest.getBalance(player.getUniqueId().toString(),0);
			if ((result != "failed") && (balance > newBalance)) {
				String setLives = Integer.toString(((Integer.valueOf(satoshiQuest.REDIS.get("LivesLeft" +player.getUniqueId().toString()))) + (satoshiQuest.LIVES_PERBUYIN * livesAmount)));
				satoshiQuest.REDIS.set("LivesLeft" +player.getUniqueId().toString(), setLives);
			}
			System.out.println("[LivesBuy] " + result);
     			satoshiQuest.updateScoreboard(player);
			} catch(Exception e) {
				e.printStackTrace();
			}
			}//end true
			}//end args.length
		}//end is args[0] > 0
	} //end isStringInt
	}//end args length >0

    return true;
  }
	public boolean isStringInt(String s)
{
    try
    {
        Integer.parseInt(s);
        return true;
    } catch (NumberFormatException ex)
    {
        return false;
    }
}

}
