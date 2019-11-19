package com.satoshiquest.satoshiquest.events;

import com.satoshiquest.satoshiquest.SatoshiQuest;
import com.satoshiquest.satoshiquest.NodeWallet;
import com.satoshiquest.satoshiquest.User;
import java.io.IOException;
import java.sql.SQLException;
import java.text.ParseException;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.apache.commons.lang.WordUtils;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.enchantment.EnchantItemEvent;
import org.bukkit.event.entity.*;
import org.bukkit.event.entity.CreatureSpawnEvent.SpawnReason;
import org.bukkit.event.player.*;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.potion.PotionData;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.potion.PotionType;
import org.bukkit.util.Vector;

public class EntityEvents implements Listener {
  SatoshiQuest satoshiQuest;
  StringBuilder rawwelcome = new StringBuilder();
  String PROBLEM_MESSAGE = "Can't join right now. Come back later";

  public EntityEvents(SatoshiQuest plugin) {
    satoshiQuest = plugin;

    for (String line : satoshiQuest.getConfig().getStringList("welcomeMessage")) {
      for (ChatColor color : ChatColor.values()) {
        line = line.replaceAll("<" + color.name() + ">", color.toString());
      }
      // add links
      final Pattern pattern = Pattern.compile("<link>(.+?)</link>");
      final Matcher matcher = pattern.matcher(line);
      matcher.find();
      String link = matcher.group(1);
      // Right here we need to replace the link variable with a minecraft-compatible link
      line = line.replaceAll("<link>" + link + "<link>", link);

      rawwelcome.append(line);
    }
  }

  @EventHandler
  public void onPlayerLogin(PlayerLoginEvent event) {
    try {
      Player player = event.getPlayer();
      final User user = new User(player);
      SatoshiQuest.REDIS.set("name:" + player.getUniqueId().toString(), player.getName());
      SatoshiQuest.REDIS.set("uuid:" + player.getName().toString(), player.getUniqueId().toString());
      if (SatoshiQuest.REDIS.sismember("banlist", event.getPlayer().getUniqueId().toString())) {
        System.out.println("kicking banned player " + event.getPlayer().getDisplayName());
        event.disallow(
            PlayerLoginEvent.Result.KICK_OTHER,
            "You are temporarily banned. Please contact satoshiquest@satoshiquest.co");
      }

    } catch (Exception e) {
      e.printStackTrace();
      event.disallow(
          PlayerLoginEvent.Result.KICK_OTHER,
          "The server is in limited capacity at this moment. Please try again later.");
    }
  }

  @EventHandler
  public void onPlayerJoin(PlayerJoinEvent event) throws ParseException{

    final Player player = event.getPlayer();
    // On dev environment, admin gets op. In production, nobody gets op.

    player.setGameMode(GameMode.SURVIVAL);
    final String ip = player.getAddress().toString().split("/")[1].split(":")[0];
    System.out.println("User " + player.getName() + "logged in with IP " + ip);
    SatoshiQuest.REDIS.set("ip" + player.getUniqueId().toString(), ip);
    SatoshiQuest.REDIS.set("displayname:" + player.getUniqueId().toString(), player.getDisplayName());
    SatoshiQuest.REDIS.set("uuid:" + player.getName().toString(), player.getUniqueId().toString());
    if (satoshiQuest.SATOSHIQUEST_ENV.equals("development") == true && satoshiQuest.ADMIN_UUID == null) {
      player.setOp(true);
    }
    if (satoshiQuest.isModerator(player)) {
      player.sendMessage(ChatColor.GREEN + "You are a moderator on this server.");
	try {
      String url = satoshiQuest.ADDRESS_URL + satoshiQuest.REDIS.get("nodeAddress"+satoshiQuest.SERVERDISPLAY_NAME);
      player.sendMessage(ChatColor.WHITE + "" + ChatColor.UNDERLINE + url);
	} catch(Exception E) {
		    System.out.println(E);
	}
    }
        
    if (!SatoshiQuest.REDIS.exists("LivesLeft" + player.getUniqueId().toString())) {
		SatoshiQuest.REDIS.set("LivesLeft" +player.getUniqueId().toString(),"0");
	}

    if (satoshiQuest.REDIS.exists("nodeAddress"+ player.getUniqueId().toString())) {
	player.sendMessage(ChatColor.GREEN + "Your Deposit address on this server: " + satoshiQuest.REDIS.exists("nodeAddress"+ player.getUniqueId().toString()));
	try {
      String url = satoshiQuest.ADDRESS_URL + satoshiQuest.REDIS.get("nodeAddress"+ player.getUniqueId().toString());
      player.sendMessage(ChatColor.WHITE + "" + ChatColor.UNDERLINE + url);
	} catch(Exception E) {
		    System.out.println(E);
	}
    }



    player.sendMessage("you have " + SatoshiQuest.REDIS.get("LivesLeft" + player.getUniqueId().toString()) + " lives!");




    String welcome = rawwelcome.toString();
    welcome = welcome.replace("<name>", player.getName());
    player.sendMessage(welcome);
     if (satoshiQuest.isModerator(player)) {
        player.setPlayerListName(
            ChatColor.RED
                + "[MOD]"
                + ChatColor.WHITE
                + player.getName());
      
    } 

    // Prints the user balance

    player.sendMessage(ChatColor.YELLOW + "     Welcome to " + satoshiQuest.SERVER_NAME + "! ");
    if (SatoshiQuest.REDIS.exists("satoshiquest:motd") == true) player.sendMessage(SatoshiQuest.REDIS.get("satoshiquest:motd"));
    try {
      player.sendMessage(
              "The loot pool is: "
                      + (int)
                      (satoshiQuest.wallet.getBalance(1)/satoshiQuest.DENOMINATION_FACTOR)
                      + " "
                      + satoshiQuest.DENOMINATION_NAME);
    } catch(Exception e) {
      e.printStackTrace();
    }


    SatoshiQuest.REDIS.zincrby("player:login", 1, player.getUniqueId().toString());
	try {
	satoshiQuest.updateScoreboard(player);
	} catch(Exception e) {
      e.printStackTrace();
    }
  }

