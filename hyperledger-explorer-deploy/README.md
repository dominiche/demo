base on hyperledger/blockchain-explorer release-3.6

0. build blockchain-explorer image
```$sh
cd hyperledger-explorer-Dockerfile/R_3.7
docker build -f Dockerfile_3.7.1 -t dominic/blockchain-explorer:3.7.1 .
```
1. start fabric network in demo/fabric-demo/src/main/resources/sdkintegration
    start fabric network. then, run the network: create channel "foo", install and instantiate a chaincode, do some transactions
    (eg. run End2endIT test in fabric-sdk-java)

2. init explorer database
in demo/hyperledger-explorer-deploy, start docker-compose, 
```$sh
docker-compose up -d
docker exec -it postgresql bash
cd /opt/pgsql/
psql -U postgres
\i explorerpg.sql
\q
exit
docker-compose down
```
3. start explorer
in demo/hyperledger-explorer-deploy, start docker-compose
```$sh
docker-compose up -d
```

TODO:   
    problems: the home page is blank(http://localhost:8080/), though the swagger page is ok(http://localhost:8080/api-docs/)
    2018/10/08 already fix.