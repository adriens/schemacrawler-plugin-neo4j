# schemacrawler-plugin-neo4j [![Build Status](https://travis-ci.org/adriens/schemacrawler-plugin-neo4j.svg?branch=master)](https://travis-ci.org/adriens/schemacrawler-plugin-neo4j) [![Coverage Status](https://s3.amazonaws.com/assets.coveralls.io/badges/coveralls_3.svg)](https://coveralls.io/github/adriens/schemacrawler-plugin-neo4j?branch=master)

## Description

This is a [schemacrawler](http://sualeh.github.io/SchemaCrawler/ "Schemacrawler Homepage") [plugin](http://sualeh.github.io/SchemaCrawler/plugins.html "Schemacrawler plugins homepage") that dumps database structure (also called schema) into a [neo4j](http://neo4j.com/ "Neo4j homepage") database. Once this done, you can view it in [neo4j web console](http://neo4j.com/developer/guide-data-visualization/#_product_keylines_neo4j_graph_visualization "Neo4j visualization"), perform [CYPHER](http://neo4j.com/developer/cypher-query-language/#_about_cypher "About CYPHER") queries on it and hence allows you to report your database schema the way you want.

<a href="https://vimeo.com/97204829" target="_blank"><img src="http://dev.assets.neo4j.com.s3.amazonaws.com/wp-content/uploads/2014/08/VLBCcWS-u6EcWuGxslN9UxCJWIiMNFR5Kv0vFnVqz3KVyvih5n3LF3RgEWpT99V6oTzlpnlvzroac8viV2gm4mOWgvw5IP8HiyZfCbx498ZgEQBX9XgqFumV.png?_ga=1.151612667.310337467.1434158905"
alt="Screenshot" border="10" /></a>


You can see these features as an addon to native schemacrawler features.

## Why spending time developing this software ?

Because i find it fun to design a graph database schema to modelize a database schema ;-p

## Development status

For now this plugin is its very early stages as i use this project to :

* Develop schemacrawler plugins
* Discover neo4j technology and what i can do with it, at my job, but also just to jave fun with drawing nice graphs


# Create your graph

## Build the plugin

As this is a standard maven projet, just :

1. Clone this git repo
2. Jump in the project directory
3. `mvn clean package`
4. Get the `jar` in the `target` directory

Below command to build it yourself :

    git clone https://github.com/adriens/schemacrawler-plugin-neo4j.git
    cd schemacrawler-plugin-neo4j
    mvn package
    ls -la target/schemacrawler-plugin-neo4j-${version}.jar

## Schemacrawler requirements

To create your neo4j database for your database schema, you need :

* A proper schemacrawler install
* A database with a schema you want to analyze (and if possible a nice one).
I personally did use the sportsdb database sample (http://www.sportsdb.org/sd/samples)
* Test a schemacrawler `graph` generation
* drop the jar you've built in `$SCHEMACARWLER_HOME/lib`

## Neo4j requirements

Follow neo4j [install instructions](http://neo4j.com/docs/stable/server-installation.html "Neo4j install instructions").
In the following sections, i will assume you are runnning neo4j on linux as it's
easier to document, but things are exactly the same.

Choose an **empty and dedicated* directoty that will contain the generated
database : BEWARE AS DURING DATABASE CREATION THIS DIRECTORY WILL BE DELETED
BY THE PLUGIN !**

# Generate the neo4j graph database

**Let's assume your neo4j graph database directory is `~/neo4j.`**

## Run the schemacrawler command

`schemacrawler -host=localhost -port=5432 -database=sportsdb -user=sports_adm -password=user_adm  -schemas=public -c=neo4j -infolevel=maximum -server=postgresql -loglevel=CONFIG -outputDir=./neo4j`

## Start neo4j server

`cd ~/apps/neo4j-community-2.3.0-M02 && ./bin/neo4j start`

## Browse the database

Go to neo4j webapp (http://localhost:7474) and take a look at your graph
database schema


# (Dirty) dev scripts


##  Start neo4j server :

`cd ~/apps/neo4j-community-2.3.0-M02 && ./bin/neo4j start`

## Stop neo4j server and make some cleanup :

`cd ~/apps/neo4j-community-2.3.0-M02 && ./bin/neo4j stop && rm -rf ~/neo4j`

## Run schemacrawler

`cd ~/tmp
sudo cp ~/NetBeansProjects/schemacrawler-plugin-neo4j/target/schemacrawler-plugin-neo4j-1.0-SNAPSHOT.jar /opt/schemacrawler/lib/ && schemacrawler -host=localhost -port=5432 -database=sportsdb -user=sports_adm -password=user_adm  -schemas=public -c=neo4j -infolevel=maximum -server=postgresql -loglevel=CONFIG -outputDir=./neo4j`

# Maven Central Repo availability

A ticket has been open to upload artifact to Sonatype (http://central.sonatype.org/), see
https://issues.sonatype.org/browse/OSSRH-16543 for more details.

# Donate

I'm not asking for money nor any kind of gift, but sometimes, to keep motivation
safe while developing free software, it's nice to get some recognization.

So, *if you like this software*, please :

1. Simply star the project on github
2. Ask to connect with me on my [my linkedin profile](https://www.linkedin.com/profile/view?id=253709684 "my linkedin profile")
3. Write some recommandation


# Acknowledgements

I want to thank my dear girlfriend for her patience when i develop software on
the couch while she's watching and for supporting and listening to my
enthousiatics thoughts about development, free software, ... and so many other
tech things...

I also want to thank [Sualeh Fatehi](https://github.com/sualeh "Sualeh Fatehi")
for his very kind help and support on Schemacrawler, and for always answering
questions very fast... and of course for his great software with which i can
make so many cool things !
