# schemacrawler-plugin-neo4j

## Description

This is a schemacrawler plugin that dumps database structure (also called schema) into a neo4j database. Once this done, you can view it in neo4j web console, perform CYPHER queries on it and hence allows you to report your database schema the way you want.
You can see these features as an addon not native schemacrawler features.

## Why ?

Because i find it fun to design a graph database schema to modelize a database schema ;-p

## Development status

For now this plugin is its very early stages as i use this project to :

* Develop schemacrawler plugins
* Discover neo4j technology and what i can do with it, at my job, but also just to jave fun with drawing nice graphs


# Create your graph

## Build the plugin


## Schemacrawler requirements

To create your neo4j database for your database schema, you need :

* A proper schemacrawler install
* A database with a schema you want to analyze (and if possible a nice one). I personally did use the sportsdb database sample (http://www.sportsdb.org/sd/samples)
* Test a schemacrawler `graph` generation
* drop the jar you've built in `$SCHEMACARWLER_HOME/lib`

## Neo4j requirements

Follow neo4j install instructions. In the following sections, i will assume you are runnning neo4j on linux as it's easier to document, but things are exactly the same.

Choose an *empty and dedicated* directoty that will contain the generated datasbe : BEWARE AS DURING DATABASE CREATION THIS DIRECTORY WILL BE DELETED BU THE PLUGIN !*

# Generate the neo4j graph database !

**Let's assume your neo4j graph database directory is `~/neo4j.`**

1. Run the schemacrawler command :

`schemacrawler -host=localhost -port=5432 -database=sportsdb -user=sports_adm -password=user_adm  -schemas=public -c=neo4j -infolevel=maximum -server=postgresql -loglevel=CONFIG -outputDir=./neo4j`
2. Start neo4j server :
`cd ~/apps/neo4j-community-2.3.0-M02 && ./bin/neo4j start`
3. Go to neo4j webapp (http://localhost:7474) and take a look at your graph database schema


# Dirty dev scripts


##  Start neo4j server :

`cd ~/apps/neo4j-community-2.3.0-M02 && ./bin/neo4j start`
  
## Stop neo4j server and make some cleanup :

`cd ~/apps/neo4j-community-2.3.0-M02 && ./bin/neo4j stop && rm -rf ~/neo4j`
  
## Run schemacrawler

`cd ~/tmp
sudo cp ~/NetBeansProjects/schemacrawler-plugin-neo4j/target/schemacrawler-plugin-neo4j-1.0-SNAPSHOT.jar /opt/schemacrawler/lib/ && schemacrawler -host=localhost -port=5432 -database=sportsdb -user=sports_adm -password=user_adm  -schemas=public -c=neo4j -infolevel=maximum -server=postgresql -loglevel=CONFIG -outputDir=./neo4j`
