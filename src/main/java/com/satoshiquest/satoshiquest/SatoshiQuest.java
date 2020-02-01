//bitcoinjake09 11/9/2019 - a bitcoin tressure hunt in minecraft - satoshiquest
package com.satoshiquest.satoshiquest;

import com.satoshiquest.satoshiquest.commands.*;
import com.satoshiquest.satoshiquest.events.*;
import com.google.gson.JsonObject;
import java.io.*;
import java.net.*;
import java.sql.DriverManager;
import java.text.ParseException;
import java.text.*;
import java.util.*;
import java.math.BigDecimal;
import java.util.concurrent.TimeUnit;
import java.util.Date;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import javax.net.ssl.HttpsURLConnection;
import org.bukkit.*;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.*;
import org.bukkit.inventory.ItemStack;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitScheduler;
import org.bukkit.scoreboard.*;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import redis.clients.jedis.Jedis;
import org.bukkit.block.Block;
import org.bukkit.material.*;
import org.bukkit.Material;
import org.bukkit.material.MaterialData;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.material.Attachable;
import org.bukkit.attribute.Attribute;
import org.bukkit.advancement.*;

// Color Table :
// GREEN : Worked, YELLOW : Processing, LIGHT_PURPLE : Any money balance, BLUE : Player name,
// DARK_BLUE UNDERLINE : Link, RED : Server error, DARK_RED : User error, GRAY : Info, DARK_GRAY :
// Clan, DARK_GREEN : Landname

public class SatoshiQuest extends JavaPlugin {
  // TODO: remove env variables not being used anymore
  // Connecting to REDIS
  // Links to the administration account via Environment Variables
  public static final String SATOSHIQUEST_ENV =
      System.getenv("SATOSHIQUEST_ENV") != null ? System.getenv("SATOSHIQUEST_ENV") : "development";
  public static final UUID ADMIN_UUID =
      System.getenv("ADMIN_UUID") != null ? UUID.fromString(System.getenv("ADMIN_UUID")) : null;
 public static final String ADMIN_ADDRESS =
      System.getenv("ADMIN_ADDRESS") != null ? System.getenv("ADMIN_ADDRESS"): "1AABTCMd4X2Gv5tX2bisLiBQDKch8yomNM";
  public static final String BITCOIN_NODE_HOST =
      System.getenv("BITCOIN_NODE_HOST") != null
          ? System.getenv("BITCOIN_NODE_HOST")
          : null;
  public static final int BITCOIN_NODE_PORT =
      System.getenv("BITCOIN_PORT_8332_TCP_PORT") != null
          ? Integer.parseInt(System.getenv("BITCOIN_PORT_8332_TCP_PORT"))
          : 8332;
  public static final String SERVERDISPLAY_NAME =
      System.getenv("SERVERDISPLAY_NAME") != null ? System.getenv("SERVERDISPLAY_NAME") : "SatoshiQuest";
  public static final Long DENOMINATION_FACTOR =
      System.getenv("DENOMINATION_FACTOR") != null
          ? Long.parseLong(System.getenv("DENOMINATION_FACTOR"))
          : 1L;
  public static final String DENOMINATION_NAME =
      System.getenv("DENOMINATION_NAME") != null ? System.getenv("DENOMINATION_NAME") : "Sats";
  public static final String BITCOIN_NODE_USERNAME = System.getenv("BITCOIN_ENV_USERNAME");
  public static final String BITCOIN_NODE_PASSWORD = System.getenv("BITCOIN_ENV_PASSWORD");
  public static final String DISCORD_HOOK_URL = System.getenv("DISCORD_HOOK_URL");
  public static final Long MINER_FEE =
          System.getenv("MINER_FEE") != null ? Long.parseLong(System.getenv("MINER_FEE")) : 1000;

  public static final String SERVER_NAME =
      System.getenv("SERVER_NAME") != null ? System.getenv("SERVER_NAME") : "SatoshiQuest";

  public static final String ADDRESS_URL =
      System.getenv("ADDRESS_URL") != null ? System.getenv("ADDRESS_URL") : "https://www.blockchain.com/btc/address/";

  public static final String TX_URL =
      System.getenv("TX_URL") != null ? System.getenv("TX_URL") : "https://www.blockchain.com/btc/tx/";

  // REDIS: Look for Environment variables on hostname and port, otherwise defaults to
  // localhost:6379
  public static final String REDIS_HOST =
      System.getenv("REDIS_PORT_6379_TCP_ADDR") != null
          ? System.getenv("REDIS_PORT_6379_TCP_ADDR")
          : "localhost";
  public static final Integer REDIS_PORT =
      System.getenv("REDIS_PORT_6379_TCP_PORT") != null
          ? Integer.parseInt(System.getenv("REDIS_PORT_6379_TCP_PORT"))
          : 6379;
  public static final Jedis REDIS = new Jedis(REDIS_HOST, REDIS_PORT);

  // Default price: $1 in bitcoin
  public static final Double BUYIN_AMOUNT =
      System.getenv("BUYIN_AMOUNT") != null ? Double.parseDouble(System.getenv("BUYIN_AMOUNT")) : 1.00;

  public static final int LIVES_PERBUYIN =
      System.getenv("LIVES_PERBUYIN") != null ? Integer.parseInt(System.getenv("LIVES_PERBUYIN")) : 1;

  public int FEE_BLOCK_CONF =
      System.getenv("FEE_BLOCK_CONF") != null ? Integer.parseInt(System.getenv("FEE_BLOCK_CONF")) : 6;

  public static final Long SPAWN_PROTECT_RADIUS =
      System.getenv("SPAWN_PROTECT_RADIUS") != null ? Long.parseLong(System.getenv("SPAWN_PROTECT_RADIUS")) : 14;

  public static final Long LOOT_ANNOUNCE_RADIUS =
      System.getenv("LOOT_ANNOUNCE_RADIUS") != null ? Long.parseLong(System.getenv("LOOT_ANNOUNCE_RADIUS")) : 20;

  public Long LOOT_RADIUS_MIN =
      System.getenv("LOOT_RADIUS_MIN") != null ? Long.parseLong(System.getenv("LOOT_RADIUS_MIN")) : 1000;

  public Long LOOT_RADIUS_MAX =
      System.getenv("LOOT_RADIUS_MAX") != null ? Long.parseLong(System.getenv("LOOT_RADIUS_MAX")) : 10000;

  public static int rand(int min, int max) {
    return min + (int) (Math.random() * ((max - min) + 1));
  }
  public Long LootSpawnX = new Long(rand(LOOT_RADIUS_MIN.intValue(),LOOT_RADIUS_MAX.intValue()));
  public Long LootSpawnZ = new Long(rand(LOOT_RADIUS_MIN.intValue(),LOOT_RADIUS_MAX.intValue()));

  public NodeWallet wallet = null;
  public Player last_loot_player;

  public Long wallet_balance_cache = 0L;
  public Long exTime15 = new Date().getTime();
  public int discordWait15 = 3;
  public Double exRate = 0.0;
  public Long livesRate = 0L;
  public Long adminRate = 0L;
  public Long totalLifeRate = 0L;
  public boolean eventsLoaded = false;

  // when true, server is closed for maintenance and not allowing players to join in.
  public boolean maintenance_mode = false;

  private Map<String, CommandAction> commands;
  private Map<String, CommandAction> modCommands;
  Set<String> leaderBoardList = REDIS.keys("LeaderBoard *");

  private Player[] moderators;


