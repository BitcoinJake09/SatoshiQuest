package com.satoshiquest.satoshiquest.events;

import com.satoshiquest.satoshiquest.SatoshiQuest;
import com.satoshiquest.satoshiquest.NodeWallet;
//import com.satoshiquest.satoshiquest.User;
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
import org.bukkit.block.BlockFace;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.enchantment.EnchantItemEvent;
import org.bukkit.event.entity.*;
import org.bukkit.event.entity.CreatureSpawnEvent.SpawnReason;
import org.bukkit.event.player.*;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.attribute.Attribute;
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
import org.bukkit.advancement.*;
import java.util.*;

public class EntityEvents implements Listener {
  SatoshiQuest satoshiQuest;
  StringBuilder rawwelcome = new StringBuilder();
  String PROBLEM_MESSAGE = "Can't join right now. Come back later";
  boolean isNewPlayer = false;

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
	NodeWallet tempWallet=null;
	if (!satoshiQuest.REDIS.exists("nodeAddress"+player.getUniqueId().toString())) {
	if (satoshiQuest.getWalletInfo(player.getUniqueId().toString())!=false) {
		try {
			tempWallet = satoshiQuest.loadWallet(player.getUniqueId().toString());
		        System.out.println("[player wallet] trying to load node wallet");
		} catch (NullPointerException npe) {
			npe.printStackTrace();
			System.out.println("[player wallet] wallet not found, attempting to create.");
		}
	} else
	{
	        tempWallet = satoshiQuest.generateNewWallet(player.getUniqueId().toString());
        	System.out.println("[player wallet] generated new wallet");
		//REDIS.set("nodeWallet"+SERVERDISPLAY_NAME,SERVERDISPLAY_NAME);
	} 
	} else { 
	tempWallet = satoshiQuest.loadWallet(player.getUniqueId().toString());
	}//nodewallet
//final User user = new User(player);
	
