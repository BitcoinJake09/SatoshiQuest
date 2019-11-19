package com.satoshiquest.satoshiquest.events;

import com.satoshiquest.satoshiquest.SatoshiQuest;
import java.util.List;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.*;
import org.bukkit.event.block.BlockIgniteEvent.IgniteCause;
import org.bukkit.event.player.PlayerInteractEvent;

public class BlockEvents implements Listener {

	SatoshiQuest satoshiQuest;

	public BlockEvents(SatoshiQuest plugin) {

		satoshiQuest = plugin;
	}

	@EventHandler
	void onBlockBreak(BlockBreakEvent event) {
		// If block is bedrock, cancel the event
		Block b = event.getBlock();
		Material m = b.getType();
		if (m.equals(Material.BEDROCK)) {
			event.setCancelled(true);
			// If player is in a no-build zone, cancel the event
		} else if (!satoshiQuest.canBuild(event.getPlayer())) {
			event.setCancelled(true);
		} else {
			event.setCancelled(false);
		}
	}

	@EventHandler
	void onBlockPlace(BlockPlaceEvent event) {
		// set clan
		// first, we check if the player has permission to build
		Block b = event.getBlock();
		Material m = b.getType();
		if (m.equals(Material.BEDROCK)) {
			event.setCancelled(true);
			// If player is in a no-build zone, cancel the event
		} else if (!satoshiQuest.canBuild(event.getPlayer())) {
			event.setCancelled(true);
		} else {
			event.setCancelled(false);
		}
	}

}
