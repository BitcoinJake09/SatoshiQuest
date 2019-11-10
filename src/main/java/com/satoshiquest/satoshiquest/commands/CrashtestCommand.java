package com.satoshiquest.satoshiquest.commands;

import com.satoshiquest.satoshiquest.SatoshiQuest;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class CrashtestCommand extends CommandAction {
  private SatoshiQuest satoshiQuest;

  public CrashtestCommand(SatoshiQuest plugin) {
    this.satoshiQuest = plugin;
  }

  public boolean run(
      CommandSender sender, Command cmd, String label, String[] args, Player player) {
    satoshiQuest.crashtest();
    return true;
  }
}