  @Override
  public void onEnable() {
    log("[startup] SatoshiQuest starting");
	getServer().createWorld(new WorldCreator(SERVERDISPLAY_NAME));

	WorldCreator nc = new WorldCreator(SERVERDISPLAY_NAME+"_nether");
	nc.environment(World.Environment.NETHER); //Change type of world eg; Normal, Nether, End.
	nc.createWorld();

	WorldCreator ec = new WorldCreator(SERVERDISPLAY_NAME+"_the_end");
	ec.environment(World.Environment.THE_END); //Change type of world eg; Normal, Nether, End.
	ec.createWorld();

    try {
  
      if (ADMIN_UUID == null) {
        log("[warning] ADMIN_UUID env variable to is not set.");
      }
  leaderBoardList = REDIS.keys("LeaderBoard *");
  int iter=1;
  for (String templeaderBoardList : leaderBoardList) {
	System.out.println(" "+iter+") "+ REDIS.get(templeaderBoardList));
		iter++;
  }
      // registers listener classes
	if (eventsLoaded == false) {
      getServer().getPluginManager().registerEvents(new EntityEvents(this), this);
      getServer().getPluginManager().registerEvents(new ServerEvents(this), this);
	//REDIS.del("spawnCreated");
	//REDIS.del("lootSpawnY");
	eventsLoaded = true;	
	}

      // player does not lose inventory on death
      System.out.println("[startup] Starting " + SERVERDISPLAY_NAME);

      // loads config file. If it doesn't exist, creates it.
      getDataFolder().mkdir();
      System.out.println("[startup] checking default config file");

      if (!new java.io.File(getDataFolder(), "config.yml").exists()) {
        saveDefaultConfig();
        System.out.println("[startup] config file does not exist. creating default.");
      }

      // loads world wallet from env variables. If not present, generates a new one each time the
      // server is run.
      /*
	if (System.getenv("PRIVATE") != null
          && System.getenv("ADDRESS") != null) {
        wallet =
                new NodeWallet(
                        System.getenv("PRIVATE"),
                        System.getenv("ADDRESS"));
        System.out.println("[world wallet] imported from environment");
      } else if (REDIS.exists("private") && REDIS.exists("address")){
        wallet =
                new NodeWallet(
                        REDIS.get("private"),
                        REDIS.get("address"));
      } else {
        wallet = new NodeWallet(this.toString());
        System.out.println("[world wallet] generated new wallet");
      } */


  //listWallets();

	//getWalletInfo(SERVERDISPLAY_NAME);
	if (!REDIS.exists("ModFlag")) {
		REDIS.set("ModFlag","false");
	}
	if (!REDIS.exists("gameRound")) {
		REDIS.set("gameRound","1");
	}
	if (!REDIS.exists("nodeAddress"+SERVERDISPLAY_NAME)) {
	if (getWalletInfo(SERVERDISPLAY_NAME)!=false) {
		try {
			wallet = loadWallet(SERVERDISPLAY_NAME);
		        System.out.println("[world wallet] trying to load node wallet");
		} catch (NullPointerException npe) {
			npe.printStackTrace();
			System.out.println("[world wallet] wallet not found, attempting to create.");
		}
	} else
	{
	        wallet = generateNewWallet(SERVERDISPLAY_NAME);
        	System.out.println("[world wallet] generated new wallet");
		//REDIS.set("nodeWallet"+SERVERDISPLAY_NAME,SERVERDISPLAY_NAME);
	} 
	} else { 
	wallet = loadWallet(SERVERDISPLAY_NAME);
	}//nodewallet
	if (!REDIS.exists("nodeAddress"+SERVERDISPLAY_NAME)) {
	try {
		if (getAccountAddress(SERVERDISPLAY_NAME)!=null) {
			REDIS.set("nodeAddress"+SERVERDISPLAY_NAME,getAccountAddress(SERVERDISPLAY_NAME));
		} else {
			REDIS.set("nodeAddress"+SERVERDISPLAY_NAME,wallet.getNewAccountAddress());
		}
	} catch (NullPointerException npe2) {
			npe2.printStackTrace();
			System.out.println("[world address] address not found, attempting to create.");
		}
	}//endAddress


	System.out.println("[Admin address] address: " + ADMIN_ADDRESS);      
        System.out.println("[world wallet] address: " + REDIS.get("nodeAddress"+SERVERDISPLAY_NAME));
	//System.out.println("[world address] address: " + REDIS.get("nodeAddress"));
        //System.out.println("The loot pool is: " + (int)(wallet.getBalance(0)/DENOMINATION_FACTOR));


      if (BITCOIN_NODE_HOST != null) {
        System.out.println("[startup] checking bitcoin node connection");
        getBlockChainInfo();
      }
	//REDIS.del("spawnCreated");
	if (!REDIS.exists("spawnCreated")) {
		addSpawnBlocks();
		REDIS.set("spawnCreated", "true");
	System.out.println("[Spawn Created] : " + REDIS.get("spawnCreated"));      
	}
	

		

	
	
	if (!REDIS.exists("FEE_BLOCK_CONF")) {
		REDIS.set("FEE_BLOCK_CONF", Integer.toString(FEE_BLOCK_CONF));
	}
	//REDIS.del("LOOT_RADIUS_MIN"); //use for testing :p
	if (!REDIS.exists("LOOT_RADIUS_MIN")) {
		REDIS.set("LOOT_RADIUS_MIN", LOOT_RADIUS_MIN.toString());
		REDIS.set("LOOT_RADIUS_MAX", LOOT_RADIUS_MAX.toString());
	} else {

		if (REDIS.exists("pushloot")){	REDIS.set("LOOT_RADIUS_MIN",Long.toString((long)Math.round((Double.valueOf(REDIS.get("LOOT_RADIUS_MIN")) * 0.1) + Double.valueOf(REDIS.get("LOOT_RADIUS_MIN")))));

REDIS.set("LOOT_RADIUS_MAX",Long.toString((long)Math.round((Double.valueOf(REDIS.get("LOOT_RADIUS_MAX")) * 0.1) + Double.valueOf(REDIS.get("LOOT_RADIUS_MAX")))));
	REDIS.del("pushloot");
	}

	}

	LOOT_RADIUS_MIN = Long.parseLong(REDIS.get("LOOT_RADIUS_MIN"));
	LOOT_RADIUS_MAX = Long.parseLong(REDIS.get("LOOT_RADIUS_MAX"));
	System.out.println("[LOOT_RADIUS_MIN] : " +LOOT_RADIUS_MIN);
	System.out.println("[LOOT_RADIUS_MAX] : " +LOOT_RADIUS_MAX);
	if ((LOOT_RADIUS_MIN >= 2147483647) || (LOOT_RADIUS_MIN <= -2147483647)) {
		LOOT_RADIUS_MIN = Long.parseLong(System.getenv("LOOT_RADIUS_MIN"));
	}

	if ((LOOT_RADIUS_MAX >= 2147483647) || (LOOT_RADIUS_MAX <= -2147483647)) {
		LOOT_RADIUS_MAX = Long.parseLong(System.getenv("LOOT_RADIUS_MAX"));
	}

	REDIS.set("LOOT_ANNOUNCE_RADIUS",Long.toString(LOOT_RADIUS_MAX/4));

  LootSpawnX = new Long(rand(LOOT_RADIUS_MIN.intValue(),LOOT_RADIUS_MAX.intValue()));
  LootSpawnZ = new Long(rand(LOOT_RADIUS_MIN.intValue(),LOOT_RADIUS_MAX.intValue()));

	int can0 = rand(0,10);
	if (can0 < 5) {
  LootSpawnX = new Long(rand(1,LOOT_RADIUS_MAX.intValue()));
	} else {
  LootSpawnZ = new Long(rand(1,LOOT_RADIUS_MAX.intValue()));
	}

	int posNeg = rand(0,10);
	if (posNeg < 5) {
		LootSpawnX = LootSpawnX * -1;
	} else if (posNeg == 2) {
		LootSpawnX = LootSpawnX * 1;
	}
	posNeg = rand(0,10);
	if (posNeg < 5) {
		LootSpawnZ = LootSpawnZ * -1;
	} else if (posNeg == 2) {
		LootSpawnZ = LootSpawnZ * 1;
	}
	long sX = (long)Bukkit.getWorld(SERVERDISPLAY_NAME).getSpawnLocation().getX();
	long sZ = (long)Bukkit.getWorld(SERVERDISPLAY_NAME).getSpawnLocation().getZ();
	
	LootSpawnX = sX + LootSpawnX;
	LootSpawnZ = sZ + LootSpawnZ;


	//REDIS.del("lootSpawnY"); //use for testing :p
	if (!REDIS.exists("lootSpawnY")) {
	REDIS.set("lootSpawnX",LootSpawnX.toString());
	REDIS.set("lootSpawnZ",LootSpawnZ.toString());
	}
  System.out.println("LOOT_ANNOUNCE_RADIUS: " + REDIS.get("LOOT_ANNOUNCE_RADIUS"));

  System.out.println("Loot X,Z: " + REDIS.get("lootSpawnX") + " " + REDIS.get("lootSpawnZ"));

	setLootBlocks();
      // creates scheduled timers (update balances, etc)
      createScheduledTimers();
      commands = new HashMap<String, CommandAction>();
      commands.put("wallet", new WalletCommand(this));
      commands.put("tip", new TipCommand(this));
      commands.put("send", new SendCommand(this));
      commands.put("lives", new LivesCommand(this));
      commands.put("leaderboard", new LeaderBoardCommand(this));
      modCommands = new HashMap<String, CommandAction>();
      modCommands.put("fixleaderboard", new FixLeaderBoardCommand(this));
      modCommands.put("crashTest", new CrashtestCommand(this));
      modCommands.put("mod", new ModCommand(this));
      modCommands.put("reset", new ResetCommand(this));
      modCommands.put("SetFee", new SetFeeCommand(this));
      modCommands.put("setlives", new SetLivesCommand(this));
      modCommands.put("ban", new BanCommand());
      modCommands.put("unban", new UnbanCommand());
      modCommands.put("banlist", new BanlistCommand());
      modCommands.put("spectate", new SpectateCommand(this));
      modCommands.put("emergencystop", new EmergencystopCommand());
      modCommands.put("motd", new MOTDCommand(this));
      System.out.println("[startup] finished");

    } catch (Exception e) {
      e.printStackTrace();
      System.out.println("[fatal] plugin enable fails");
      Bukkit.shutdown();
    }
  }

  public static final NodeWallet generateNewWallet(String account_id)
      throws IOException, org.json.simple.parser.ParseException {
    JSONParser parser = new JSONParser();

    final JSONObject jsonObject = new JSONObject();
    jsonObject.put("jsonrpc", "1.0");
    jsonObject.put("id", "satoshiquest");
    jsonObject.put("method", "createwallet");
    JSONArray params = new JSONArray();
    params.add(account_id);
    //System.out.println("Creating wallet: " + account_id);
    //System.out.println(params);
    jsonObject.put("params", params);
    //System.out.println("Checking blockchain info...");
    URL url = new URL("http://" + SatoshiQuest.BITCOIN_NODE_HOST + ":" + SatoshiQuest.BITCOIN_NODE_PORT);
    //System.out.println(url.toString());
    HttpURLConnection con = (HttpURLConnection) url.openConnection();
    String userPassword = SatoshiQuest.BITCOIN_NODE_USERNAME + ":" + SatoshiQuest.BITCOIN_NODE_PASSWORD;
    String encoding = Base64.getEncoder().encodeToString(userPassword.getBytes());
    con.setRequestProperty("Authorization", "Basic " + encoding);

    con.setRequestMethod("POST");
    con.setRequestProperty("User-Agent", "Mozilla/1.22 (compatible; MSIE 2.0; Windows 3.1)");
    con.setRequestProperty("Accept-Language", "en-US,en;q=0.5");
    con.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
    con.setDoOutput(true);
    OutputStreamWriter out = new OutputStreamWriter(con.getOutputStream());
    //System.out.println(jsonObject.toString());
    out.write(jsonObject.toString());
    out.close();

    if(con.getResponseCode()==200) {
        BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
        String inputLine;
        StringBuffer response = new StringBuffer();

        while ((inputLine = in.readLine()) != null) {
            response.append(inputLine);
        }
        in.close();
        //System.out.println(response.toString());
        JSONObject response_object = (JSONObject) parser.parse(response.toString());
        //System.out.println(response_object);

        return new NodeWallet(account_id);
    } else {
        BufferedReader in = new BufferedReader(new InputStreamReader(con.getErrorStream()));
        String inputLine;
        StringBuffer response = new StringBuffer();

        while ((inputLine = in.readLine()) != null) {
            response.append(inputLine);
        }
        in.close();
        //System.out.println(response.toString());
        JSONObject response_object = (JSONObject) parser.parse(response.toString());
        //System.out.println(response_object);
        return null;
    }


  }

