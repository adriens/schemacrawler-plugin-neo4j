## List all tables

List all tables :

```
match (n:TABLE) return n;
```

Count tables :

```
match (n:TABLE) return count(*);
```

Graph how tables references each others (not yet done) :

```
MATCH (n:FOREIGN_KEY)-[b:BELONGS_TO_TABLE]->(t:TABLE) RETURN n,t LIMIT 500;
```