How to run (for testing and development)
=========================================
1. run make
--------------

2. create variables.env and write your configuration
-----------------------------------------------------
An example configuration would be:

```
BITCOIN_PORT_8332_TCP_PORT=8332
ADMIN_ADDRESS=x
ADMIN_UUID=x
BITCOIN_ENV_USERNAME=x
BITCOIN_ENV_PASSWORD=x
BITCOIN_NODE_HOST=192.168.0.11
```

3. run docker-compose up
--------------------------

port to docker
----------------------
sudo iptables -t nat -L -n

sudo iptables -t nat -A POSTROUTING --source 172.17.0.3 --destination 172.17.0.3 -p tcp --dport 25564 -j MASQUERADE