    public static final void listWallets()
            throws IOException, org.json.simple.parser.ParseException {
        JSONParser parser = new JSONParser();

        final JSONObject jsonObject = new JSONObject();
        jsonObject.put("jsonrpc", "1.0");
        jsonObject.put("id", "satoshiquest");
        jsonObject.put("method", "listwallets");
        JSONArray params = new JSONArray();
        //System.out.println(params);
        jsonObject.put("params", params);
        URL url = new URL("http://" + SatoshiQuest.BITCOIN_NODE_HOST + ":" + SatoshiQuest.BITCOIN_NODE_PORT+ "/wallet/");
        //System.out.println(url.toString());
        HttpURLConnection con = (HttpURLConnection) url.openConnection();
        String userPassword = SatoshiQuest.BITCOIN_NODE_USERNAME + ":" + SatoshiQuest.BITCOIN_NODE_PASSWORD;
        String encoding = Base64.getEncoder().encodeToString(userPassword.getBytes());
        con.setRequestProperty("Authorization", "Basic " + encoding);

        con.setRequestMethod("POST");
        con.setRequestProperty("User-Agent", "Mozilla/1.22 (compatible; MSIE 2.0; Windows 3.1)");
        con.setRequestProperty("Accept-Language", "en-US,en;q=0.5");
        con.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
        con.setDoOutput(true);
        OutputStreamWriter out = new OutputStreamWriter(con.getOutputStream());
        //System.out.println(jsonObject.toString());
        out.write(jsonObject.toString());
        out.close();

        if(con.getResponseCode()==200) {
            BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
            String inputLine;
            StringBuffer response = new StringBuffer();

            while ((inputLine = in.readLine()) != null) {
                response.append(inputLine);
            }
            in.close();
            //System.out.println(response.toString());
            JSONObject response_object = (JSONObject) parser.parse(response.toString());
            //System.out.println(response_object);

        } else {
            BufferedReader in = new BufferedReader(new InputStreamReader(con.getErrorStream()));
            String inputLine;
            StringBuffer response = new StringBuffer();

            while ((inputLine = in.readLine()) != null) {
                response.append(inputLine);
            }
            in.close();
            //System.out.println(response.toString());
            JSONObject response_object = (JSONObject) parser.parse(response.toString());
            //System.out.println(response_object);
        }


    }

    public static final boolean getWalletInfo(String account_id)
            throws IOException, org.json.simple.parser.ParseException {
        JSONParser parser = new JSONParser();

        final JSONObject jsonObject = new JSONObject();
        jsonObject.put("jsonrpc", "1.0");
        jsonObject.put("id", "satoshiquest");
        jsonObject.put("method", "getwalletinfo");
        JSONArray params = new JSONArray();
        //System.out.println(params);
        jsonObject.put("params", params);
        URL url = new URL("http://" + SatoshiQuest.BITCOIN_NODE_HOST + ":" + SatoshiQuest.BITCOIN_NODE_PORT + "/wallet/" + account_id);
        //System.out.println(url.toString());
        HttpURLConnection con = (HttpURLConnection) url.openConnection();
        String userPassword = SatoshiQuest.BITCOIN_NODE_USERNAME + ":" + SatoshiQuest.BITCOIN_NODE_PASSWORD;
        String encoding = Base64.getEncoder().encodeToString(userPassword.getBytes());
        con.setRequestProperty("Authorization", "Basic " + encoding);

        con.setRequestMethod("POST");
        con.setRequestProperty("User-Agent", "Mozilla/1.22 (compatible; MSIE 2.0; Windows 3.1)");
        con.setRequestProperty("Accept-Language", "en-US,en;q=0.5");
        con.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
        con.setDoOutput(true);
        OutputStreamWriter out = new OutputStreamWriter(con.getOutputStream());
        //System.out.println(jsonObject.toString());
        out.write(jsonObject.toString());
        out.close();

        if(con.getResponseCode()==200) {
            BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
            String inputLine;
            StringBuffer response = new StringBuffer();

            while ((inputLine = in.readLine()) != null) {
                response.append(inputLine);
            }
            in.close();
            //System.out.println(response.toString());
            JSONObject response_object = (JSONObject) parser.parse(response.toString());
            //System.out.println(response_object);
		return true;

        } else {
            BufferedReader in = new BufferedReader(new InputStreamReader(con.getErrorStream()));
            String inputLine;
            StringBuffer response = new StringBuffer();

            while ((inputLine = in.readLine()) != null) {
                response.append(inputLine);
            }
            in.close();
            //System.out.println(response.toString());
            JSONObject response_object = (JSONObject) parser.parse(response.toString());
            //System.out.println(response_object);
		return false;
        }


    }

  public static final NodeWallet loadWallet(String account_id)
      throws IOException, org.json.simple.parser.ParseException {
    JSONParser parser = new JSONParser();

    final JSONObject jsonObject = new JSONObject();
    jsonObject.put("jsonrpc", "1.0");
    jsonObject.put("id", "satoshiquest");
    jsonObject.put("method", "loadwallet");
    JSONArray params = new JSONArray();
    params.add(account_id);
    //System.out.println("Loading wallet: " + account_id);
    //System.out.println(params);
    jsonObject.put("params", params);
    //System.out.println("Checking blockchain info...");
    URL url = new URL("http://" + SatoshiQuest.BITCOIN_NODE_HOST + ":" + SatoshiQuest.BITCOIN_NODE_PORT + "/wallet/");
    //System.out.println(url.toString());
    //System.out.println(jsonObject.toString());

      HttpURLConnection con = (HttpURLConnection) url.openConnection();
    String userPassword = SatoshiQuest.BITCOIN_NODE_USERNAME + ":" + SatoshiQuest.BITCOIN_NODE_PASSWORD;
    String encoding = Base64.getEncoder().encodeToString(userPassword.getBytes());
    con.setRequestProperty("Authorization", "Basic " + encoding);

    con.setRequestMethod("POST");
    con.setRequestProperty("User-Agent", "Mozilla/1.22 (compatible; MSIE 2.0; Windows 3.1)");
    con.setRequestProperty("Accept-Language", "en-US,en;q=0.5");
    con.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
    con.setDoOutput(true);
    OutputStreamWriter out = new OutputStreamWriter(con.getOutputStream());
    out.write(jsonObject.toString());
    out.close();

    if(con.getResponseCode()==200) {

        BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
        String inputLine;
        StringBuffer response = new StringBuffer();

        while ((inputLine = in.readLine()) != null) {
            response.append(inputLine);
        }
        in.close();
        //System.out.println(response.toString());
        JSONObject response_object = (JSONObject) parser.parse(response.toString());
        //System.out.println(response_object);

        return new NodeWallet(account_id);
    } else {
        BufferedReader in = new BufferedReader(new InputStreamReader(con.getErrorStream()));
        String inputLine;
        StringBuffer response = new StringBuffer();

        while ((inputLine = in.readLine()) != null) {
            response.append(inputLine);
        }
        in.close();
        //System.out.println(response.toString());
        JSONObject response_object = (JSONObject) parser.parse(response.toString());
	
        return null;
    }
  }
  // @todo: make this just accept the endpoint name and (optional) parameters
  public JSONObject getBlockChainInfo() throws org.json.simple.parser.ParseException {
    JSONParser parser = new JSONParser();

    try {
      final JSONObject jsonObject = new JSONObject();
      jsonObject.put("jsonrpc", "1.0");
      jsonObject.put("id", "satoshiquest");
      jsonObject.put("method", "getblockchaininfo");
      JSONArray params = new JSONArray();
      jsonObject.put("params", params);
      //System.out.println("Checking blockchain info...");
      URL url = new URL("http://" + BITCOIN_NODE_HOST + ":" + BITCOIN_NODE_PORT);
      //System.out.println(url.toString());
      HttpURLConnection con = (HttpURLConnection) url.openConnection();
      String userPassword = BITCOIN_NODE_USERNAME + ":" + BITCOIN_NODE_PASSWORD;
      String encoding = java.util.Base64.getEncoder().encodeToString(userPassword.getBytes());
      con.setRequestProperty("Authorization", "Basic " + encoding);

      con.setRequestMethod("POST");
      con.setRequestProperty("User-Agent", "satoshiquest plugin");
      con.setRequestProperty("Accept-Language", "en-US,en;q=0.5");
      con.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
      con.setDoOutput(true);
      OutputStreamWriter out = new OutputStreamWriter(con.getOutputStream());
      out.write(jsonObject.toString());
      out.close();

    if(con.getResponseCode()==200) {

        BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
        String inputLine;
        StringBuffer response = new StringBuffer();

        while ((inputLine = in.readLine()) != null) {
            response.append(inputLine);
        }
        in.close();
        //System.out.println(response.toString());
        JSONObject response_object = (JSONObject) parser.parse(response.toString());
        //System.out.println(response_object);
      return (JSONObject) parser.parse(response.toString());
    } else {
        BufferedReader in = new BufferedReader(new InputStreamReader(con.getErrorStream()));
        String inputLine;
        StringBuffer response = new StringBuffer();

        while ((inputLine = in.readLine()) != null) {
            response.append(inputLine);
        }
        in.close();
        //System.out.println(response.toString());
        JSONObject response_object = (JSONObject) parser.parse(response.toString());
	
        return new JSONObject();
    }
    } catch (IOException e) {
      System.out.println("problem connecting with bitcoin node");
      System.out.println(e);
      // Unable to call API?
    }

    return new JSONObject(); // just give them an empty object
  }

  public Long getReceivedByAddress(String account_id, int confirmations) throws IOException, org.json.simple.parser.ParseException {
	try {
	String address = REDIS.get("nodeAddress" + account_id);
        JSONParser parser = new JSONParser();

        final JSONObject jsonObject = new JSONObject();
        jsonObject.put("jsonrpc", "1.0");
        jsonObject.put("id", "satoshiquest");
        jsonObject.put("method", "getreceivedbyaddress");
        JSONArray params = new JSONArray();
	params.add(address);
	params.add(confirmations);
        //System.out.println("Parms: " + params);
        jsonObject.put("params", params);
        URL url = new URL("http://" + SatoshiQuest.BITCOIN_NODE_HOST + ":" + SatoshiQuest.BITCOIN_NODE_PORT + "/wallet/" + account_id);
        //System.out.println(url.toString());
        HttpURLConnection con = (HttpURLConnection) url.openConnection();
        String userPassword = SatoshiQuest.BITCOIN_NODE_USERNAME + ":" + SatoshiQuest.BITCOIN_NODE_PASSWORD;
        String encoding = Base64.getEncoder().encodeToString(userPassword.getBytes());
        con.setRequestProperty("Authorization", "Basic " + encoding);

        con.setRequestMethod("POST");
        con.setRequestProperty("User-Agent", "Mozilla/1.22 (compatible; MSIE 2.0; Windows 3.1)");
        con.setRequestProperty("Accept-Language", "en-US,en;q=0.5");
        con.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
        con.setDoOutput(true);
        OutputStreamWriter out = new OutputStreamWriter(con.getOutputStream());
        //System.out.println(jsonObject.toString());
        out.write(jsonObject.toString());
        out.close();

        if(con.getResponseCode()==200) {
            BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
            String inputLine;
            StringBuffer response = new StringBuffer();

            while ((inputLine = in.readLine()) != null) {
                response.append(inputLine);
            }
            in.close();
            //System.out.println(response.toString());
            JSONObject response_object = (JSONObject) parser.parse(response.toString());
	    Double d = Double.parseDouble(response_object.get("result").toString().trim()) * 100000000L;
	    final Long balance = d.longValue();
            //System.out.println(balance);
	    return balance;

        } else {
            BufferedReader in = new BufferedReader(new InputStreamReader(con.getErrorStream()));
            String inputLine;
            StringBuffer response = new StringBuffer();

            while ((inputLine = in.readLine()) != null) {
                response.append(inputLine);
            }
            in.close();
            //System.out.println(response.toString());
            JSONObject response_object = (JSONObject) parser.parse(response.toString());
	    Double d = Double.parseDouble(response_object.get("result").toString().trim()) * 100000000L;
	    final Long balance = d.longValue();
            //System.out.println(balance);
	    return balance;
        }
	} catch(Exception e) {
		e.printStackTrace();
	}
	return 0L;
   }

