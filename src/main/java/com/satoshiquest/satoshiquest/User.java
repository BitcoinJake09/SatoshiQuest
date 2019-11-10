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
    this.wallet = new NodeWallet(this.player.getUniqueId().toString());
  }

}
