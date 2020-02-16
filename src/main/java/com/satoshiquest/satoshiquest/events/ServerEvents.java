package com.satoshiquest.satoshiquest.events;

import com.satoshiquest.satoshiquest.SatoshiQuest;
import org.bukkit.ChatColor;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.server.ServerListPingEvent;

public class ServerEvents implements Listener {
  SatoshiQuest satoshiQuest;

  public ServerEvents(SatoshiQuest plugin) {
    satoshiQuest = plugin;
  }

  @EventHandler
  public void onServerListPing(ServerListPingEvent event) {

    event.setMotd(
        ChatColor.GOLD
            + ChatColor.BOLD.toString()
            + SatoshiQuest.SERVERDISPLAY_NAME
            + ChatColor.RESET
            + " - The server that runs on "
            + SatoshiQuest.DENOMINATION_NAME);
  }
}
