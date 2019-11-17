package com.satoshiquest.satoshiquest;

import java.io.IOException;
import java.text.ParseException;
import java.util.UUID;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.*;

public class User {
  private SatoshiQuest satoshiQuest;
  public Player player;
  public NodeWallet wallet;
  public UUID uuid;

  public User(Player player)
      throws ParseException, org.json.simple.parser.ParseException, IOException {
	this.player = player;
	this.wallet = null;
	satoshiQuest.getWalletInfo(player.getUniqueId().toString());
	if (!satoshiQuest.REDIS.exists("nodeWallet"+player.getUniqueId().toString())) {
		if (satoshiQuest.loadWallet(player.getUniqueId().toString())!=null) {
			try {
				wallet = satoshiQuest.loadWallet(satoshiQuest.REDIS.get("nodeWallet"+player.getUniqueId().toString()));
			        System.out.println("[world wallet] trying to load node wallet");
			} catch (NullPointerException npe) {
				npe.printStackTrace();
				System.out.println("[world wallet] wallet not found, attempting to create.");
			}
		} else {
	        	wallet = satoshiQuest.generateNewWallet(player.getUniqueId().toString());
        		System.out.println("[world wallet] generated new wallet");
			satoshiQuest.REDIS.set("nodeWallet"+player.getUniqueId().toString(),player.getUniqueId().toString());
		} 
	} else { 
		wallet = satoshiQuest.loadWallet(satoshiQuest.REDIS.get("nodeWallet"+player.getUniqueId().toString()));
	}//nodewallet
	if (!satoshiQuest.REDIS.exists("nodeAddress"+player.getUniqueId().toString())) {
	try {
		if (wallet.getAccountAddress()!=null) {
			satoshiQuest.REDIS.set("nodeAddress"+player.getUniqueId().toString(),wallet.getAccountAddress());
		} else {
			satoshiQuest.REDIS.set("nodeAddress"+player.getUniqueId().toString(),wallet.getNewAccountAddress());
		}
	} catch (NullPointerException npe2) {
			npe2.printStackTrace();
			System.out.println("[world address] address not found, attempting to create.");
		}
	}//endAddress

  }
}
