How to run (for testing and development)
=========================================

to rename easy use the renameScript.sh
open and Change the "CHANGEME"s accordingly

1. run make
--------------

2. create variables.env and write your configuration
-----------------------------------------------------
An example configuration would be:

```
    - BITCOIN_PORT_8332_TCP_PORT=8332
    - ADMIN_ADDRESS=x
    - ADMIN_UUID=x
    - BITCOIN_ENV_USERNAME=x
    - BITCOIN_ENV_PASSWORD=x
    - BITCOIN_NODE_HOST=192.168.0.11
    - BUYIN_AMOUNT=1.00
    - LIVES_PERBUYIN=1
    - SPAWN_PROTECT_RADIUS=25
    - LOOT_RADIUS_MIN=500
    - LOOT_RADIUS_MAX=500
    - LOOT_ANNOUNCE_RADIUS=100
    - ADDRESS_URL=https://www.blockchain.com/btc/address/
    - TX_URL=https://www.blockchain.com/btc/tx/
    - SERVER_WEBSITE=http://AllAboutBTC.com/SatoshiQuest.html
    - COINGECKO_CRYPTO=bitcoin
    - DENOMINATION_NAME=Sats
    - SERVER_NAME=SatoshiQuest
    - CRYPTO_DECIMALS=8
    - DISPLAY_DECIMALS=6
    - USD_DECIMALS=0.000
    - CONFS_TARGET=1
    - VOTE_URL
    - MIN_FEE=1.2
    - MAX_FEE=15
    - CRYPTO_TICKER=BTC
    - VOTE_API_KEY
    - DISCORD_URL
    - DISCORD_HOOK_CHANNEL_ID
    - DISCORD_HOOK_URL
    - ADMIN_ADDRESS
    - ADMIN2_ADDRESS
```

3. run docker-compose up
--------------------------

port to docker
----------------------
sudo iptables -t nat -L -n

sudo iptables -t nat -A POSTROUTING --source 172.17.0.3 --destination 172.17.0.3 -p tcp --dport 25565 -j MASQUERADE

----------------------
to be added to the HUB with other servers enable Bungee and give the HUB operator the IP:PORT and Name to display
Configuring your Spigot servers for BungeeCordPermalink

    On your Spigot servers, navigate to the Spigot directory and open spigot.yml.

    Change bungeecord: false to bungeecord: true. Save and exit.

    Open server.properties.

    Change online-mode=true to online-mode=false. Save and exit.

    Restart the Spigot servers.

How to run without docker

=========================================
1. Download [Spigot 1.16.1](https://getbukkit.org/get/fDoqnvFqdhjFcURmY7Oqtzu0giKFAiyY)
2. Start server with ```java -jar Spigot-1.16.1.jar```
    Restart the Spigot servers.
3. Copy SatoshiQuest.jar to plugin folder
4. Set EULA.txt to true
5. Edit and run setenv.sh script
6. Make sure you have Bitcoin and Redis server running.
7. Edit server.properties to point to your local IP
8. Start server with ```java -jar Spigot-1.16.1.jar```


InGame mod/admin commands:

/mod list -list of mods

/mod flag -locks spawn even when players have lives i think?

/mod beta -gives new players 1 free life and shows beta on rounds

/mod expandingloot -will allow loot to expand further after each round won

/mod add playername -mods a player