  public Long getBalance(String account_id, int confirmations) throws IOException, org.json.simple.parser.ParseException {
	try {
	//String address = REDIS.get("nodeAddress" + account_id);
        JSONParser parser = new JSONParser();

        final JSONObject jsonObject = new JSONObject();
        jsonObject.put("jsonrpc", "1.0");
        jsonObject.put("id", "satoshiquest");
        jsonObject.put("method", "getbalance");
        JSONArray params = new JSONArray();
	params.add("*");
	params.add(confirmations);
        //System.out.println("Parms: " + params);
        jsonObject.put("params", params);
        URL url = new URL("http://" + SatoshiQuest.BITCOIN_NODE_HOST + ":" + SatoshiQuest.BITCOIN_NODE_PORT + "/wallet/" + account_id);
        //System.out.println(url.toString());
        HttpURLConnection con = (HttpURLConnection) url.openConnection();
        String userPassword = SatoshiQuest.BITCOIN_NODE_USERNAME + ":" + SatoshiQuest.BITCOIN_NODE_PASSWORD;
        String encoding = Base64.getEncoder().encodeToString(userPassword.getBytes());
        con.setRequestProperty("Authorization", "Basic " + encoding);

        con.setRequestMethod("POST");
        con.setRequestProperty("User-Agent", "Mozilla/1.22 (compatible; MSIE 2.0; Windows 3.1)");
        con.setRequestProperty("Accept-Language", "en-US,en;q=0.5");
        con.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
        con.setDoOutput(true);
        OutputStreamWriter out = new OutputStreamWriter(con.getOutputStream());
        //System.out.println(jsonObject.toString());
        out.write(jsonObject.toString());
        out.close();

        if(con.getResponseCode()==200) {
            BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
            String inputLine;
            StringBuffer response = new StringBuffer();

            while ((inputLine = in.readLine()) != null) {
                response.append(inputLine);
            }
            in.close();
            //System.out.println(response.toString());
            JSONObject response_object = (JSONObject) parser.parse(response.toString());
	    Double d = Double.parseDouble(response_object.get("result").toString().trim()) * 100000000L;
	    final Long balance = d.longValue();
            //System.out.println(balance);
	    return balance;

        } else {
            BufferedReader in = new BufferedReader(new InputStreamReader(con.getErrorStream()));
            String inputLine;
            StringBuffer response = new StringBuffer();

            while ((inputLine = in.readLine()) != null) {
                response.append(inputLine);
            }
            in.close();
            //System.out.println(response.toString());
            JSONObject response_object = (JSONObject) parser.parse(response.toString());
	    Double d = Double.parseDouble(response_object.get("result").toString().trim()) * 100000000L;
	    final Long balance = d.longValue();
            //System.out.println(balance);
	    return balance;
        }
	} catch(Exception e) {
		e.printStackTrace();
	}
	return 0L;
   }

  public Long getUnconfirmedBalance(String account_id) throws IOException, org.json.simple.parser.ParseException {
	try {
	//String address = REDIS.get("nodeAddress" + account_id);
        JSONParser parser = new JSONParser();

        final JSONObject jsonObject = new JSONObject();
        jsonObject.put("jsonrpc", "1.0");
        jsonObject.put("id", "satoshiquest");
        jsonObject.put("method", "getunconfirmedbalance");
        URL url = new URL("http://" + SatoshiQuest.BITCOIN_NODE_HOST + ":" + SatoshiQuest.BITCOIN_NODE_PORT + "/wallet/" + account_id);
        //System.out.println(url.toString());
        HttpURLConnection con = (HttpURLConnection) url.openConnection();
        String userPassword = SatoshiQuest.BITCOIN_NODE_USERNAME + ":" + SatoshiQuest.BITCOIN_NODE_PASSWORD;
        String encoding = Base64.getEncoder().encodeToString(userPassword.getBytes());
        con.setRequestProperty("Authorization", "Basic " + encoding);

        con.setRequestMethod("POST");
        con.setRequestProperty("User-Agent", "Mozilla/1.22 (compatible; MSIE 2.0; Windows 3.1)");
        con.setRequestProperty("Accept-Language", "en-US,en;q=0.5");
        con.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
        con.setDoOutput(true);
        OutputStreamWriter out = new OutputStreamWriter(con.getOutputStream());
        //System.out.println(jsonObject.toString());
        out.write(jsonObject.toString());
        out.close();

        if(con.getResponseCode()==200) {
            BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
            String inputLine;
            StringBuffer response = new StringBuffer();

            while ((inputLine = in.readLine()) != null) {
                response.append(inputLine);
            }
            in.close();
            //System.out.println(response.toString());
            JSONObject response_object = (JSONObject) parser.parse(response.toString());
	    Double d = Double.parseDouble(response_object.get("result").toString().trim()) * 100000000L;
	    final Long balance = d.longValue();
            //System.out.println(balance);
	    return balance;

        } else {
            BufferedReader in = new BufferedReader(new InputStreamReader(con.getErrorStream()));
            String inputLine;
            StringBuffer response = new StringBuffer();

            while ((inputLine = in.readLine()) != null) {
                response.append(inputLine);
            }
            in.close();
            //System.out.println(response.toString());
            JSONObject response_object = (JSONObject) parser.parse(response.toString());
	    Double d = Double.parseDouble(response_object.get("result").toString().trim()) * 100000000L;
	    final Long balance = d.longValue();
            //System.out.println(balance);
	    return balance;
        }
	} catch(Exception e) {
		e.printStackTrace();
	}
	return 0L;
   }

  public String getAccountAddress(String account_id) throws IOException, ParseException {

    JSONParser parser = new JSONParser();

    final JSONObject jsonObject = new JSONObject();
    jsonObject.put("jsonrpc", "1.0");
    jsonObject.put("id", "satoshiquest");
    jsonObject.put("method", "getaddressesbylabel");
    JSONArray params = new JSONArray();
    params.add(account_id);
    jsonObject.put("params", params);
    URL url = new URL("http://" + BITCOIN_NODE_HOST + ":" + BITCOIN_NODE_PORT + "/wallet/" + account_id);
    HttpURLConnection con = (HttpURLConnection) url.openConnection();
    String userPassword = BITCOIN_NODE_USERNAME + ":" + BITCOIN_NODE_PASSWORD;
    String encoding = Base64.getEncoder().encodeToString(userPassword.getBytes());
    con.setRequestProperty("Authorization", "Basic " + encoding);
    con.setConnectTimeout(5000);
    con.setRequestMethod("POST");
    con.setRequestProperty("User-Agent", "Mozilla/1.22 (compatible; MSIE 2.0; Windows 3.1)");
    con.setRequestProperty("Accept-Language", "en-US,en;q=0.5");
    con.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
    con.setDoOutput(true);
    OutputStreamWriter out = new OutputStreamWriter(con.getOutputStream());
    out.write(jsonObject.toString());
    out.close();
	try {
    if(con.getResponseCode()==200) {

    BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
    String inputLine;
    StringBuffer response = new StringBuffer();

    while ((inputLine = in.readLine()) != null) {
      response.append(inputLine);
    }
    in.close();

    JSONObject response_object = (JSONObject) parser.parse(response.toString());

	String stringToSplit = response_object.get("result").toString();
        String[] tempArray;
        tempArray = stringToSplit.split("\"");
	int tempAddy = 0;
        for (int i = 0; i < tempArray.length; i++)
		if (tempArray[i].length() > 16) {
          	  //System.out.println(tempArray[i]);
		  tempAddy = i;
		}


    return tempArray[tempAddy];
    } else {
        BufferedReader in = new BufferedReader(new InputStreamReader(con.getErrorStream()));
        String inputLine;
        StringBuffer response = new StringBuffer();

        while ((inputLine = in.readLine()) != null) {
            response.append(inputLine);
        }
        in.close();
        //System.out.println(response.toString());
        JSONObject response_object = (JSONObject) parser.parse(response.toString());
	return null;
	}
	} catch(Exception e) {
		e.printStackTrace();
	}
	return null;
  }

  public String sendToAddress(String account_id, String address, Long sat) throws IOException, ParseException {
try {
    JSONParser parser = new JSONParser();

    final JSONObject jsonObject = new JSONObject();
    jsonObject.put("jsonrpc", "1.0");
    jsonObject.put("id", "satoshiquest");
    jsonObject.put("method", "sendtoaddress");
    JSONArray params = new JSONArray();
    params.add(address);
    //System.out.println(sat);
    BigDecimal decimalSat = new BigDecimal(sat * 0.00000001);
    decimalSat = decimalSat.setScale(8, BigDecimal.ROUND_DOWN);
    //System.out.println(decimalSat);
    params.add(decimalSat);
    params.add(SERVERDISPLAY_NAME);
    params.add(SERVERDISPLAY_NAME);
    params.add(false);
    params.add(false);
    params.add(FEE_BLOCK_CONF);
    //System.out.println(params);
    jsonObject.put("params", params);
    //System.out.println("Checking blockchain info...");
    URL url = new URL("http://" + BITCOIN_NODE_HOST + ":" + BITCOIN_NODE_PORT + "/wallet/" + account_id);
    //System.out.println(url.toString());
    HttpURLConnection con = (HttpURLConnection) url.openConnection();
    String userPassword = BITCOIN_NODE_USERNAME + ":" + BITCOIN_NODE_PASSWORD;
    String encoding = Base64.getEncoder().encodeToString(userPassword.getBytes());
    con.setRequestProperty("Authorization", "Basic " + encoding);

    con.setRequestMethod("POST");
    con.setRequestProperty("User-Agent", "Mozilla/1.22 (compatible; MSIE 2.0; Windows 3.1)");
    con.setRequestProperty("Accept-Language", "en-US,en;q=0.5");
    con.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
    con.setDoOutput(true);
    OutputStreamWriter out = new OutputStreamWriter(con.getOutputStream());
    out.write(jsonObject.toString());
    out.close();

    if(con.getResponseCode()==200) {

        BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
        String inputLine;
        StringBuffer response = new StringBuffer();

        while ((inputLine = in.readLine()) != null) {
            response.append(inputLine);
        }
        in.close();
        //System.out.println(response.toString());
        JSONObject response_object = (JSONObject) parser.parse(response.toString());
        //System.out.println(response_object);
    return (String) response_object.get("result");
} else {
        BufferedReader in = new BufferedReader(new InputStreamReader(con.getErrorStream()));
        String inputLine;
        StringBuffer response = new StringBuffer();

        while ((inputLine = in.readLine()) != null) {
            response.append(inputLine);
        }
        in.close();
        //System.out.println(response.toString());
        JSONObject response_object = (JSONObject) parser.parse(response.toString());
    return "failed";
	}
	} catch(Exception e) {
		e.printStackTrace();
	}
    return "failed";
  }

