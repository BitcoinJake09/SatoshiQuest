//bitcoinjake09 11/9/2019 - a bitcoin tressure hunt in minecraft - satoshiquest
package com.satoshiquest.satoshiquest;

import com.satoshiquest.satoshiquest.commands.*;
import com.satoshiquest.satoshiquest.events.*;
import com.google.gson.JsonObject;
import java.io.*;
import java.net.*;
import java.sql.DriverManager;
import java.text.ParseException;
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

  public static final Long SPAWN_PROTECT_RADIUS =
      System.getenv("SPAWN_PROTECT_RADIUS") != null ? Long.parseLong(System.getenv("SPAWN_PROTECT_RADIUS")) : 14;

  public static final Long LOOT_ANNOUNCE_RADIUS =
      System.getenv("LOOT_ANNOUNCE_RADIUS") != null ? Long.parseLong(System.getenv("LOOT_ANNOUNCE_RADIUS")) : 20;

  public static final Long LOOT_RADIUS_MIN =
      System.getenv("LOOT_RADIUS_MIN") != null ? Long.parseLong(System.getenv("LOOT_RADIUS_MIN")) : 1000;

  public static final Long LOOT_RADIUS_MAX =
      System.getenv("LOOT_RADIUS_MAX") != null ? Long.parseLong(System.getenv("LOOT_RADIUS_MAX")) : 10000;

  public static int rand(int min, int max) {
    return min + (int) (Math.random() * ((max - min) + 1));
  }
  public Long LootSpawnX = new Long(rand(LOOT_RADIUS_MIN.intValue(),LOOT_RADIUS_MAX.intValue()));
  public Long LootSpawnZ = new Long(rand(LOOT_RADIUS_MIN.intValue(),LOOT_RADIUS_MAX.intValue()));

  public NodeWallet wallet = null;
  public Player last_loot_player;

  public Long wallet_balance_cache = 0L;
  public Long exTime = new Date().getTime();
  public Double exRate = 0.0;
  public Long livesRate = 0L;
  public Long adminRate = 0L;
  public Long totalLifeRate = 0L;

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
      getServer().getPluginManager().registerEvents(new ChatEvents(this), this);
      getServer().getPluginManager().registerEvents(new BlockEvents(this), this);
      getServer().getPluginManager().registerEvents(new EntityEvents(this), this);
      getServer().getPluginManager().registerEvents(new ServerEvents(this), this);

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

