package com.satoshiquest.satoshiquest.commands;

import com.satoshiquest.satoshiquest.SatoshiQuest;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;


public class SpectateCommand extends CommandAction {
    private SatoshiQuest satoshiQuest;

    public SpectateCommand(SatoshiQuest plugin) {
        this.satoshiQuest = plugin;
    }

    public boolean run(CommandSender sender, Command cmd, String label, String[] args, Player player) {
        if (args.length == 1) {

            if(Bukkit.getPlayer(args[0]) != null) {
                ((Player) sender).setGameMode(GameMode.SPECTATOR);
                ((Player) sender).setSpectatorTarget(Bukkit.getPlayer(args[0]));
		sender.sendMessage(((Player) sender) + "You're now spectating " + args[0] + ".");
            } else {
		sender.sendMessage(((Player) sender) + "Player " + args[0] + " isn't online.");
            }
            return true;
        } else {
		((Player) sender).setGameMode(GameMode.SURVIVAL);
		sender.sendMessage("set gamemode to survival");
	return true;
	}
        //return false;
    }
}