      SatoshiQuest.REDIS.set("name:" + player.getUniqueId().toString(), player.getName());
      SatoshiQuest.REDIS.set("uuid:" + player.getName().toString(), player.getUniqueId().toString());
      if (SatoshiQuest.REDIS.sismember("banlist", event.getPlayer().getUniqueId().toString())) {
        System.out.println("kicking banned player " + event.getPlayer().getDisplayName());
        event.disallow(
            PlayerLoginEvent.Result.KICK_OTHER,
            "You are temporarily banned. Please contact satoshiquest@satoshiquest.co");
      }
      if (SatoshiQuest.REDIS.exists("winner")) {
        System.out.println("kicking player " + event.getPlayer().getDisplayName() + " while world loads");
        event.disallow(
            PlayerLoginEvent.Result.KICK_OTHER,
            "World is resetting for new game, please try again in a moment.");
      }
	if (!SatoshiQuest.REDIS.exists("winner")) {
	if (player.getWorld() == Bukkit.getServer().getWorld("world")) {
	    Location location = Bukkit
                                .getServer()
                                .getWorld(SatoshiQuest.SERVERDISPLAY_NAME).getSpawnLocation();
                        player.teleport(location);
			//player.sendMessage(ChatColor.WHITE + "Welcome to " + SatoshiQuest.SERVERDISPLAY_NAME);
	}
	}
	if (SatoshiQuest.REDIS.exists("toldSpawn" + event.getPlayer().getUniqueId().toString())) {
	SatoshiQuest.REDIS.del("toldSpawn" + event.getPlayer().getUniqueId().toString());
	}
	if (SatoshiQuest.REDIS.exists("toldSpawn2" + event.getPlayer().getUniqueId().toString())) {
	SatoshiQuest.REDIS.del("toldSpawn2" + event.getPlayer().getUniqueId().toString());
	}
	if (SatoshiQuest.REDIS.exists("toldNether" + event.getPlayer().getUniqueId().toString())) {
	SatoshiQuest.REDIS.del("toldNether" + event.getPlayer().getUniqueId().toString());
	}
	if (SatoshiQuest.REDIS.exists("toldNether2" + event.getPlayer().getUniqueId().toString())) {
	SatoshiQuest.REDIS.del("toldNether2" + event.getPlayer().getUniqueId().toString());
	}
	if (SatoshiQuest.REDIS.exists("toldNether3" + event.getPlayer().getUniqueId().toString())) {
	SatoshiQuest.REDIS.del("toldNether3" + event.getPlayer().getUniqueId().toString());
	}
	if (!SatoshiQuest.REDIS.exists("txFee" + event.getPlayer().getUniqueId().toString())){
		boolean setFee = satoshiQuest.setSatByte(event.getPlayer().getUniqueId().toString(), 1.2);
		//System.out.println("no fee set, set to 1.2sats/byte. "+setFee);
		SatoshiQuest.REDIS.set("txFee" + event.getPlayer().getUniqueId().toString(),"1.2");
	} else 	if (SatoshiQuest.REDIS.exists("txFee" + event.getPlayer().getUniqueId().toString())) {
		boolean setFee = satoshiQuest.setSatByte(event.getPlayer().getUniqueId().toString(), Double.parseDouble(SatoshiQuest.REDIS.get("txFee" + event.getPlayer().getUniqueId().toString())));
		//System.out.println("player fee is set to " + SatoshiQuest.REDIS.get("txFee" + event.getPlayer().getUniqueId().toString()) + "sats/byte.");
	}

    } catch (Exception e) {
      e.printStackTrace();
      event.disallow(
          PlayerLoginEvent.Result.KICK_OTHER,
          "The server is in limited capacity at this moment. Please try again later.");
    }
  }
  @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        // Called when a player leaves a server
        Player player = event.getPlayer();
        String quitMessage = "left the game";
	if(System.getenv("DISCORD_HOOK_URL")!=null) {
			satoshiQuest.sendDiscordMessage("Player " + player.getName() + " " + quitMessage +" with " + SatoshiQuest.REDIS.get("LivesLeft" + player.getUniqueId().toString()) + " lives");
	}
    }

  @EventHandler
  public void onPlayerJoin(PlayerJoinEvent event) throws ParseException{

    final Player player = event.getPlayer();
    // On dev environment, admin gets op. In production, nobody gets op.

    player.setGameMode(GameMode.SURVIVAL);
    //player.setGameMode(GameMode.CREATIVE); // test for admin
	if (!SatoshiQuest.REDIS.exists("winner")) {
	if (player.getWorld() == Bukkit.getServer().getWorld("world")) {
	    Location location = Bukkit
                                .getServer()
                                .getWorld(SatoshiQuest.SERVERDISPLAY_NAME).getSpawnLocation();
                        player.teleport(location);
			//player.sendMessage(ChatColor.WHITE + "Welcome to " + SatoshiQuest.SERVERDISPLAY_NAME);
	}
	}

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
    if (satoshiQuest.REDIS.exists("LootAnnounced" +player.getUniqueId().toString())) {
		satoshiQuest.REDIS.del("LootAnnounced" +player.getUniqueId().toString());
		}   
    if (!SatoshiQuest.REDIS.exists("LivesLeft" + player.getUniqueId().toString())) {
		isNewPlayer = true;
		SatoshiQuest.REDIS.set("LivesLeft" +player.getUniqueId().toString(),"0");
		if (SatoshiQuest.REDIS.exists("BetaTest")){
		SatoshiQuest.REDIS.set("LivesLeft" +player.getUniqueId().toString(),"1");
		player.sendMessage(ChatColor.WHITE + "You get 1 free life during beta test.");
		}
	}
    if (satoshiQuest.REDIS.exists("ClearInv" +player.getUniqueId().toString())) {
		PlayerInventory pli2 = player.getInventory();
		pli2.clear(); //delete player world datas
		pli2.setArmorContents(new ItemStack[4]);
		Inventory pe2 = player.getEnderChest();
		pe2.clear();
		player.setLevel(0);
		player.setExp(0);
		player.setExhaustion(0);
		player.setFoodLevel(20);
player.setHealth(player.getAttribute(Attribute.GENERIC_MAX_HEALTH).getValue());

		Iterator<Advancement> it = Bukkit.getServer().advancementIterator();
		// gets all 'registered' advancements on the server.
		while (it.hasNext()) {
		// loops through these.
		Advancement a = it.next();
	        AdvancementProgress progress = player.getAdvancementProgress(a);
		if (progress.isDone() == true) {
		         for(String c: a.getCriteria()) {
	 player.getAdvancementProgress(a).revokeCriteria(c);
			}
		}

            }

		satoshiQuest.REDIS.del("ClearInv" +player.getUniqueId().toString());
	}
	try {
    if (satoshiQuest.REDIS.exists("nodeAddress"+ player.getUniqueId().toString())) {
	player.sendMessage(ChatColor.GREEN + "Your Deposit address on this server: " + satoshiQuest.REDIS.get("nodeAddress"+ player.getUniqueId().toString()));

      String url = satoshiQuest.ADDRESS_URL + satoshiQuest.REDIS.get("nodeAddress"+ player.getUniqueId().toString());
      player.sendMessage(ChatColor.WHITE + "" + ChatColor.UNDERLINE + url);

    } else {
	satoshiQuest.REDIS.set("nodeAddress"+ player.getUniqueId().toString(),satoshiQuest.getAccountAddress(player.getUniqueId().toString()));
	player.sendMessage(ChatColor.GREEN + "Your Deposit address on this server: " + satoshiQuest.REDIS.get("nodeAddress"+ player.getUniqueId().toString()));

      String url2 = satoshiQuest.ADDRESS_URL + satoshiQuest.REDIS.get("nodeAddress"+ player.getUniqueId().toString());
      player.sendMessage(ChatColor.WHITE + "" + ChatColor.UNDERLINE + url2);

	}
	} catch(Exception E) {
		    System.out.println(E);
	}


    player.sendMessage("you have " + SatoshiQuest.REDIS.get("LivesLeft" + player.getUniqueId().toString()) + " lives!");

		try {
		satoshiQuest.updateScoreboard(player);
		} catch (Exception excep) {
			System.out.println(excep);
		}


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
                      + Long.toString((long)(satoshiQuest.getBalance(satoshiQuest.SERVERDISPLAY_NAME,1) * satoshiQuest.THIS_ROUND_WIN_PERC))
                      + " "
                      + satoshiQuest.DENOMINATION_NAME);
	player.sendMessage(
              "The loot pool unconfirmed is: "
                      + Long.toString((long)(satoshiQuest.getBalance(satoshiQuest.SERVERDISPLAY_NAME,0) * satoshiQuest.THIS_ROUND_WIN_PERC))
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

if(System.getenv("DISCORD_HOOK_URL")!=null) {
			if (isNewPlayer == true) {
			satoshiQuest.announce("NEW Player " + player.getName() + " joined!");
			satoshiQuest.sendDiscordMessage("NEW Player " + player.getName() + " joined!");
			} else {
			satoshiQuest.sendDiscordMessage("Player " + player.getName() + " joined with " + SatoshiQuest.REDIS.get("LivesLeft" + event.getPlayer().getUniqueId().toString()) + " lives");
			}
		}
player.sendMessage(ChatColor.WHITE + "More info: " + ChatColor.UNDERLINE + ""+satoshiQuest.SERVER_WEBSITE);
  }

	@EventHandler
	public void onPlayerGameModeChange(PlayerGameModeChangeEvent event) throws ParseException, org.json.simple.parser.ParseException, IOException {
                final Player player=event.getPlayer();
		if (player.getUniqueId().toString().equals(satoshiQuest.ADMIN_UUID.toString()))		
		event.setCancelled(false);
		else 
		event.setCancelled(true);
	}

    public boolean isAtSpawn(Player player)
	{
		Location spawn = Bukkit.getServer().getWorld(satoshiQuest.SERVERDISPLAY_NAME).getSpawnLocation();
	World getworld = Bukkit.getServer().getWorld(satoshiQuest.SERVERDISPLAY_NAME);
	if(player.getWorld()==getworld){
		double spawnx = spawn.getX();
		double spawnz = spawn.getZ();
		double playerx=(double)player.getLocation().getX();
                double playerz=(double)player.getLocation().getZ();
	        //System.out.println("x:"+playerx+" z:"+playerz);  //for testing lol
	if ((((playerx<spawnx+SatoshiQuest.SPAWN_PROTECT_RADIUS+1)&&(playerx>spawnx-SatoshiQuest.SPAWN_PROTECT_RADIUS-1))) && (((playerz<spawnz+SatoshiQuest.SPAWN_PROTECT_RADIUS+1)&&(playerz>spawnz-SatoshiQuest.SPAWN_PROTECT_RADIUS-1)))) {return true;}
	} 
        return false;//not
	
	}


  @EventHandler
  public void onPlayerMove(PlayerMoveEvent event)
      throws ParseException, org.json.simple.parser.ParseException, IOException {

	if (!SatoshiQuest.REDIS.exists("winner")) {
	if (event.getPlayer().getWorld() == Bukkit.getServer().getWorld("world")) {
	    		satoshiQuest.teleportToLootSpawn(event.getPlayer());
	}
//event.getPlayer().sendMessage(ChatColor.WHITE + "Welcome to " + SatoshiQuest.SERVERDISPLAY_NAME);
	}
	if (event.getFrom().getBlock() != event.getTo().getBlock()) {
      if ((Integer.parseInt(SatoshiQuest.REDIS.get("LivesLeft" + event.getPlayer().getUniqueId().toString())) <= 0) || (!satoshiQuest.canLeaveSpawn())) {
		if (isAtSpawn(event.getPlayer()) == false) {
			if (!SatoshiQuest.REDIS.exists("toldSpawn" + event.getPlayer().getUniqueId().toString())) {
			event.getPlayer().sendMessage(ChatColor.RED + "you cant leave spawn with 0 lives! or when a mod locks spawn.");
			event.getPlayer().sendMessage(ChatColor.GREEN + "try /wallet for your deposit & life info.");
			SatoshiQuest.REDIS.set("toldSpawn" + event.getPlayer().getUniqueId().toString(),"true");
			}
			satoshiQuest.teleportToSpawn(event.getPlayer());
		}
	}
	if (isAtSpawn(event.getPlayer()) == true) {
		event.getPlayer().setExhaustion(0);
		event.getPlayer().setFoodLevel(20);
		event.getPlayer().setHealth(event.getPlayer().getAttribute(Attribute.GENERIC_MAX_HEALTH).getValue());
	}
	satoshiQuest.didFindLoot(event.getPlayer());
	if (event.getFrom().getChunk() != event.getTo().getChunk()) {
		if (satoshiQuest.isNearLoot(event.getPlayer())) {
			//event.getPlayer().sendMessage(ChatColor.GREEN + "You are getting near... stay focused!");
		}
		try {
		satoshiQuest.updateScoreboard(event.getPlayer());
		} catch (Exception e){
		//e.printStackTrace();
		}
	}
	}
	World wNether = Bukkit.getServer().getWorld(satoshiQuest.SERVERDISPLAY_NAME+"_nether");
	if(event.getPlayer().getWorld()==wNether){
		if (event.getPlayer().getLocation().getY() >= 120) {
			if (!SatoshiQuest.REDIS.exists("toldNether" + event.getPlayer().getUniqueId().toString())) {
				event.getPlayer().sendMessage(ChatColor.RED + "you cant go on the roof!");
				SatoshiQuest.REDIS.set("toldNether" + event.getPlayer().getUniqueId().toString(), "True");
			}
		} 
		if (event.getPlayer().getLocation().getY() >= 125) {
			if (!SatoshiQuest.REDIS.exists("toldNether2" + event.getPlayer().getUniqueId().toString())) {
			event.getPlayer().sendMessage(ChatColor.RED + "Please DONT DO IT!");
				SatoshiQuest.REDIS.set("toldNether2" + event.getPlayer().getUniqueId().toString(), "True");
			}
		}
		if (event.getPlayer().getLocation().getY() >= 127) {
			if (!SatoshiQuest.REDIS.exists("toldNether3" + event.getPlayer().getUniqueId().toString())) {
			event.getPlayer().sendMessage(ChatColor.RED + "My disappointment is immeasurable and my day is ruined!");
			SatoshiQuest.REDIS.set("toldNethe3" + event.getPlayer().getUniqueId().toString(), "True");
			}
			satoshiQuest.teleportToLootSpawn(event.getPlayer());
		}
	} //wNether


  }
	@EventHandler
        public void onBlockBreak(BlockBreakEvent event){
	World getworld = Bukkit.getServer().getWorld(satoshiQuest.SERVERDISPLAY_NAME);
	if(event.getBlock().getWorld()==getworld){
	Location spawn = Bukkit.getServer().getWorld(satoshiQuest.SERVERDISPLAY_NAME).getSpawnLocation();
	Block spawnBlock = spawn.getBlock();
	double spawnx = spawn.getX();
	double spawnz = spawn.getZ();
	double spawny = spawn.getY();
	double spawnRadius = satoshiQuest.SPAWN_PROTECT_RADIUS;
	double posX = spawnx + (spawnRadius);
	double negX = spawnx - (spawnRadius);
	double posZ = spawnz + (spawnRadius);
	double negZ = spawnz - (spawnRadius);
		for(double x = negX; x <= posX; x++) {
			for(double z = negZ; z <= posZ; z++) {
			if(event.getBlock().getX() == x && event.getBlock().getZ() == z) { event.setCancelled(true);}
			}
		}
			for (double lootX = (Double.parseDouble(satoshiQuest.REDIS.get("lootSpawnX"))-1);lootX<=(Double.parseDouble(satoshiQuest.REDIS.get("lootSpawnX"))+1);lootX++) {
				for (double lootZ = (Double.parseDouble(satoshiQuest.REDIS.get("lootSpawnZ"))-1);lootZ<=(Double.parseDouble(satoshiQuest.REDIS.get("lootSpawnZ"))+1);lootZ++) {
					for (double lootY = Double.parseDouble(satoshiQuest.REDIS.get("lootSpawnY"));lootY<=(Double.parseDouble(satoshiQuest.REDIS.get("lootSpawnY"))+4);lootY++) {
	                			if(event.getBlock().getX() == lootX && event.getBlock().getZ() == lootZ && event.getBlock().getY() == lootY) {
							if (!SatoshiQuest.REDIS.exists("toldSpawn2" + event.getPlayer().getUniqueId().toString())) {
							event.getPlayer().sendMessage(ChatColor.RED + "you are not allow to do that.");
							SatoshiQuest.REDIS.set("toldSpawn2" + event.getPlayer().getUniqueId().toString(), "True");
							}
							event.setCancelled(true);
						}
					}//end for lootY
				}//end for lootZ
		}//end for lootX


          }//end get world
            }

	@EventHandler
        public void onBlockPlace(BlockPlaceEvent event){
	World getworld = Bukkit.getServer().getWorld(satoshiQuest.SERVERDISPLAY_NAME);
	if(event.getBlock().getWorld()==getworld){
	Location spawn = Bukkit.getServer().getWorld(satoshiQuest.SERVERDISPLAY_NAME).getSpawnLocation();
	Block spawnBlock = spawn.getBlock();
	double spawnx = spawn.getX();
	double spawnz = spawn.getZ();
	double spawny = spawn.getY();
	double spawnRadius = satoshiQuest.SPAWN_PROTECT_RADIUS;
	double posX = spawnx + (spawnRadius);
	double negX = spawnx - (spawnRadius);
	double posZ = spawnz + (spawnRadius);
	double negZ = spawnz - (spawnRadius);
		for(double x = negX; x <= posX; x++) {
			for(double z = negZ; z <= posZ; z++) {
			if(event.getBlock().getX() == x && event.getBlock().getZ() == z) { event.setCancelled(true);}
			}
		}
			for (double lootX = (Double.parseDouble(satoshiQuest.REDIS.get("lootSpawnX"))-1);lootX<=(Double.parseDouble(satoshiQuest.REDIS.get("lootSpawnX"))+1);lootX++) {
				for (double lootZ = (Double.parseDouble(satoshiQuest.REDIS.get("lootSpawnZ"))-1);lootZ<=(Double.parseDouble(satoshiQuest.REDIS.get("lootSpawnZ"))+1);lootZ++) {
					for (double lootY = Double.parseDouble(satoshiQuest.REDIS.get("lootSpawnY"));lootY<=(Double.parseDouble(satoshiQuest.REDIS.get("lootSpawnY"))+4);lootY++) {
	                			if(event.getBlock().getX() == lootX && event.getBlock().getZ() == lootZ && event.getBlock().getY() == lootY) {
						
						if (!SatoshiQuest.REDIS.exists("toldSpawn2" + event.getPlayer().getUniqueId().toString())) {
							event.getPlayer().sendMessage(ChatColor.RED + "you are not allow to do that.");
							SatoshiQuest.REDIS.set("toldSpawn2" + event.getPlayer().getUniqueId().toString(), "True");
						}
						event.setCancelled(true);
		}
					}//end for lootY
				}//end for lootZ
		}//end for lootX
          }
            }

  @EventHandler
  public void onPlayerInteract(PlayerInteractEvent event)
      throws ParseException, org.json.simple.parser.ParseException, IOException {

      // If player doesn't have permission, disallow the player to interact with it.
	if (isAtSpawn(event.getPlayer()) == true) {
        event.setCancelled(true);
	if (!SatoshiQuest.REDIS.exists("toldSpawn2" + event.getPlayer().getUniqueId().toString())) {
		event.getPlayer().sendMessage(ChatColor.RED + "you are not allow to do that.");
		SatoshiQuest.REDIS.set("toldSpawn2" + event.getPlayer().getUniqueId().toString(), "True");
	}
      }

    
  }


  @EventHandler
  public void onPlayerDeath(PlayerDeathEvent event) {

		Player p = event.getEntity();
		Player player = (Player) p;
		if(System.getenv("DISCORD_HOOK_URL")!=null) {
			satoshiQuest.sendDiscordMessage("" +event.getDeathMessage());
		}
if (isAtSpawn(player) == false) {
	if (Integer.parseInt(SatoshiQuest.REDIS.get("LivesLeft" + player.getUniqueId().toString())) >= 1)	{
		int livesLeft = Integer.parseInt(SatoshiQuest.REDIS.get("LivesLeft" + player.getUniqueId().toString())) - 1;
		if(!SatoshiQuest.REDIS.get("ModFlag").equals("true")) {
		SatoshiQuest.REDIS.set("LivesLeft" +player.getUniqueId().toString(),String.valueOf(livesLeft));

            player.sendMessage(ChatColor.RED + "You Lost a life. You have " + SatoshiQuest.REDIS.get("LivesLeft" + player.getUniqueId().toString())  + " lives left.");
		}
	}
	if (Integer.parseInt(SatoshiQuest.REDIS.get("LivesLeft" + player.getUniqueId().toString())) <= 0)	{
		//tp player to spawn
		   satoshiQuest.teleportToSpawn(player);
	SatoshiQuest.REDIS.set("LivesLeft" +player.getUniqueId().toString(),"0");
	}
	}//end spawncheck
	try {
	satoshiQuest.updateScoreboard(player);	
	} catch (Exception e){
		e.printStackTrace();
	}
  }

  String spawnKey(Location location) {
    return location.getWorld().getName()
        + location.getChunk().getX()
        + ","
        + location.getChunk().getZ()
        + "spawn";
  }


  @EventHandler
    public void onPortal(PlayerPortalEvent event) {
        Player player = event.getPlayer();

        if (event.getCause() == PlayerTeleportEvent.TeleportCause.NETHER_PORTAL) {
            event.useTravelAgent(true);
            event.getPortalTravelAgent().setCanCreatePortal(true);
            Location location;
            if (player.getWorld() == Bukkit.getServer().getWorld(satoshiQuest.SERVERDISPLAY_NAME)) {
                 location = new Location(Bukkit.getServer().getWorld(satoshiQuest.SERVERDISPLAY_NAME+"_nether"), event.getFrom().getBlockX() / 8, event.getFrom().getBlockY(), event.getFrom().getBlockZ() / 8);
            } else {
                location = new Location(Bukkit.getServer().getWorld(satoshiQuest.SERVERDISPLAY_NAME), event.getFrom().getBlockX() * 8, event.getFrom().getBlockY(), event.getFrom().getBlockZ() * 8);
            }
            event.setTo(event.getPortalTravelAgent().findOrCreate(location));
        }

        if (event.getCause() == PlayerTeleportEvent.TeleportCause.END_PORTAL) {
            if (player.getWorld() == Bukkit.getServer().getWorld(satoshiQuest.SERVERDISPLAY_NAME)) {
                Location loc = new Location(Bukkit.getServer().getWorld(satoshiQuest.SERVERDISPLAY_NAME+"_the_end"), 100, 50, 0); // This is the vanilla location for obsidian platform.
                event.setTo(loc);
                Block block = loc.getBlock();
                for (int x = block.getX() - 2; x <= block.getX() + 2; x++) {
                    for (int z = block.getZ() - 2; z <= block.getZ() + 2; z++) {
                        Block platformBlock = loc.getWorld().getBlockAt(x, block.getY() - 1, z);
                        if (platformBlock.getType() != Material.OBSIDIAN) {
                            platformBlock.setType(Material.OBSIDIAN);
                        }
                        for (int yMod = 1; yMod <= 3; yMod++) {
                            Block b = platformBlock.getRelative(BlockFace.UP, yMod);
                            if (b.getType() != Material.AIR) {
                                b.setType(Material.AIR);
                            }
                        }
                    }
                }
            } else if (player.getWorld() == Bukkit.getServer().getWorld(satoshiQuest.SERVERDISPLAY_NAME+"_the_end")) {
                event.setTo(Bukkit.getServer().getWorld(satoshiQuest.SERVERDISPLAY_NAME).getSpawnLocation());
            }
        }
    }

    @EventHandler
	void onExplode(EntityExplodeEvent event) {

	Location spawn = Bukkit.getServer().getWorld(satoshiQuest.SERVERDISPLAY_NAME).getSpawnLocation();
	World getworld = Bukkit.getServer().getWorld(satoshiQuest.SERVERDISPLAY_NAME);
	if(event.getEntity().getWorld()==getworld){
		double spawnx = spawn.getX();
		double spawnz = spawn.getZ();
		double eventx=(double)event.getLocation().getX();
                double eventz=(double)event.getLocation().getZ();
		double announceRadius = 10;
		double lootX = Double.parseDouble(satoshiQuest.REDIS.get("lootSpawnX"));
		double lootZ = Double.parseDouble(satoshiQuest.REDIS.get("lootSpawnZ"));
	        //System.out.println("x:"+playerx+" z:"+playerz);  //for testing lol
	if ((((eventx<spawnx+(announceRadius+SatoshiQuest.SPAWN_PROTECT_RADIUS)+1)&&(eventx>spawnx-(announceRadius+SatoshiQuest.SPAWN_PROTECT_RADIUS)-1))) && (((eventz<spawnz+(announceRadius+SatoshiQuest.SPAWN_PROTECT_RADIUS)+1)&&(eventz>spawnz-(announceRadius+SatoshiQuest.SPAWN_PROTECT_RADIUS)-1)))) {
		event.setCancelled(true);
	}
	if ((((eventx<lootX+announceRadius)&&(eventx>lootX-announceRadius))) && (((eventz<lootZ+announceRadius)&&(eventz>lootZ-announceRadius)))) {
		event.setCancelled(true);
	}

	}//end world


  }


}