  public String sendMany(String account_id, String address1, String address2, Long sat1, Long sat2) throws IOException, ParseException {
try {
    JSONParser parser = new JSONParser();

    final JSONObject jsonObject = new JSONObject();
    jsonObject.put("jsonrpc", "1.0");
    jsonObject.put("id", "satoshiquest");
    jsonObject.put("method", "sendmany");
    JSONArray params = new JSONArray();
    params.add("");
    final JSONObject addresses = new JSONObject();

    //System.out.println(sat1);
    BigDecimal decimalSat1 = new BigDecimal(sat1 * 0.00000001);
    decimalSat1 = decimalSat1.setScale(8, BigDecimal.ROUND_DOWN);
    //System.out.println(decimalSat1);
    addresses.put(address1,decimalSat1);


    //System.out.println(sat2);
    BigDecimal decimalSat2 = new BigDecimal(sat2 * 0.00000001);
    decimalSat2 = decimalSat2.setScale(8, BigDecimal.ROUND_DOWN);
    //System.out.println(decimalSat2);
    addresses.put(address2,decimalSat2);
    params.add(addresses);

    params.add(FEE_BLOCK_CONF);
    params.add("SatoshiQuest");//the comment :p

    //System.out.println(params);
    jsonObject.put("params", params);
    //System.out.println("Checking blockchain info...");
    URL url = new URL("http://" + BITCOIN_NODE_HOST + ":" + BITCOIN_NODE_PORT + "/wallet/" + account_id);
    //System.out.println(url.toString());
    HttpURLConnection con = (HttpURLConnection) url.openConnection();
    String userPassword = BITCOIN_NODE_USERNAME + ":" + BITCOIN_NODE_PASSWORD;
    String encoding = Base64.getEncoder().encodeToString(userPassword.getBytes());
    con.setRequestProperty("Authorization", "Basic " + encoding);

    con.setRequestMethod("POST");
    con.setRequestProperty("User-Agent", "Mozilla/1.22 (compatible; MSIE 2.0; Windows 3.1)");
    con.setRequestProperty("Accept-Language", "en-US,en;q=0.5");
    con.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
    con.setDoOutput(true);
    OutputStreamWriter out = new OutputStreamWriter(con.getOutputStream());
    out.write(jsonObject.toString());
    out.close();

if(con.getResponseCode()==200) {

        BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
        String inputLine;
        StringBuffer response = new StringBuffer();

        while ((inputLine = in.readLine()) != null) {
            response.append(inputLine);
        }
        in.close();
        //System.out.println(response.toString());
        JSONObject response_object = (JSONObject) parser.parse(response.toString());
        //System.out.println(response_object);

    return (String) response_object.get("result");
 } else {
        BufferedReader in = new BufferedReader(new InputStreamReader(con.getErrorStream()));
        String inputLine;
        StringBuffer response = new StringBuffer();

        while ((inputLine = in.readLine()) != null) {
            response.append(inputLine);
        }
        in.close();
        //System.out.println(response.toString());
        JSONObject response_object = (JSONObject) parser.parse(response.toString());
    return "failed";
	}
	} catch(Exception e) {
		e.printStackTrace();
	}
    return "failed";
  }

  public static void announce(final String message) {
    for (Player player : Bukkit.getOnlinePlayers()) {
      player.sendMessage(message);
    }
  }

    public void updateScoreboard(final Player player) throws ParseException, org.json.simple.parser.ParseException, IOException {
            //User user=new User(player);
 		boolean isPlayersAroundLoot = false;
		for(Player p : Bukkit.getServer().getWorld(SERVERDISPLAY_NAME).getPlayers()) {
		if (isNearLoot(p)) {
			isPlayersAroundLoot = true;
			break;
		}
		if (isPlayersAroundLoot == true) {
			break;
		}
		}

		ScoreboardManager scoreboardManager;
                Scoreboard playSBoard;
                Objective playSBoardObj;
                scoreboardManager = Bukkit.getScoreboardManager();
                playSBoard= scoreboardManager.getNewScoreboard();
                playSBoardObj = playSBoard.registerNewObjective("wallet","dummy");

                playSBoardObj.setDisplaySlot(DisplaySlot.SIDEBAR);

                playSBoardObj.setDisplayName(ChatColor.GREEN + ChatColor.BOLD.toString() + "Satoshi" + ChatColor.GOLD + ChatColor.BOLD.toString() + "Quest");

		if (isPlayersAroundLoot == false) {
		long lootBalance = (long)(getBalance(SERVERDISPLAY_NAME,1) * 0.85);
		double lootAmount =  (double)(exRate * (lootBalance * 0.00000001));        
		DecimalFormat df = new DecimalFormat("#.##");
        	//System.out.print(df.format(lootAmount));
		String whatRound = "Round " + REDIS.get("gameRound");
				if (REDIS.exists("BetaTest")){
					whatRound = "BetaTest Round " + REDIS.get("gameRound");
				}
                Score score7 = playSBoardObj.getScore(ChatColor.GREEN + "" + whatRound);
		score7.setScore(7);

                Score score6 = playSBoardObj.getScore(ChatColor.GREEN + "Lives: " + REDIS.get("LivesLeft" + player.getUniqueId().toString()));
		score6.setScore(6);

		Score score5 = playSBoardObj.getScore(ChatColor.GREEN + "Balance: " + Long.toString(getBalance(player.getUniqueId().toString(),1)));
		score5.setScore(5);

		Score score4 = playSBoardObj.getScore(ChatColor.GREEN + "Loot: " + Long.toString(lootBalance) + "sats");
		score4.setScore(4);

		Score score3 = playSBoardObj.getScore(ChatColor.GREEN + "Loot: $" + df.format(lootAmount));
		score3.setScore(3);

		Score score2 = playSBoardObj.getScore(ChatColor.GREEN + "Loot max spawn: " + LOOT_RADIUS_MAX.toString());
		score2.setScore(2);

		Score score1 = playSBoardObj.getScore(ChatColor.GREEN + "Loot min spawn: " + LOOT_RADIUS_MIN.toString());
		score1.setScore(1);
		} else if (isPlayersAroundLoot == true) {
		Score[] scores = new Score[15];
		int iter = 1;
		for(Player pl : Bukkit.getServer().getWorld(SERVERDISPLAY_NAME).getPlayers()) {
		if ((isNearLoot(pl)) && (iter <= 8)) {
		scores[iter] = playSBoardObj.getScore(ChatColor.RED + " " + " X: " + Integer.toString((int)(pl.getLocation().getX())) + " Z: " + Integer.toString((int)(pl.getLocation().getZ())));
		scores[iter].setScore(iter);
		iter = iter + 1;
		scores[iter] = playSBoardObj.getScore(ChatColor.GREEN + " " + pl.getName());
		scores[iter].setScore(iter);
		iter = iter + 1;
		}

		}
		}
		if (isNearLoot(player)){
		Score scoreIsNear = playSBoardObj.getScore(ChatColor.GREEN + player.getName() + " you are near..");
		scoreIsNear.setScore(0);
		}
      		  player.setScoreboard(playSBoard);
            
       
       
    }
  public void teleportToSpawn(Player player) {
    SatoshiQuest satoshiQuest = this;
    // TODO: open the tps inventory
    //player.sendMessage(ChatColor.GREEN + "Teleporting to spawn...");
    player.setMetadata("teleporting", new FixedMetadataValue(satoshiQuest, true));
    World world = Bukkit.getWorld(SERVERDISPLAY_NAME);

    final Location spawn = world.getSpawnLocation();

    Chunk c = spawn.getChunk();
    if (!c.isLoaded()) {
      c.load();
    }
    satoshiQuest
        .getServer()
        .getScheduler()
        .scheduleSyncDelayedTask(
            satoshiQuest,
            new Runnable() {

              public void run() {
                player.teleport(spawn);
                player.removeMetadata("teleporting", satoshiQuest);
              }
            },
            60L);
  }

