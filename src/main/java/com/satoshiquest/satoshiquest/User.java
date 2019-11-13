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
	if (satoshiQuest.REDIS.exists("nodeWallet"+this.player.getUniqueId().toString())) {
		try {
			this.wallet = satoshiQuest.loadWallet(satoshiQuest.REDIS.get("nodeWallet"+this.player.getUniqueId().toString()));
		        System.out.println("[user wallet] trying to load node wallet");
		} catch (NullPointerException npe) {
			npe.printStackTrace();
			System.out.println("[user wallet] wallet not found, attempting to create.");
		}
	} else if(!satoshiQuest.REDIS.exists("nodeWallet"+this.player.getUniqueId().toString()))
	{
	        this.wallet = satoshiQuest.generateNewWallet(this.player.getUniqueId().toString());
        	System.out.println("[user wallet] generated new wallet");
		satoshiQuest.REDIS.set("nodeWallet"+this.player.getUniqueId().toString(),this.player.getUniqueId().toString());
	}
  }

}
