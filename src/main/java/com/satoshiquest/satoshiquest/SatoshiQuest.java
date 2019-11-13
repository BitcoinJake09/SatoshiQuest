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
          System.getenv("MINER_FEE") != null ? Long.parseLong(System.getenv("MINER_FEE")) : 10000;

  public static final String SERVER_NAME =
      System.getenv("SERVER_NAME") != null ? System.getenv("SERVER_NAME") : "SatoshiQuest";

  public static final String ADDRESS_URL =
      System.getenv("ADDRESS_URL") != null ? System.getenv("ADDRESS_URL") : "https://www.blockchain.com/btc/address/";



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

  // Default price: 10,000 satoshis or 100 bits
  public static final Long BUYIN_AMOUNT =
      System.getenv("BUYIN_AMOUNT") != null ? Long.parseLong(System.getenv("BUYIN_AMOUNT")) : 10000;

  public static final Long LIVES_PERBUYIN =
      System.getenv("LIVES_PERBUYIN") != null ? Long.parseLong(System.getenv("LIVES_PERBUYIN")) : 1;

  public static final Long SPAWN_PROTECT_RADIUS =
      System.getenv("SPAWN_PROTECT_RADIUS") != null ? Long.parseLong(System.getenv("SPAWN_PROTECT_RADIUS")) : 14;

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
  // when true, server is closed for maintenance and not allowing players to join in.
  public boolean maintenance_mode = false;

  private Map<String, CommandAction> commands;
  private Map<String, CommandAction> modCommands;
  private Player[] moderators;


  @Override
  public void onEnable() {
    log("[startup] SatoshiQuest starting");
    
    try {
  
      if (ADMIN_UUID == null) {
        log("[warning] ADMIN_UUID env variable to is not set.");
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

  System.out.println("Loot X,Z: " + LootSpawnX + " " + LootSpawnZ);
	if (REDIS.exists("nodeWallet")) {
		try {
			wallet = loadWallet(REDIS.get("nodeWallet"));
		        System.out.println("[world wallet] trying to load node wallet");
		} catch (NullPointerException npe) {
			npe.printStackTrace();
			System.out.println("[world wallet] wallet not found, attempting to create.");
		}
	} else if(!REDIS.exists("nodeWallet"))
	{
	        wallet = generateNewWallet(SERVERDISPLAY_NAME);
        	System.out.println("[world wallet] generated new wallet");
		REDIS.set("nodeWallet",SERVERDISPLAY_NAME);
	}

      //System.out.println("[world wallet] address: " + wallet.getAccountAddress());



      if (BITCOIN_NODE_HOST != null) {
        System.out.println("[startup] checking bitcoin node connection");
        getBlockChainInfo();
      }


      // creates scheduled timers (update balances, etc)
      createScheduledTimers();
      commands = new HashMap<String, CommandAction>();
      commands.put("wallet", new WalletCommand(this));
      modCommands = new HashMap<String, CommandAction>();
      modCommands.put("crashTest", new CrashtestCommand(this));
      modCommands.put("mod", new ModCommand());
      modCommands.put("ban", new BanCommand());
      modCommands.put("unban", new UnbanCommand());
      modCommands.put("banlist", new BanlistCommand());
      modCommands.put("spectate", new SpectateCommand(this));
      modCommands.put("emergencystop", new EmergencystopCommand());
      modCommands.put("motd", new MOTDCommand(this));
      publish_stats();
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

    return new NodeWallet(account_id);
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

    return new NodeWallet(account_id);
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

  public void announce(final String message) {
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

                Score score = playSBoardObj.getScore(ChatColor.GREEN + SatoshiQuest.DENOMINATION_NAME);
		
		score.setScore(Integer.parseInt(REDIS.get("LivesLeft:" + player.getUniqueId().toString())));
      		  player.setScoreboard(playSBoard);
            
       
       
    }
  public void teleportToSpawn(Player player) {
    SatoshiQuest satoshiQuest = this;
    // TODO: open the tps inventory
    player.sendMessage(ChatColor.GREEN + "Teleporting to spawn...");
    player.setMetadata("teleporting", new FixedMetadataValue(satoshiQuest, true));
    World world = Bukkit.getWorld("world");

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
            World world = Bukkit.getWorld("world");
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
          }
        },
        0,
        30000L);
  }

  public void publish_stats() {
    try {
      Long balance = wallet.getBalance(0);
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
	Location spawn = Bukkit.getServer().getWorlds().get(0).getSpawnLocation();
		double spawnx = spawn.getX();
		double spawnz = spawn.getZ();
		double playerx=(double)player.getLocation().getX();
                double playerz=(double)player.getLocation().getZ();

	if (!((playerx<spawnx+SPAWN_PROTECT_RADIUS)&&(playerx>spawnx-SPAWN_PROTECT_RADIUS)))return true;
	else if(!((playerz<spawnz+SPAWN_PROTECT_RADIUS)&&(playerz>spawnz-SPAWN_PROTECT_RADIUS)))return true;
		System.out.println("You may not build at spawn.");  
               return false;//not

  }

  public static boolean isNearLoot(Player player) {
	Location spawn = Bukkit.getServer().getWorlds().get(0).getSpawnLocation();
		double spawnx = spawn.getX();
		double spawnz = spawn.getZ();
		double playerx=(double)player.getLocation().getX();
                double playerz=(double)player.getLocation().getZ();

	if (!((playerx<spawnx+8)&&(playerx>spawnx-8)))return true;
	else if(!((playerz<spawnz+8)&&(playerz>spawnz-8)))return true;
		System.out.println("You are near...");  
               return false;//not

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

      player.sendMessage("Address: " + user.wallet.getAccountAddress());
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

  public void crashtest() {
    this.setEnabled(false);
  }
}