  public void createScheduledTimers() {
    BukkitScheduler scheduler = Bukkit.getServer().getScheduler();
    scheduler.scheduleSyncRepeatingTask(
        this,
        new Runnable() {
          @Override
          public void run() {
            // A villager is born
	    //World world = Bukkit.getWorld("world");

          }
        },
        0,
        7200L);



    scheduler.scheduleSyncRepeatingTask(
        this,
        new Runnable() {
          @Override
          public void run() {
            //publish_stats();
try {
		long waitTime15 = 1000 * 60 * 15;
if((exTime15 <= ((new Date().getTime()) - waitTime15))||(exRate == 0)) {
		//announce player location in discord if near
		World world = Bukkit.getServer().getWorld(SERVERDISPLAY_NAME);
		File BaseFolder = new File(Bukkit.getWorlds().get(0).getWorldFolder(), "players");
	            for(Player p : world.getPlayers()){
			if (isNearLoot(p)) {
				Double px=(double)p.getLocation().getX();
		                Double pz=(double)p.getLocation().getZ();
				String toAnnounce = ("@ here: player " + p.getName() + " is near the loot! their last know location was:  X: " + px.intValue() + "   Z: " + pz.intValue());
				if(System.getenv("DISCORD_HOOK_URL")!=null) {
					sendDiscordMessage(toAnnounce);
				}
			}
		}

			exRate =  Double.parseDouble(getExchangeRate("btc"));
			livesRate =  (long)((BUYIN_AMOUNT/(exRate*0.00000001))*0.90);
			adminRate =  (long)((BUYIN_AMOUNT/(exRate*0.00000001))*0.10);
			totalLifeRate = livesRate + adminRate;
			announce("Currently Bitcoin is: $"+ exRate);
		        //System.out.println("Currently Bitcoin is: $"+ exRate);
			announce("1 Life is: "+ totalLifeRate + " " +DENOMINATION_NAME);
			announce("Active updates in the discord: https://discordapp.com/invite/S38Ser6");
		        //System.out.println("1 Life is: "+ totalLifeRate + " " +DENOMINATION_NAME);
			if ((System.getenv("DISCORD_HOOK_URL")!=null)&&(discordWait15 >= 3)) {
			// announce loot in discord
			long lootBalance = (long)(getBalance(SERVERDISPLAY_NAME,1) * 0.85);
			double lootAmount =  (double)(exRate * (lootBalance * 0.00000001));        
			DecimalFormat df = new DecimalFormat("#.##");
        		//System.out.print(df.format(lootAmount));
			String lootAnnounce = ("Current BTC in loot: " +lootBalance + " sats! worth: $"+df.format(lootAmount)+" USD!");
				sendDiscordMessage("1 Life is: "+ totalLifeRate + " sats");
				sendDiscordMessage(lootAnnounce);
				sendDiscordMessage("For more info check out http://AllAboutBTC.com/SatoshiQuest.html");
				discordWait15=0;
			} else {
				discordWait15++;
			}

		//announce("Current time is: "+ (new Date().getTime()));
		

		exTime15 = new Date().getTime();
		} //end waitTime15

} catch (Exception e) {
			e.printStackTrace();
		}
          }
        },
        0,
        1000L);
  }

  public void publish_stats() {
    try {
      Long balance = getBalance(SERVERDISPLAY_NAME,1); //error here
      REDIS.set("loot:pool", Long.toString(balance));
      if (System.getenv("ELASTICSEARCH_ENDPOINT") != null) {
        JSONParser parser = new JSONParser();

        final JSONObject jsonObject = new JSONObject();

        jsonObject.put("balance", balance);
        jsonObject.put("time", new Date().getTime());
        URL url = new URL(System.getenv("ELASTICSEARCH_ENDPOINT") + "-stats/_doc");
        //System.out.println(url.toString());
        HttpURLConnection con = (HttpURLConnection) url.openConnection();

        con.setRequestMethod("POST");
        con.setRequestProperty("Content-Type", "application/json; charset=UTF-8");

        con.setDoOutput(true);
        OutputStreamWriter out = new OutputStreamWriter(con.getOutputStream());
        out.write(jsonObject.toString());
        out.close();

    if(con.getResponseCode()==200) {

        BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
        String inputLine;
        StringBuffer response = new StringBuffer();

        while ((inputLine = in.readLine()) != null) {
            response.append(inputLine);
        }
        in.close();
        //System.out.println(response.toString());
        JSONObject response_object = (JSONObject) parser.parse(response.toString());
        //System.out.println(response_object);
	}
      }
    } catch (Exception e) {
      e.printStackTrace();
    }
  }


  public void log(String msg) {
    Bukkit.getLogger().info(msg);
  }

  public static boolean canBuild(Player player) {
	Location spawn = Bukkit.getServer().getWorld(SERVERDISPLAY_NAME).getSpawnLocation();
		double spawnx = spawn.getX();
		double spawnz = spawn.getZ();
		double playerx=(double)player.getLocation().getX();
                double playerz=(double)player.getLocation().getZ();
	World world = Bukkit.getServer().getWorld(SERVERDISPLAY_NAME);
	if(player.getWorld()==world){
	if (((playerx<=spawnx+SPAWN_PROTECT_RADIUS+1)&&(playerx>=spawnx-SPAWN_PROTECT_RADIUS-1)) && ((playerz<=spawnz+SPAWN_PROTECT_RADIUS+1)&&(playerz>=spawnz-SPAWN_PROTECT_RADIUS-1))) {return false;}
	}

		//System.out.println("You may not build at spawn.");  
		//player.sendMessage(ChatColor.RED + "you cant build at spawn.");
               return true;//not

  }

  public boolean isNearLoot(Player player) {
	String toAnnounce = "";
	Location spawn = Bukkit.getServer().getWorld(SERVERDISPLAY_NAME).getSpawnLocation();
	double lootX = Double.parseDouble(REDIS.get("lootSpawnX"));
	double lootZ = Double.parseDouble(REDIS.get("lootSpawnZ"));
		Double playerx=(double)player.getLocation().getX();
                Double playerz=(double)player.getLocation().getZ();
	double announceRadius = (double)Long.parseLong(REDIS.get("LOOT_ANNOUNCE_RADIUS"));
	World world = Bukkit.getServer().getWorld(SERVERDISPLAY_NAME);
	if(player.getWorld()==world){
	if ((((playerx<lootX+announceRadius)&&(playerx>lootX-announceRadius))) && (((playerz<lootZ+announceRadius)&&(playerz>lootZ-announceRadius)))) {
		if (!REDIS.exists("LootAnnounced" +player.getUniqueId().toString())) {
		REDIS.set("LootAnnounced" +player.getUniqueId().toString(), "true");

		toAnnounce = ("@ here: player " + player.getName() + " is near the loot! their last know location was:  X: " + playerx.intValue() + " Z:" + playerz.intValue());
		announce(toAnnounce);
if(System.getenv("DISCORD_HOOK_URL")!=null) {
			sendDiscordMessage(toAnnounce);
		}
		}
		//System.out.println("You are near...");
		//String toAnnounce = (player.getName() + " - X:" + playerx.intValue() + " Z:" + playerz.intValue());
		//announce(toAnnounce);
		//setLootBlocks();
		return true;
		}
	}
               return false;//not

  }

  public void didFindLoot(Player player) {
	World getworld = Bukkit.getServer().getWorld(SERVERDISPLAY_NAME);
	if(player.getWorld()==getworld){
	Location spawn = Bukkit.getServer().getWorld(SERVERDISPLAY_NAME).getSpawnLocation();
	double lootX = Double.parseDouble(REDIS.get("lootSpawnX"));
	double lootZ = Double.parseDouble(REDIS.get("lootSpawnZ"));
	double lootY = Double.parseDouble(REDIS.get("lootSpawnY"));
		double playerx=(double)player.getLocation().getX();
                double playerz=(double)player.getLocation().getZ();
                double playery=(double)player.getLocation().getY();


	if ((playerx<lootX+0.8)&&(playerx>lootX-0.8) && (playerz<lootZ+0.8)&&(playerz>lootZ-0.8) && (playery<lootY+2)&&(playery>lootY-2) && (!REDIS.exists("winner"))) {
		System.out.println(player.getDisplayName() + " won!");
		REDIS.set("winner",player.getDisplayName());
		announce(player.getName() + " WON!");
		REDIS.set("pushloot","true");
		//sendloot to winner
		long sendLoot = 0L;
		String result = "failed";
		//result = "test";
		try {
		if (getBalance(SERVERDISPLAY_NAME,1) > 0) {
			sendLoot = (long)((double)getBalance(SERVERDISPLAY_NAME,1) * 0.85);
			Long sendback = (long)((double)getBalance(SERVERDISPLAY_NAME,1) * 0.025);
		if (REDIS.exists("ExternalAddress" +player.getUniqueId().toString())) {
		result = sendMany(SERVERDISPLAY_NAME, REDIS.get("ExternalAddress" +player.getUniqueId().toString()), REDIS.get("nodeAddress"+SERVERDISPLAY_NAME), sendLoot, sendback);
		} 
		if (result == "failed") {
		player.sendMessage(ChatColor.YELLOW + "External OnWin address not set, or failed, trying in-game wallet.");
		result = sendMany(SERVERDISPLAY_NAME, REDIS.get("nodeAddress"+player.getUniqueId().toString()), REDIS.get("nodeAddress"+SERVERDISPLAY_NAME), sendLoot, sendback);
		}
			System.out.println("won " + sendLoot + " LOOT! txid: " +result);
		}
		} catch (Exception exs) {
			System.out.println(exs);
		}
		//reset? DONE :D
	if (result == "failed") {
		sendLoot = 0L;
		announce("Loot failed transfer possibly due to high fee");
	}
	DateFormat dateFormat = new SimpleDateFormat("MM/dd/yyyy");
	Date date = new Date();
	System.out.println(dateFormat.format(date) + " " + player.getName() + " " + sendLoot);

  	leaderBoardList = REDIS.keys("LeaderBoard *");

		double amtUSD = (double)(exRate * (sendLoot * 0.00000001));
if (result != "failed"){
	int iter=1;
		for (String templeaderBoardList : leaderBoardList) {
		announce(iter+") "+ REDIS.get(templeaderBoardList));
		iter++;
		}
		DecimalFormat df = new DecimalFormat("#.##");
        	System.out.print(df.format(amtUSD));
		if (REDIS.exists("BetaTest")){
REDIS.set("LeaderBoard " + iter, "BetaTest Round " + REDIS.get("gameRound") + " " +dateFormat.format(date) + " " + player.getName() + " $" + df.format(amtUSD) + " Sats " + sendLoot);
		announce("NEW! " +iter+") "+ REDIS.get("LeaderBoard " + iter));
		if(System.getenv("DISCORD_HOOK_URL")!=null) {
			sendDiscordMessage(dateFormat.format(date) + " " + player.getName() + " WON " + "BetaTest Round " + REDIS.get("gameRound") + " with " + sendLoot + " SATS worth $" + df.format(amtUSD));
		}
		}//betatest
		if (!REDIS.exists("BetaTest")){
REDIS.set("LeaderBoard " + iter, "Round " + REDIS.get("gameRound") + " " +dateFormat.format(date) + " " + player.getName() + " $" + df.format(amtUSD) + " Sats " + sendLoot);
		announce("NEW! " +iter+") "+ REDIS.get("LeaderBoard " + iter));
		if(System.getenv("DISCORD_HOOK_URL")!=null) {
			sendDiscordMessage(dateFormat.format(date) + " " + player.getName() + " WON " + "Round " + REDIS.get("gameRound") + " with " + sendLoot + " SATS worth $" + df.format(amtUSD));
		}
		}//betatest
	}
	REDIS.set("gameRound",Integer.toString(Integer.parseInt(REDIS.get("gameRound"))+1));	
    World world = Bukkit.getServer().getWorld(SERVERDISPLAY_NAME);
 if(!world.equals(null)) {
File BaseFolder = new File(Bukkit.getWorlds().get(0).getWorldFolder(), "players");
	    for(OfflinePlayer offlinePlayer : Bukkit.getOfflinePlayers()) {
		if (result != "failed"){
		int tempLivesOfflinePlayer = Integer.parseInt(REDIS.get("LivesLeft" +offlinePlayer.getUniqueId().toString()));
		if (tempLivesOfflinePlayer >= 1) {
		REDIS.set("LivesLeft" +offlinePlayer.getUniqueId().toString(), Integer.toString(tempLivesOfflinePlayer-1));
		}
		if (REDIS.exists("LootAnnounced" +offlinePlayer.getUniqueId().toString())) {
		REDIS.del("LootAnnounced" +offlinePlayer.getUniqueId().toString());
		}
		}
		REDIS.set("ClearInv" +offlinePlayer.getUniqueId().toString(), "true");
		}
	   for(Player p : Bukkit.getServer().getWorld(SERVERDISPLAY_NAME).getPlayers()) {
                p.teleport(Bukkit.getWorlds().get(0).getSpawnLocation());
		REDIS.del("ClearInv" +p.getUniqueId().toString());
		PlayerInventory pli1= p.getInventory();
		pli1.clear(); //delete player world datas
		pli1.setArmorContents(new ItemStack[4]);
		Inventory pe1  = p.getEnderChest();
		pe1.clear();
		p.setLevel(0);
		p.setExp(0);
		p.setExhaustion(0);
		p.setFoodLevel(20);
		p.setHealth(p.getAttribute(Attribute.GENERIC_MAX_HEALTH).getValue());
		
		Iterator<Advancement> it = Bukkit.getServer().advancementIterator();
		// gets all 'registered' advancements on the server.
		while (it.hasNext()) {
		// loops through these.
		Advancement a = it.next();
	        AdvancementProgress progress = p.getAdvancementProgress(a);
		if (progress.isDone() == true) {
		         for(String c: a.getCriteria()) {
	 p.getAdvancementProgress(a).revokeCriteria(c);
			}
		}

            }
        



		try {
		updateScoreboard(p);
		} catch (Exception excep) {
			System.out.println(excep);
		}
		}
            Bukkit.getServer().unloadWorld(world, false);
Bukkit.getServer().unloadWorld(Bukkit.getServer().getWorld(SERVERDISPLAY_NAME+"_the_end"), false);
Bukkit.getServer().unloadWorld(Bukkit.getServer().getWorld(SERVERDISPLAY_NAME+"_nether"), false);
            //System.out.println(world.getName() + " unloaded!");
        }
		deleteLootWorlds();

		REDIS.del("lootSpawnY");
		REDIS.del("spawnCreated");
		onEnable();
		while(Bukkit.getServer().getWorld(SERVERDISPLAY_NAME+"_the_end") == null){
		
		}
		REDIS.del("winner");
//Bukkit.getServer().dispatchCommand(Bukkit.getConsoleSender(), "restart");


	} 
	}
  }

public void deleteLootWorlds() {

	File dir = new File(getServer().getWorldContainer().getAbsolutePath() + "/" + SERVERDISPLAY_NAME);
	boolean deld = deleteWorld(dir);
		if (deld == true) {
			System.out.println(SERVERDISPLAY_NAME + " world deleted and ready for reset!");
		}

File dir2 = new File(getServer().getWorldContainer().getAbsolutePath() + "/" + SERVERDISPLAY_NAME+"_nether");
	boolean deld2 = deleteWorld(dir2);
		if (deld2 == true) {
			System.out.println(SERVERDISPLAY_NAME + " nether deleted and ready for reset!");
		}

File dir3 = new File(getServer().getWorldContainer().getAbsolutePath() + "/" + SERVERDISPLAY_NAME+"_the_end");
	boolean deld3 = deleteWorld(dir3);
		if (deld3 == true) {
			System.out.println(SERVERDISPLAY_NAME + " end deleted and ready for reset!");
		}
	}


public void teleportToLootSpawn(Player player) {
    SatoshiQuest satoshiQuest = this;
    // TODO: open the tps inventory
    //player.sendMessage(ChatColor.GREEN + "Teleporting to spawn...");
    player.setMetadata("teleporting", new FixedMetadataValue(satoshiQuest, true));
    World world = Bukkit.getWorld(SERVERDISPLAY_NAME);

    final Location spawn = world.getSpawnLocation();

    Chunk c = spawn.getChunk();
    if (!c.isLoaded()) {
      c.load();
    }
    satoshiQuest
        .getServer()
        .getScheduler()
        .scheduleSyncDelayedTask(
            satoshiQuest,
            new Runnable() {

              public void run() {
                player.teleport(spawn);
                player.removeMetadata("teleporting", satoshiQuest);
              }
            },
            60L);
  }


