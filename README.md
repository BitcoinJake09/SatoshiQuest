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
