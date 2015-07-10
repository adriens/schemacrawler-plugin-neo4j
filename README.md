# schemacrawler-plugin-neo4j
A plugin to dump a neo4j database that draws database schema

Still in very early development stage. I'm using this project to discover Neo4j features.

Start neo4j server :

  cd ~/apps/neo4j-community-2.3.0-M02 && ./bin/neo4j start
  
Stop neo4j server and make some cleanup :

  cd ~/apps/neo4j-community-2.3.0-M02 && ./bin/neo4j stop && rm -rf ~/neo4j
  
Run schemacrawler :

  cd ~/tmp
  sudo cp ~/NetBeansProjects/schemacrawler-plugin-neo4j/target/schemacrawler-plugin-neo4j-1.0-SNAPSHOT.jar /opt/schemacrawler/lib/ && schemacrawler -host=localhost -port=5432 -database=sportsdb -user=sports_adm -password=user_adm  -schemas=public -c=neo4j -infolevel=maximum -server=postgresql -loglevel=CONFIG -outputDir=./neo4j