  private static boolean deleteWorld(File path) {
        if(path.exists()) {
            File files[] = path.listFiles();
            for(int i=0; i<files.length; i++) {
                if(files[i].isDirectory()) {
                    deleteWorld(files[i]);
                } else {
                    files[i].delete();
                }
            }
        }
        return(path.delete());
    }

  public boolean canLeaveSpawn() {
	try {
	if(REDIS.get("ModFlag").equals("true")) {
		return false;
	} else if(REDIS.get("ModFlag").equals("false")) {
		return true;
	}
	} catch (Exception e) {
      e.printStackTrace();
    }
	return false;
  }

  private void addSpawnBlocks() {
	Location spawn = Bukkit.getServer().getWorld(SERVERDISPLAY_NAME).getSpawnLocation();
	Block spawnBlock = spawn.getBlock();
	double spawnx = spawn.getX();
	double spawnz = spawn.getZ();
	double spawny = spawn.getY();
	double spawnRadius = SPAWN_PROTECT_RADIUS;
	double posX = spawnx + (spawnRadius);
	double negX = spawnx - (spawnRadius);
	double posZ = spawnz + (spawnRadius);
	double negZ = spawnz - (spawnRadius);
	double lowPoint = spawny;
	double highPoint = spawny;

	for(double x = negX; x <= posX; x++) {
			for(double z = negZ; z <= posZ; z++) {
	                double tempSpawnBlock = spawnBlock.getWorld().getBlockAt((int)x,((int)spawnBlock.getWorld().getHighestBlockAt((int)x, (int)z).getY()-1), (int)z).getY();
			if (tempSpawnBlock < lowPoint) {
				lowPoint = tempSpawnBlock;
			} else if (tempSpawnBlock > highPoint) {
				highPoint = tempSpawnBlock;
			}
			}

	}
		for(double x = negX; x <= posX; x++) {
			for(double z = negZ; z <= posZ; z++) {
				for(double y = lowPoint; y <= highPoint-1; y++) {
	                Block tempblock = spawnBlock.getWorld().getBlockAt((int)x,(int)y, (int)z);
			tempblock.setType(Material.EMERALD_BLOCK);
				}
			}
		}
		double midX = negX + posX;
		double midZ = negZ + posZ;
		for(double y = lowPoint; y <= highPoint-1; y++) {
	                Block tempblock = spawnBlock.getWorld().getBlockAt((int)negX,(int)y, (int)negZ);
			tempblock.setType(Material.WATER);
	                tempblock = spawnBlock.getWorld().getBlockAt((int)posX,(int)y, (int)posZ);
			tempblock.setType(Material.WATER);
	                tempblock = spawnBlock.getWorld().getBlockAt((int)negX,(int)y, (int)posZ);
			tempblock.setType(Material.WATER);
	                tempblock = spawnBlock.getWorld().getBlockAt((int)posX,(int)y, (int)negZ);
			tempblock.setType(Material.WATER);
				}


	Block tempSetSpawnBlock = spawnBlock.getWorld().getBlockAt((int)spawnx,((int)spawnBlock.getWorld().getHighestBlockAt((int)spawnx, (int)spawnz).getY()-1), (int)spawnz);
	Location setSpawnBlock = tempSetSpawnBlock.getLocation();
Bukkit.getServer().getWorld(SERVERDISPLAY_NAME).setSpawnLocation(setSpawnBlock.getBlockX(), setSpawnBlock.getBlockY() + 1, setSpawnBlock.getBlockZ());
   }