getWalletInfo(SERVERDISPLAY_NAME);
	if (!REDIS.exists("nodeWallet"+SERVERDISPLAY_NAME)) {
	if (loadWallet(SERVERDISPLAY_NAME)!=null) {
		try {
			wallet = loadWallet(REDIS.get("nodeWallet"+SERVERDISPLAY_NAME));
		        System.out.println("[world wallet] trying to load node wallet");
		} catch (NullPointerException npe) {
			npe.printStackTrace();
			System.out.println("[world wallet] wallet not found, attempting to create.");
		}
	} else
	{
	        wallet = generateNewWallet(SERVERDISPLAY_NAME);
        	System.out.println("[world wallet] generated new wallet");
		REDIS.set("nodeWallet"+SERVERDISPLAY_NAME,SERVERDISPLAY_NAME);
	} 
	} else { 
	wallet = loadWallet(REDIS.get("nodeWallet"+SERVERDISPLAY_NAME));
	}//nodewallet
	if (!REDIS.exists("nodeAddress"+SERVERDISPLAY_NAME)) {
	try {
		if (wallet.getAccountAddress()!=null) {
			REDIS.set("nodeAddress"+SERVERDISPLAY_NAME,wallet.getAccountAddress());
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

  LootSpawnX = new Long(rand(LOOT_RADIUS_MIN.intValue(),LOOT_RADIUS_MAX.intValue()));
  LootSpawnZ = new Long(rand(LOOT_RADIUS_MIN.intValue(),LOOT_RADIUS_MAX.intValue()));

	int posNeg = rand(1,2);
	if (posNeg == 1) {
		LootSpawnX = LootSpawnX * -1;
	} else if (posNeg == 2) {
		LootSpawnX = LootSpawnX * 1;
	}
	posNeg = rand(1,2);
	if (posNeg == 1) {
		LootSpawnZ = LootSpawnZ * -1;
	} else if (posNeg == 2) {
		LootSpawnZ = LootSpawnZ * 1;
	}
	long sX = (long)Bukkit.getWorld(SERVERDISPLAY_NAME).getSpawnLocation().getX();
	long sZ = (long)Bukkit.getWorld(SERVERDISPLAY_NAME).getSpawnLocation().getZ();
	
	LootSpawnX = sX + LootSpawnX;
	LootSpawnZ = sZ + LootSpawnZ;



	if (!REDIS.exists("lootSpawnY")) {
	REDIS.set("lootSpawnX",LootSpawnX.toString());
	REDIS.set("lootSpawnZ",LootSpawnZ.toString());
	}
  System.out.println("Loot X,Z: " + REDIS.get("lootSpawnX") + " " + REDIS.get("lootSpawnZ"));
	setLootBlocks();
      // creates scheduled timers (update balances, etc)
      createScheduledTimers();
      commands = new HashMap<String, CommandAction>();
      commands.put("wallet", new WalletCommand(this));
      commands.put("tip", new TipCommand(this));
      commands.put("send", new SendCommand(this));
      commands.put("lives", new LivesCommand(this));
      modCommands = new HashMap<String, CommandAction>();
      modCommands.put("crashTest", new CrashtestCommand(this));
      modCommands.put("mod", new ModCommand());
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
    System.out.println("Creating wallet: " + account_id);
    System.out.println(params);
    jsonObject.put("params", params);
    System.out.println("Checking blockchain info...");
    URL url = new URL("http://" + SatoshiQuest.BITCOIN_NODE_HOST + ":" + SatoshiQuest.BITCOIN_NODE_PORT);
    System.out.println(url.toString());
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
    System.out.println(jsonObject.toString());
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
        System.out.println(response.toString());
        JSONObject response_object = (JSONObject) parser.parse(response.toString());
        System.out.println(response_object);

        return new NodeWallet(account_id);
    } else {
        BufferedReader in = new BufferedReader(new InputStreamReader(con.getErrorStream()));
        String inputLine;
        StringBuffer response = new StringBuffer();

        while ((inputLine = in.readLine()) != null) {
            response.append(inputLine);
        }
        in.close();
        System.out.println(response.toString());
        JSONObject response_object = (JSONObject) parser.parse(response.toString());
        System.out.println(response_object);
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
        System.out.println(params);
        jsonObject.put("params", params);
        URL url = new URL("http://" + SatoshiQuest.BITCOIN_NODE_HOST + ":" + SatoshiQuest.BITCOIN_NODE_PORT+ "/wallet/");
        System.out.println(url.toString());
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
        System.out.println(jsonObject.toString());
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
            System.out.println(response.toString());
            JSONObject response_object = (JSONObject) parser.parse(response.toString());
            System.out.println(response_object);

        } else {
            BufferedReader in = new BufferedReader(new InputStreamReader(con.getErrorStream()));
            String inputLine;
            StringBuffer response = new StringBuffer();

            while ((inputLine = in.readLine()) != null) {
                response.append(inputLine);
            }
            in.close();
            System.out.println(response.toString());
            JSONObject response_object = (JSONObject) parser.parse(response.toString());
            System.out.println(response_object);
        }


    }

    public static final void getWalletInfo(String account_id)
            throws IOException, org.json.simple.parser.ParseException {
        JSONParser parser = new JSONParser();

        final JSONObject jsonObject = new JSONObject();
        jsonObject.put("jsonrpc", "1.0");
        jsonObject.put("id", "satoshiquest");
        jsonObject.put("method", "getwalletinfo");
        JSONArray params = new JSONArray();
        System.out.println(params);
        jsonObject.put("params", params);
        URL url = new URL("http://" + SatoshiQuest.BITCOIN_NODE_HOST + ":" + SatoshiQuest.BITCOIN_NODE_PORT + "/wallet/" + account_id);
        System.out.println(url.toString());
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
        System.out.println(jsonObject.toString());
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
            System.out.println(response.toString());
            JSONObject response_object = (JSONObject) parser.parse(response.toString());
            System.out.println(response_object);

        } else {
            BufferedReader in = new BufferedReader(new InputStreamReader(con.getErrorStream()));
            String inputLine;
            StringBuffer response = new StringBuffer();

            while ((inputLine = in.readLine()) != null) {
                response.append(inputLine);
            }
            in.close();
            System.out.println(response.toString());
            JSONObject response_object = (JSONObject) parser.parse(response.toString());
            System.out.println(response_object);
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
    System.out.println("Loading wallet: " + account_id);
    System.out.println(params);
    jsonObject.put("params", params);
    System.out.println("Checking blockchain info...");
    URL url = new URL("http://" + SatoshiQuest.BITCOIN_NODE_HOST + ":" + SatoshiQuest.BITCOIN_NODE_PORT + "/wallet/" + account_id);
    System.out.println(url.toString());
    System.out.println(jsonObject.toString());

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
        System.out.println(response.toString());
        JSONObject response_object = (JSONObject) parser.parse(response.toString());
        System.out.println(response_object);

        return new NodeWallet(account_id);
    } else {
        BufferedReader in = new BufferedReader(new InputStreamReader(con.getErrorStream()));
        String inputLine;
        StringBuffer response = new StringBuffer();

        while ((inputLine = in.readLine()) != null) {
            response.append(inputLine);
        }
        in.close();
        System.out.println(response.toString());
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
      System.out.println("Checking blockchain info...");
      URL url = new URL("http://" + BITCOIN_NODE_HOST + ":" + BITCOIN_NODE_PORT);
      System.out.println(url.toString());
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

      int responseCode = con.getResponseCode();

      BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
      String inputLine;
      StringBuffer response = new StringBuffer();

      while ((inputLine = in.readLine()) != null) {
        response.append(inputLine);
      }
      in.close();
      System.out.println(response.toString());
      return (JSONObject) parser.parse(response.toString());
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
        System.out.println("Parms: " + params);
        jsonObject.put("params", params);
        URL url = new URL("http://" + SatoshiQuest.BITCOIN_NODE_HOST + ":" + SatoshiQuest.BITCOIN_NODE_PORT + "/wallet/" + account_id);
        System.out.println(url.toString());
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
        System.out.println(jsonObject.toString());
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
            System.out.println(response.toString());
            JSONObject response_object = (JSONObject) parser.parse(response.toString());
	    Double d = Double.parseDouble(response_object.get("result").toString().trim()) * 100000000L;
	    final Long balance = d.longValue();
            System.out.println(balance);
	    return balance;

        } else {
            BufferedReader in = new BufferedReader(new InputStreamReader(con.getErrorStream()));
            String inputLine;
            StringBuffer response = new StringBuffer();

            while ((inputLine = in.readLine()) != null) {
                response.append(inputLine);
            }
            in.close();
            System.out.println(response.toString());
            JSONObject response_object = (JSONObject) parser.parse(response.toString());
	    Double d = Double.parseDouble(response_object.get("result").toString().trim()) * 100000000L;
	    final Long balance = d.longValue();
            System.out.println(balance);
	    return balance;
        }
	} catch(Exception e) {
		e.printStackTrace();
	}
	return 0L;
   }

  public Long getBalance(String account_id, int confirmations) throws IOException, org.json.simple.parser.ParseException {
	try {
	String address = REDIS.get("nodeAddress" + account_id);
        JSONParser parser = new JSONParser();

        final JSONObject jsonObject = new JSONObject();
        jsonObject.put("jsonrpc", "1.0");
        jsonObject.put("id", "satoshiquest");
        jsonObject.put("method", "getbalance");
        JSONArray params = new JSONArray();
	params.add("*");
	params.add(confirmations);
        System.out.println("Parms: " + params);
        jsonObject.put("params", params);
        URL url = new URL("http://" + SatoshiQuest.BITCOIN_NODE_HOST + ":" + SatoshiQuest.BITCOIN_NODE_PORT + "/wallet/" + account_id);
        System.out.println(url.toString());
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
        System.out.println(jsonObject.toString());
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
            System.out.println(response.toString());
            JSONObject response_object = (JSONObject) parser.parse(response.toString());
	    Double d = Double.parseDouble(response_object.get("result").toString().trim()) * 100000000L;
	    final Long balance = d.longValue();
            System.out.println(balance);
	    return balance;

        } else {
            BufferedReader in = new BufferedReader(new InputStreamReader(con.getErrorStream()));
            String inputLine;
            StringBuffer response = new StringBuffer();

            while ((inputLine = in.readLine()) != null) {
                response.append(inputLine);
            }
            in.close();
            System.out.println(response.toString());
            JSONObject response_object = (JSONObject) parser.parse(response.toString());
	    Double d = Double.parseDouble(response_object.get("result").toString().trim()) * 100000000L;
	    final Long balance = d.longValue();
            System.out.println(balance);
	    return balance;
        }
	} catch(Exception e) {
		e.printStackTrace();
	}
	return 0L;
   }

  public Long getUnconfirmedBalance(String account_id) throws IOException, org.json.simple.parser.ParseException {
	try {
	String address = REDIS.get("nodeAddress" + account_id);
        JSONParser parser = new JSONParser();

        final JSONObject jsonObject = new JSONObject();
        jsonObject.put("jsonrpc", "1.0");
        jsonObject.put("id", "satoshiquest");
        jsonObject.put("method", "getunconfirmedbalance");
        URL url = new URL("http://" + SatoshiQuest.BITCOIN_NODE_HOST + ":" + SatoshiQuest.BITCOIN_NODE_PORT + "/wallet/" + account_id);
        System.out.println(url.toString());
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
        System.out.println(jsonObject.toString());
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
            System.out.println(response.toString());
            JSONObject response_object = (JSONObject) parser.parse(response.toString());
	    Double d = Double.parseDouble(response_object.get("result").toString().trim()) * 100000000L;
	    final Long balance = d.longValue();
            System.out.println(balance);
	    return balance;

        } else {
            BufferedReader in = new BufferedReader(new InputStreamReader(con.getErrorStream()));
            String inputLine;
            StringBuffer response = new StringBuffer();

            while ((inputLine = in.readLine()) != null) {
                response.append(inputLine);
            }
            in.close();
            System.out.println(response.toString());
            JSONObject response_object = (JSONObject) parser.parse(response.toString());
	    Double d = Double.parseDouble(response_object.get("result").toString().trim()) * 100000000L;
	    final Long balance = d.longValue();
            System.out.println(balance);
	    return balance;
        }
	} catch(Exception e) {
		e.printStackTrace();
	}
	return 0L;
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
    System.out.println(sat);
    BigDecimal decimalSat = new BigDecimal(sat * 0.00000001);
    decimalSat = decimalSat.setScale(8, BigDecimal.ROUND_DOWN);
    System.out.println(decimalSat);
    params.add(decimalSat);
    params.add(SERVERDISPLAY_NAME);
    params.add(SERVERDISPLAY_NAME);
    params.add(false);
    params.add(false);
    params.add(18);
    System.out.println(params);
    jsonObject.put("params", params);
    System.out.println("Checking blockchain info...");
    URL url = new URL("http://" + BITCOIN_NODE_HOST + ":" + BITCOIN_NODE_PORT + "/wallet/" + account_id);
    System.out.println(url.toString());
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

    int responseCode = con.getResponseCode();

    BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
    String inputLine;
    StringBuffer response = new StringBuffer();

    while ((inputLine = in.readLine()) != null) {
      response.append(inputLine);
    }
    in.close();
    System.out.println(response.toString());
    JSONObject response_object = (JSONObject) parser.parse(response.toString());
    System.out.println(response_object);
    return (String) response_object.get("result");
	} catch(Exception e) {
		e.printStackTrace();
	}
    return "failed";
  }

  public String sendMany(String account_id, String address1, String address2, Long sat1, Long sat2, int confirmations) throws IOException, ParseException {
try {
    JSONParser parser = new JSONParser();

    final JSONObject jsonObject = new JSONObject();
    jsonObject.put("jsonrpc", "1.0");
    jsonObject.put("id", "satoshiquest");
    jsonObject.put("method", "sendmany");
    JSONArray params = new JSONArray();
    params.add("");
    final JSONObject addresses = new JSONObject();

    System.out.println(sat1);
    BigDecimal decimalSat1 = new BigDecimal(sat1 * 0.00000001);
    decimalSat1 = decimalSat1.setScale(8, BigDecimal.ROUND_DOWN);
    System.out.println(decimalSat1);
    addresses.put(address1,decimalSat1);


    System.out.println(sat2);
    BigDecimal decimalSat2 = new BigDecimal(sat2 * 0.00000001);
    decimalSat2 = decimalSat2.setScale(8, BigDecimal.ROUND_DOWN);
    System.out.println(decimalSat2);
    addresses.put(address2,decimalSat2);
    params.add(addresses);

    params.add(confirmations);
    params.add("SatoshiQuest");//the comment :p

    System.out.println(params);
    jsonObject.put("params", params);
    System.out.println("Checking blockchain info...");
    URL url = new URL("http://" + BITCOIN_NODE_HOST + ":" + BITCOIN_NODE_PORT + "/wallet/" + account_id);
    System.out.println(url.toString());
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

    int responseCode = con.getResponseCode();

    BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
    String inputLine;
    StringBuffer response = new StringBuffer();

    while ((inputLine = in.readLine()) != null) {
      response.append(inputLine);
    }
    in.close();
    System.out.println(response.toString());
    JSONObject response_object = (JSONObject) parser.parse(response.toString());
    System.out.println(response_object);
    return (String) response_object.get("result");
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
            User user=new User(player);
 	
		
                ScoreboardManager scoreboardManager;
                Scoreboard playSBoard;
                Objective playSBoardObj;
                scoreboardManager = Bukkit.getScoreboardManager();
                playSBoard= scoreboardManager.getNewScoreboard();
                playSBoardObj = playSBoard.registerNewObjective("wallet","dummy");

                playSBoardObj.setDisplaySlot(DisplaySlot.SIDEBAR);

                playSBoardObj.setDisplayName(ChatColor.GREEN + ChatColor.BOLD.toString() + "Satoshi" + ChatColor.GOLD + ChatColor.BOLD.toString() + "Quest");


		long lootBalance = (long)(getBalance(SERVERDISPLAY_NAME,1) * 0.85);
		Score score2 = playSBoardObj.getScore(ChatColor.GREEN + "Loot: " + Long.toString(lootBalance) + " -txFee");
		score2.setScore(1);

		Score score3 = playSBoardObj.getScore(ChatColor.GREEN + "Balance: " + Long.toString(getBalance(player.getUniqueId().toString(),1)));
		score3.setScore(2);
                Score score = playSBoardObj.getScore(ChatColor.GREEN + "Lives: " + REDIS.get("LivesLeft" + player.getUniqueId().toString()));
		score.setScore(3);
      		  player.setScoreboard(playSBoard);
            
       
       
    }
  public void teleportToSpawn(Player player) {
    SatoshiQuest satoshiQuest = this;
    // TODO: open the tps inventory
    player.sendMessage(ChatColor.GREEN + "Teleporting to spawn...");
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
            publish_stats();
		long waitTime = 1000 * 60 * 15;
		//announce("Current time is: "+ (new Date().getTime()));
		if((exTime <= ((new Date().getTime()) - waitTime))||(exRate == 0)) {
		try {
		exRate =  Double.parseDouble(getExchangeRate("btc"));
		livesRate =  (long)((BUYIN_AMOUNT/(exRate*0.00000001))*0.90);
		adminRate =  (long)((BUYIN_AMOUNT/(exRate*0.00000001))*0.10);
		totalLifeRate = livesRate + adminRate;
		announce("Currently Bitcoin is: $"+ exRate);
	        System.out.println("Currently Bitcoin is: $"+ exRate);
		announce("1 Life is: "+ totalLifeRate + " " +DENOMINATION_NAME);
	        System.out.println("1 Life is: "+ totalLifeRate + " " +DENOMINATION_NAME);
		} catch (Exception e) {
			e.printStackTrace();
		}
		exTime = new Date().getTime();
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
        System.out.println(url.toString());
        HttpURLConnection con = (HttpURLConnection) url.openConnection();

        con.setRequestMethod("POST");
        con.setRequestProperty("Content-Type", "application/json; charset=UTF-8");

        con.setDoOutput(true);
        OutputStreamWriter out = new OutputStreamWriter(con.getOutputStream());
        out.write(jsonObject.toString());
        out.close();

        int responseCode = con.getResponseCode();

        BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
        String inputLine;
        StringBuffer response = new StringBuffer();

        while ((inputLine = in.readLine()) != null) {
          response.append(inputLine);
        }
        in.close();
        System.out.println(response.toString());
        JSONObject response_object = (JSONObject) parser.parse(response.toString());
        System.out.println(response_object);
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

	if (!((playerx<spawnx+SPAWN_PROTECT_RADIUS)&&(playerx>spawnx-SPAWN_PROTECT_RADIUS)))return true;
	else if(!((playerz<spawnz+SPAWN_PROTECT_RADIUS)&&(playerz>spawnz-SPAWN_PROTECT_RADIUS)))return true;
	
		//System.out.println("You may not build at spawn.");  
		//player.sendMessage(ChatColor.RED + "you cant build at spawn.");
               return false;//not

  }

  public static boolean isNearLoot(Player player) {
	Location spawn = Bukkit.getServer().getWorld(SERVERDISPLAY_NAME).getSpawnLocation();
	double lootX = Double.parseDouble(REDIS.get("lootSpawnX"));
	double lootZ = Double.parseDouble(REDIS.get("lootSpawnZ"));
		Double playerx=(double)player.getLocation().getX();
                Double playerz=(double)player.getLocation().getZ();
	double announceRadius = (double)LOOT_ANNOUNCE_RADIUS;

	if ((((playerx<lootX+announceRadius)&&(playerx>lootX-announceRadius))) || (((playerz<lootZ+announceRadius)&&(playerz>lootZ-announceRadius)))) {
		System.out.println("You are near...");
		String toAnnounce = (player.getName() + "is near the loot! their current location is: X:" + playerx.intValue() + " Z:" + playerz.intValue());
		announce(toAnnounce);
		//setLootBlocks();
		return true;
		}
               return false;//not

  }

  public void didFindLoot(Player player) {
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

		//sendloot to winner
		long sendLoot = 0L;
		String result = "failed";
		try {
		if (getBalance(SERVERDISPLAY_NAME,1) > 0) {
			sendLoot = (long)((double)getBalance(SERVERDISPLAY_NAME,1) * 0.85);
			Long sendback = (long)((double)getBalance(SERVERDISPLAY_NAME,1) * 0.025);
		if (REDIS.exists("ExternalAddress" +player.getUniqueId().toString())) {
		result = sendMany(SERVERDISPLAY_NAME, REDIS.get("ExternalAddress" +player.getUniqueId().toString()), REDIS.get("nodeAddress"+SERVERDISPLAY_NAME), sendLoot, sendback, 24);
		} else {
		result = sendMany(SERVERDISPLAY_NAME, REDIS.get("nodeAddress"+player.getUniqueId().toString()), REDIS.get("nodeAddress"+SERVERDISPLAY_NAME), sendLoot, sendback, 24);
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

	int iter=1;
		for (String templeaderBoardList : leaderBoardList) {
		announce(iter+") "+ REDIS.get(templeaderBoardList));
		iter++;
		}
REDIS.set("LeaderBoard " + iter,dateFormat.format(date) + " " + player.getName() + " " + sendLoot);
		announce("NEW! " +iter+") "+ REDIS.get("LeaderBoard " + iter));

    World world = Bukkit.getServer().getWorld(SERVERDISPLAY_NAME);
 if(!world.equals(null)) {
File BaseFolder = new File(Bukkit.getWorlds().get(0).getWorldFolder(), "players");
            for(Player p : world.getPlayers()){
                p.teleport(Bukkit.getWorlds().get(0).getSpawnLocation());
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
		int tempLives = Integer.parseInt(REDIS.get("LivesLeft" +p.getUniqueId().toString()));
		if (tempLives >= 1) {
		REDIS.set("LivesLeft" +p.getUniqueId().toString(), Integer.toString(tempLives-1));
		}
		try {
		updateScoreboard(p);
		} catch (Exception excep) {
			System.out.println(excep);
		}
            }
	    for(OfflinePlayer offlinePlayer : Bukkit.getOfflinePlayers()) {
		REDIS.set("ClearInv" +offlinePlayer.getUniqueId().toString(), "true");
		}
            Bukkit.getServer().unloadWorld(world, false);
            System.out.println(world.getName() + " unloaded!");
        }
	File dir = new File(getServer().getWorldContainer().getAbsolutePath() + "/" + SERVERDISPLAY_NAME);
	boolean deld = deleteWorld(dir);
		if (deld == true) {
			System.out.println(SERVERDISPLAY_NAME + " world deleted and ready for reset!");
		}
		REDIS.del("winner");
		REDIS.del("lootSpawnY");
		REDIS.del("spawnCreated");
//Bukkit.getServer().dispatchCommand(Bukkit.getConsoleSender(), "restart");
		onEnable();
	} 
  }

public void teleportToLootSpawn(Player player) {
        if (!player.hasMetadata("teleporting")) {
            player.sendMessage(ChatColor.GREEN + "Teleporting back now that world is reset...");
            player.setMetadata("teleporting", new FixedMetadataValue(this, true));
            Location location= getServer().createWorld(new WorldCreator(SERVERDISPLAY_NAME)).getSpawnLocation();
            System.out.println("location: " + location);
            final Location spawn=location;

            Chunk c = spawn.getChunk();
            System.out.println("Chunk: " + c);
            if (!c.isLoaded()) {
                c.load();
            }
            SatoshiQuest plugin = this;
            BukkitScheduler scheduler = Bukkit.getServer().getScheduler();
            scheduler.scheduleSyncDelayedTask(this, new Runnable() {

                public void run() {
                    player.teleport(spawn);
                    player.removeMetadata("teleporting", plugin);
                }
            }, 60L);
        }
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

  private void addSpawnBlocks() {
	Location spawn = Bukkit.getServer().getWorld(SERVERDISPLAY_NAME).getSpawnLocation();
	Block spawnBlock = spawn.getBlock();
	double spawnx = spawn.getX();
	double spawnz = spawn.getZ();
	double spawny = spawn.getY();
	double spawnRadius = SPAWN_PROTECT_RADIUS;
	double posX = spawnx + (spawnRadius-2);
	double negX = spawnx - (spawnRadius+2);
	double posZ = spawnz + (spawnRadius-2);
	double negZ = spawnz - (spawnRadius+2);
		for(double x = negX; x <= posX; x++) {
			for(double z = negZ; z <= posZ; z++) {
	                Block tempblock = spawnBlock.getWorld().getBlockAt((int)x,((int)spawnBlock.getWorld().getHighestBlockAt((int)x, (int)z).getY()-1), (int)z);
			tempblock.setType(Material.EMERALD_BLOCK);
			}
		}
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

  public void sendWalletInfo(final Player player, final User user) {
    if (BITCOIN_NODE_HOST != null) {
      // TODO: Rewrite send wallet info
    }
    try {
      Long balance = user.wallet.getBalance(0);

      player.sendMessage("Address: " + user.wallet.address);
      player.sendMessage("Balance: " + balance);
      player.sendMessage(
          "URL: " + ChatColor.BLUE + ChatColor.UNDERLINE + ChatColor.BOLD + ADDRESS_URL + user.wallet.getAccountAddress());
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
  public boolean sendDiscordMessage(String content) {
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
            return true;
          } else {
            BufferedReader in = new BufferedReader(new InputStreamReader(con.getErrorStream()));
            String inputLine;
            StringBuffer response = new StringBuffer();

            while ((inputLine = in.readLine()) != null) {
              response.append(inputLine);
            }
            in.close();
            System.out.println(response.toString());
            return false;
          }

      } catch (Exception e) {
          e.printStackTrace();
          return false;
      }
    }
    return false;

  }
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
  public void crashtest() {
    this.setEnabled(false);
  }


    public static String getExchangeRate(String crypto)
	{
	String price="0000.00000000";
	 try {
            
                URL url=new URL("https://api.cryptonator.com/api/ticker/"+crypto+"-usd");
                
                System.out.println(url.toString());
                HttpsURLConnection con = (HttpsURLConnection) url.openConnection();
                con.setRequestMethod("GET");
                con.setRequestProperty("User-Agent", "Mozilla/1.22 (compatible; MSIE 2.0; Windows 3.1)");
                con.setRequestProperty("Accept-Language", "en-US,en;q=0.5");

                int responseCode = con.getResponseCode();

                BufferedReader in = new BufferedReader(
                        new InputStreamReader(con.getInputStream()));
                String inputLine;
                StringBuffer response = new StringBuffer();

                while ((inputLine = in.readLine()) != null) {
                    response.append(inputLine);
                }
                in.close();
		JSONParser parser = new JSONParser();
            	final JSONObject jsonobj,jsonobj2;
            try {
                jsonobj = (JSONObject) parser.parse(response.toString());
                jsonobj2 = (JSONObject) parser.parse(jsonobj.get("ticker").toString());
		//double val=Double.parseDouble(jsonobj2.get("price").toString());
		

		price=jsonobj2.get("price").toString();
                //System.out.println(crypto + "price: "+price);

            } catch (org.json.simple.parser.ParseException e) {
                e.printStackTrace();
            }

        } catch (IOException e) {
            System.out.println("[PRICE] problem updating price for "+crypto);
            System.out.println(e);
            // wallet might be new and it's not listed on the blockchain yet
        } 
	return price;
    }


}