    public boolean isNotAtSpawn(Location location)
	{
		Location spawn = Bukkit.getServer().getWorlds().get(0).getSpawnLocation();
		double spawnx = spawn.getX();
		double spawnz = spawn.getZ();
		double playerx=(double)location.getX();
                double playerz=(double)location.getZ();
	        //System.out.println("x:"+playerx+" z:"+playerz);  //for testing lol
	if (!((playerx<spawnx+SatoshiQuest.SPAWN_PROTECT_RADIUS)&&(playerx>spawnx-SatoshiQuest.SPAWN_PROTECT_RADIUS)))return true;
	else if(!((playerz<spawnz+SatoshiQuest.SPAWN_PROTECT_RADIUS)&&(playerz>spawnz-SatoshiQuest.SPAWN_PROTECT_RADIUS)))return true;

               return false;//not
	}

  @EventHandler
  public void onPlayerMove(PlayerMoveEvent event)
      throws ParseException, org.json.simple.parser.ParseException, IOException {
  if (SatoshiQuest.REDIS.get("LivesLeft" + event.getPlayer().getUniqueId().toString()) == "0") {
		if (isNotAtSpawn(event.getPlayer().getLocation())) {
			event.getPlayer().sendMessage(ChatColor.RED + "you cant leave spawn.");
			satoshiQuest.teleportToSpawn(event.getPlayer());
		}
	}
  }

  @EventHandler
  public void onPlayerInteract(PlayerInteractEvent event)
      throws ParseException, org.json.simple.parser.ParseException, IOException {

      // If player doesn't have permission, disallow the player to interact with it.
      if (!satoshiQuest.canBuild(event.getPlayer())) {
        event.setCancelled(true);
        event.getPlayer().sendMessage(ChatColor.DARK_RED + "You don't have permission to do that!");
      }
      if (SatoshiQuest.REDIS.get("LivesLeft" + event.getPlayer().getUniqueId().toString()) == "0") {
		if (isNotAtSpawn(event.getPlayer().getLocation())) {
			event.getPlayer().sendMessage(ChatColor.RED + "you cant leave spawn.");
			satoshiQuest.teleportToSpawn(event.getPlayer());
		}
	}
    
  }


  @EventHandler
  public void onPlayerDeath(PlayerDeathEvent event) {
	if(event instanceof Player ){
		Player p = event.getEntity();
		Player player = (Player) p;
if (!isNotAtSpawn(player.getLocation())) {
	if (Integer.parseInt(SatoshiQuest.REDIS.get("LivesLeft" + player.getUniqueId().toString())) >= 1)	{
		int livesLeft = Integer.parseInt(SatoshiQuest.REDIS.get("LivesLeft" + player.getUniqueId().toString())) - 1;
		SatoshiQuest.REDIS.set("LivesLeft" +player.getUniqueId().toString(),String.valueOf(livesLeft));

            player.sendMessage(ChatColor.RED + "You Lost a life. You have " + SatoshiQuest.REDIS.get("LivesLeft" + player.getUniqueId().toString())  + " lives left.");
	}
	if (Integer.parseInt(SatoshiQuest.REDIS.get("LivesLeft" + player.getUniqueId().toString())) == 0)	{
		//tp player to spawn
		   satoshiQuest.teleportToSpawn(player);
	}
	}//end spawncheck
	}
  }

    @EventHandler
    public void onPlayerGameModeChange(PlayerGameModeChangeEvent event)
            throws ParseException, org.json.simple.parser.ParseException, IOException {
        if (satoshiQuest.SATOSHIQUEST_ENV.equals("production")) {
            event.getPlayer().sendMessage(ChatColor.RED + "Sorry changing gamemode are not allowed.");
            event.setCancelled(true);
        } else if (satoshiQuest.SATOSHIQUEST_ENV.equals("development") == true && satoshiQuest.ADMIN_UUID == event.getPlayer().getUniqueId()) {
    		event.getPlayer().setOp(true);
                event.setCancelled(false);
        } else
                event.setCancelled(false);
    } 

  String spawnKey(Location location) {
    return location.getWorld().getName()
        + location.getChunk().getX()
        + ","
        + location.getChunk().getZ()
        + "spawn";
  }

}