   private static void setLootBlocks() {
	Location spawn = Bukkit.getServer().getWorld(SERVERDISPLAY_NAME).getSpawnLocation();
	Block lootBlock = spawn.getBlock();
	double lootX = Double.parseDouble(REDIS.get("lootSpawnX"));
	double lootZ = Double.parseDouble(REDIS.get("lootSpawnZ"));
	
	double lootY = Bukkit.getServer().getWorld(SERVERDISPLAY_NAME).getHighestBlockAt((int)lootX,(int)lootZ).getY()-1;
	if (!REDIS.exists("lootSpawnY")) {
		Block blocky = Bukkit.getServer().getWorld(SERVERDISPLAY_NAME).getHighestBlockAt((int)lootX,(int)lootZ);
		lootY = (double)blocky.getY()-1;
		REDIS.set("lootSpawnY",Double.toString(lootY));
	}
	lootY = Double.parseDouble(REDIS.get("lootSpawnY"));
			Block tempblock = Bukkit.getServer().getWorld(SERVERDISPLAY_NAME).getBlockAt((int)lootX,(int)lootY,(int)lootZ);

	                tempblock = lootBlock.getWorld().getBlockAt((int)lootX, (int)lootY, (int)lootZ);
			tempblock.setType(Material.GOLD_BLOCK);//loot spawn block

			//loot walls
	                tempblock = lootBlock.getWorld().getBlockAt((int)lootX+1, (int)lootY, (int)lootZ+1);
			tempblock.setType(Material.GOLD_BLOCK);
	                tempblock = lootBlock.getWorld().getBlockAt((int)lootX-1, (int)lootY, (int)lootZ-1);
			tempblock.setType(Material.GOLD_BLOCK);
	                tempblock = lootBlock.getWorld().getBlockAt((int)lootX-1, (int)lootY, (int)lootZ+1);
			tempblock.setType(Material.GOLD_BLOCK);
	                tempblock = lootBlock.getWorld().getBlockAt((int)lootX+1, (int)lootY, (int)lootZ-1);
			tempblock.setType(Material.GOLD_BLOCK);

	                tempblock = lootBlock.getWorld().getBlockAt((int)lootX+1, (int)lootY+1, (int)lootZ+1);
			tempblock.setType(Material.GOLD_BLOCK);
	                tempblock = lootBlock.getWorld().getBlockAt((int)lootX-1, (int)lootY+1, (int)lootZ-1);
			tempblock.setType(Material.GOLD_BLOCK);
	                tempblock = lootBlock.getWorld().getBlockAt((int)lootX-1, (int)lootY+1, (int)lootZ+1);
			tempblock.setType(Material.GOLD_BLOCK);
	                tempblock = lootBlock.getWorld().getBlockAt((int)lootX+1, (int)lootY+1, (int)lootZ-1);
			tempblock.setType(Material.GOLD_BLOCK);

	                tempblock = lootBlock.getWorld().getBlockAt((int)lootX+1, (int)lootY+2, (int)lootZ+1);
			tempblock.setType(Material.GOLD_BLOCK);
	                tempblock = lootBlock.getWorld().getBlockAt((int)lootX-1, (int)lootY+2, (int)lootZ-1);
			tempblock.setType(Material.GOLD_BLOCK);
	                tempblock = lootBlock.getWorld().getBlockAt((int)lootX-1, (int)lootY+2, (int)lootZ+1);
			tempblock.setType(Material.GOLD_BLOCK);
	                tempblock = lootBlock.getWorld().getBlockAt((int)lootX+1, (int)lootY+2, (int)lootZ-1);
			tempblock.setType(Material.GOLD_BLOCK);

	                tempblock = lootBlock.getWorld().getBlockAt((int)lootX+1, (int)lootY+3, (int)lootZ+1);
			tempblock.setType(Material.GOLD_BLOCK);
	                tempblock = lootBlock.getWorld().getBlockAt((int)lootX-1, (int)lootY+3, (int)lootZ-1);
			tempblock.setType(Material.GOLD_BLOCK);
	                tempblock = lootBlock.getWorld().getBlockAt((int)lootX-1, (int)lootY+3, (int)lootZ+1);
			tempblock.setType(Material.GOLD_BLOCK);
	                tempblock = lootBlock.getWorld().getBlockAt((int)lootX+1, (int)lootY+3, (int)lootZ-1);
			tempblock.setType(Material.GOLD_BLOCK);

	                tempblock = lootBlock.getWorld().getBlockAt((int)lootX+1, (int)lootY+4, (int)lootZ+1);
			tempblock.setType(Material.GOLD_BLOCK);
	                tempblock = lootBlock.getWorld().getBlockAt((int)lootX-1, (int)lootY+4, (int)lootZ-1);
			tempblock.setType(Material.GOLD_BLOCK);
	                tempblock = lootBlock.getWorld().getBlockAt((int)lootX-1, (int)lootY+4, (int)lootZ+1);
			tempblock.setType(Material.GOLD_BLOCK);
	                tempblock = lootBlock.getWorld().getBlockAt((int)lootX+1, (int)lootY+4, (int)lootZ-1);
			tempblock.setType(Material.GOLD_BLOCK);
			
	                tempblock = lootBlock.getWorld().getBlockAt((int)lootX, (int)lootY+4, (int)lootZ);
			tempblock.setType(Material.GOLD_BLOCK);//loot cap block

   }
  public boolean isModerator(Player player) {
    if (REDIS.sismember("moderators", player.getUniqueId().toString())) {
      return true;
    } else if (ADMIN_UUID != null
        && player.getUniqueId().toString().equals(ADMIN_UUID.toString())) {
      return true;
    } else {
      return false;
    }
  }

  public void sendWalletInfo(final Player player) {
    if (BITCOIN_NODE_HOST != null) {
      // TODO: Rewrite send wallet info
    }
    try {
      Long balance = getBalance(player.getUniqueId().toString(),1);

      player.sendMessage("Address: " + getAccountAddress(player.getUniqueId().toString()));
      player.sendMessage("Balance: " + balance);
      player.sendMessage(
          "URL: " + ChatColor.BLUE + ChatColor.UNDERLINE + ChatColor.BOLD + ADDRESS_URL + getAccountAddress(player.getUniqueId().toString()));
      player.sendMessage("-----------");

    } catch (Exception e) {
      e.printStackTrace();
      player.sendMessage(ChatColor.RED + "Error reading wallet. Please try again later.");
    }
  };

  @Override
  public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
    // we don't allow server commands (yet?)
    if (sender instanceof Player) {
      final Player player = (Player) sender;
      // PLAYER COMMANDS
      for (Map.Entry<String, CommandAction> entry : commands.entrySet()) {
        if (cmd.getName().equalsIgnoreCase(entry.getKey())) {
          entry.getValue().run(sender, cmd, label, args, player);
        }
      }

      // MODERATOR COMMANDS
      for (Map.Entry<String, CommandAction> entry : modCommands.entrySet()) {
        if (cmd.getName().equalsIgnoreCase(entry.getKey())) {
          if (isModerator(player)) {
            entry.getValue().run(sender, cmd, label, args, player);
          } else {
            sender.sendMessage(
                ChatColor.DARK_RED + "You don't have enough permissions to execute this command!");
          }
        }
      }
    }
    return true;
  }


  public String urlenEncode(String en) throws UnsupportedEncodingException {
    return URLEncoder.encode(en, "UTF-8");
  }

  public String urlenDecode(String en) throws UnsupportedEncodingException {
    return URLDecoder.decode(en, "UTF-8");
  }
  public void sendDiscordMessage(String content) {
    if(System.getenv("DISCORD_HOOK_URL")!=null) {
      //System.out.println("[discord] "+content);
      try {
          String json = "{\"content\":\""+content+"\"}";

          JSONParser parser = new JSONParser();

          final JSONObject jsonObject = new JSONObject();
          jsonObject.put("content", content);
          CookieHandler.setDefault(new CookieManager());

          URL url = new URL(System.getenv("DISCORD_HOOK_URL"));
          HttpsURLConnection con = null;

          System.setProperty("http.agent", "");

          con = (HttpsURLConnection) url.openConnection();

          con.setRequestMethod("POST");
          con.setRequestProperty("Content-Type", "application/json");
          con.setRequestProperty("Cookie", "satoshiquest=true");
          con.setRequestProperty("User-Agent", "Mozilla/5.0 (Macintosh; U; Intel Mac OS X 10.4; en-US; rv:1.9.2.2) Gecko/20100316 Firefox/3.6.2");

          con.setDoOutput(true);
          OutputStreamWriter out = new OutputStreamWriter(con.getOutputStream());
          out.write(json);
          out.close();
	if(con.getResponseCode()==200) {
            BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
            String inputLine;
            StringBuffer response = new StringBuffer();

            while ((inputLine = in.readLine()) != null) {
              response.append(inputLine);
            }
            in.close();
            //System.out.println(response.toString());
            //return true;
          } else {
            //return false;
          }
          

      } catch (Exception e) {
          e.printStackTrace();
          //return false;
      }
    }
    //return false;

  }

/*
{
  "last_message_id": "3343820033257021450",
  "type": 1,
  "id": "319674150115610528",
  "recipients": [
    {
      "username": "test",
      "discriminator": "9999",
      "id": "82198898841029460",
      "avatar": "33ecab261d4681afa4d85a04691c4a01"
    }
  ]
}

  public void sendDiscordDM(String content) {
    if(System.getenv("DISCORD_HOOK_URL")!=null) {
      System.out.println("[discord] "+content);
      try {
          String json = "{\"content\":\""+content+"\"}";

          JSONParser parser = new JSONParser();

          final JSONObject jsonObject = new JSONObject();
          jsonObject.put("content", content);
          CookieHandler.setDefault(new CookieManager());

          URL url = new URL(System.getenv("DISCORD_HOOK_URL"));
          HttpsURLConnection con = null;

          System.setProperty("http.agent", "");

          con = (HttpsURLConnection) url.openConnection();

          con.setRequestMethod("POST");
          con.setRequestProperty("Content-Type", "application/json");
          con.setRequestProperty("Cookie", "satoshiquest=true");
          con.setRequestProperty("User-Agent", "Mozilla/5.0 (Macintosh; U; Intel Mac OS X 10.4; en-US; rv:1.9.2.2) Gecko/20100316 Firefox/3.6.2");

          con.setDoOutput(true);
          OutputStreamWriter out = new OutputStreamWriter(con.getOutputStream());
          out.write(json);
          out.close();
	int responseCode = con.getResponseCode();
          if(responseCode==200) {
            BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
            String inputLine;
            StringBuffer response = new StringBuffer();

            while ((inputLine = in.readLine()) != null) {
              response.append(inputLine);
            }
            in.close();
            System.out.println(response.toString());
            //return true;
          } else {
            //return false;
          }
          

      } catch (Exception e) {
          e.printStackTrace();
          //return false;
      }
    }
    //return false;

  }
*/
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
	public static boolean isStringDouble(String s)
{
    try
    {
        Double.parseDouble(s);
        return true;
    } catch (NumberFormatException ex)
    {
        return false;
    }
}
  public void crashtest() {
    this.setEnabled(false);
  }


    public String getExchangeRate(String crypto)
	{
	String price="10000.00000000";
	String rate = exRate.toString();
	 try {
            
                URL url=new URL("https://api.cryptonator.com/api/ticker/"+crypto+"-usd");
                
                //System.out.println(url.toString());
                HttpsURLConnection con = (HttpsURLConnection) url.openConnection();
                con.setRequestMethod("GET");
                con.setRequestProperty("User-Agent", "Mozilla/1.22 (compatible; MSIE 2.0; Windows 3.1)");
                con.setRequestProperty("Accept-Language", "en-US,en;q=0.5");
	StringBuffer response = new StringBuffer();
    if(con.getResponseCode()==200) {
            BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
            String inputLine;
            response = new StringBuffer();

            while ((inputLine = in.readLine()) != null) {
              response.append(inputLine);
            }
            in.close();
            //System.out.println(response.toString());

          } else {
            BufferedReader in = new BufferedReader(new InputStreamReader(con.getErrorStream()));
            String inputLine;
            response = new StringBuffer();

            while ((inputLine = in.readLine()) != null) {
              response.append(inputLine);
            }
            in.close();
            //System.out.println(response.toString());
		}
		JSONParser parser = new JSONParser();
            	final JSONObject jsonobj,jsonobj2;

                jsonobj = (JSONObject) parser.parse(response.toString());
                jsonobj2 = (JSONObject) parser.parse(jsonobj.get("ticker").toString());
		//double val=Double.parseDouble(jsonobj2.get("price").toString());
		

		price=jsonobj2.get("price").toString();
                //System.out.println(crypto + "price: "+price);


        } catch (Exception e) {
            System.out.println("[PRICE] problem updating price for "+crypto);
                e.printStackTrace();
            // wallet might be new and it's not listed on the blockchain yet
        }
	if (isStringDouble(price)) {
		return price;
	} else {
		return rate;
	}
    }


}

