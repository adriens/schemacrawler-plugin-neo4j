package io.github.adriens.schemacrawler.plugin.neo4j;
/*
 http://www.tutorialspoint.com/neo4j/neo4j_native_java_api_example.htm
 http://www.sportsstandards.org/oc

 Search nodes :

 http://stackoverflow.com/questions/15368579/select-a-node-by-name-in-neo4j-in-java

 schemacrawler -host=localhost -port=5432 -database=sportsdb -user=sc -password=sc  -schemas=public -c=neo4j -infolevel=maximum -server=postgresql -loglevel=CONFIG -outputDir=c:/tmp

 schemacrawler -host=localhost -port=5432 -database=sportsdb -user=sports_adm -password=user_adm  -schemas=public -c=neo4j -infolevel=maximum -server=postgresql -loglevel=CONFIG -outputDir=./neo4j

 MATCH (n)
 RETURN n;

 MATCH (a)-[r:CONTAINS_SCHEMA]->(b)
 RETURN r


 */

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.util.logging.Logger;
import org.apache.commons.io.FileUtils;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Transaction;
import org.neo4j.graphdb.factory.GraphDatabaseFactory;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Relationship;

import schemacrawler.schema.Catalog;
import schemacrawler.schema.Column;
import schemacrawler.schema.ForeignKey;
import schemacrawler.schema.ForeignKeyColumnReference;
import schemacrawler.schema.Index;
import schemacrawler.schema.IndexColumn;
import schemacrawler.schema.PrimaryKey;
import schemacrawler.schema.Schema;
import schemacrawler.schema.Sequence;
import schemacrawler.schema.Synonym;
import schemacrawler.schema.Table;
import schemacrawler.tools.executable.BaseStagedExecutable;

/**
 *
 * @author adriens
 */
public class AdditionalExecutable extends BaseStagedExecutable {

    private static final Logger LOGGER = Logger.getLogger(AdditionalExecutable.class.getName());

    static final String COMMAND = "neo4j";

    private String outputDir;
    private GraphDatabaseFactory dbFactory;
    private GraphDatabaseService dbService;

    protected AdditionalExecutable() {
        super(COMMAND);
    }

    public void init() throws IOException {
        // recursively delete directory contents
        System.out.println("Deleting directory <" + getOutputDir() + "> ...");
        FileUtils.deleteDirectory(new File(getOutputDir()));
        System.out.println("Output directory cleaned");
        setDbFactory(new GraphDatabaseFactory());
        setDbService(dbFactory.newEmbeddedDatabase(getOutputDir()));
    }

    public void feedTables(final Catalog catalog) {
        try (Transaction tx = getDbService().beginTx()) {
            for (final Schema schema : catalog.getSchemas()) {
                Node schemaNode = dbService.createNode(DatabaseNodeType.SCHEMA);
                schemaNode.setProperty("name", schema.getName());
                schemaNode.setProperty("fullName", schema.getFullName());
                schemaNode.setProperty("lookupKey", schema.getLookupKey());
                schemaNode.setProperty("remarks", schema.getRemarks());

                // we have the schema node
                // for each table, attach the table to the schema without
                // having to find it for better performances (no index needed)
                for (final Table table : catalog.getTables(schema)) {
                    Node tableNode = dbService.createNode(DatabaseNodeType.TABLE);
                    tableNode.setProperty("nbColumns", table.getColumns().size());
                    tableNode.setProperty("definition", table.getDefinition());
                    tableNode.setProperty("lookupKey", table.getLookupKey());
                    tableNode.setProperty("name", table.getName());
                    tableNode.setProperty("remarks", table.getRemarks());
                    tableNode.setProperty("schemaName", table.getSchema().getName());
                    tableNode.setProperty("fullName", table.getFullName());
                    tableNode.setProperty("tableType", table.getTableType().toString());
                    table.getDefinition();

                    // attach the table to its schema
                    Relationship schemaRelationShip = tableNode.createRelationshipTo(schemaNode, SchemaRelationShips.BELONGS_TO_SCHEMA);

                    //writer.println("o--> " + table);
                    for (final Column column : table.getColumns()) {
                        Node columnNode = dbService.createNode(DatabaseNodeType.TABLE_COLUMN);
                        columnNode.setProperty("columnOrdinalPosition", column.getOrdinalPosition());
                        columnNode.setProperty("columnDataType", column.getColumnDataType().toString());
                        columnNode.setProperty("name", column.getName());
                        columnNode.setProperty("fullName", column.getFullName());
                        if (column.getDefaultValue() != null) {
                            columnNode.setProperty("defaultValue", column.getDefaultValue());
                        }

                        if (column.getLookupKey() != null) {
                            columnNode.setProperty("lookupKey", column.getLookupKey());
                        }

                        if (column.getRemarks() != null) {
                            columnNode.setProperty("remarks", column.getRemarks());
                        }

                        if (column.getShortName() != null) {
                            columnNode.setProperty("shortName", column.getShortName());
                        }
                        columnNode.setProperty("size", column.getSize());
                        columnNode.setProperty("width", column.getWidth());

                        Relationship relationship = columnNode.createRelationshipTo(tableNode, SchemaRelationShips.IS_COLUMN_OF_TABLE);
                    }
                    // end of columns
                    for (final Index index : table.getIndexes()) {
                        Node indexNode;
                        if (index.isUnique()) {
                            indexNode = dbService.createNode(DatabaseNodeType.UNIQUE_INDEX);
                        } else {
                            indexNode = dbService.createNode(DatabaseNodeType.INDEX);
                        }
                        indexNode.setProperty("schema", index.getSchema().getName());
                        indexNode.setProperty("name", index.getName());
                        indexNode.setProperty("shortName", index.getShortName());
                        indexNode.setProperty("fullName", index.getFullName());
                        indexNode.setProperty("cardinality", index.getCardinality());
                        indexNode.setProperty("indexPages", index.getPages());

                        indexNode.setProperty("indexTypeName", index.getIndexType().name());
                        indexNode.setProperty("indexTypeId", index.getIndexType().getId());
                        indexNode.setProperty("indexTypeOrdinal", index.getIndexType().ordinal());

                        //indexNode.setProperty("lookupKey", index.getLookupKey());
                        if (index.hasDefinition()) {
                            indexNode.setProperty("definition", index.getDefinition());
                        }

                        if (index.hasRemarks()) {
                            indexNode.setProperty("remarks", index.getRemarks());
                        }

                        // attach index to it's table
                        Relationship indexBelongsToTable = indexNode.createRelationshipTo(tableNode, SchemaRelationShips.BELONGS_TO_TABLE);
                        // attach index to table columns
                        for (final IndexColumn indexColumn : index.getColumns()) {
                            //indexColumn.getFullName()
                            // Attach index to table column
                            // get the target column to attach by its fullname
                            Node targetColumnNode = dbService.findNode(DatabaseNodeType.TABLE_COLUMN, "fullName", indexColumn.getFullName());
                            Relationship indexPointsToColumns = indexNode.createRelationshipTo(targetColumnNode, SchemaRelationShips.INDEXES_COLUMN);
                        }

                    }

                    // put the PK
                    if (table.getPrimaryKey() != null) {
                        // there is a PK
                        PrimaryKey pk = table.getPrimaryKey();
                        Node pkNode = dbService.createNode(DatabaseNodeType.PRIMARY_KEY);
                        pkNode.setProperty("fullName", pk.getFullName());
                        pkNode.setProperty("name", pk.getName());
                        if (pk.hasDefinition()) {
                            pkNode.setProperty("definition", pk.getDefinition());
                        }
                        if (pk.hasRemarks()) {
                            pkNode.setProperty("remarks", pk.getRemarks());
                        }
                        pkNode.setProperty("cardinality", pk.getCardinality());

                        // attach PK to table columns
                        for (final IndexColumn pkColumn : pk.getColumns()) {
                            Node targetColumnNode = dbService.findNode(DatabaseNodeType.TABLE_COLUMN, "fullName", pkColumn.getFullName());
                            Relationship pkPointsToColumn = pkNode.createRelationshipTo(targetColumnNode, SchemaRelationShips.PK_OF_COLUMN);
                        }
                    }
                    //table.getPrivileges()
                    //table.getTriggers();

                    // Put exported foreign keys
                    for (final ForeignKey fk : table.getExportedForeignKeys()) {
                        Node fkNode = dbService.createNode(DatabaseNodeType.FOREIGN_KEY);
                        fkNode.setProperty("fullName", fk.getFullName());
                        fkNode.setProperty("name", fk.getName());
                        if (fk.hasRemarks()) {
                            fkNode.setProperty("remarks", fk.getRemarks());
                        }

                        fkNode.setProperty("updateRuleId", fk.getUpdateRule().getId());
                        fkNode.setProperty("updateRuleName", fk.getUpdateRule().name());
                        fkNode.setProperty("updateRuleOrdinal", fk.getUpdateRule().ordinal());

                        fkNode.setProperty("deleteRuleId", fk.getDeleteRule().getId());
                        fkNode.setProperty("deleteRuleName", fk.getDeleteRule().name());
                        fkNode.setProperty("deleteRuleOrdinal", fk.getDeleteRule().ordinal());

                        fkNode.setProperty("deferrabilityName", fk.getDeferrability().name());

                        for (final ForeignKeyColumnReference fkRef : fk.getColumnReferences()) {
                            // get remote PK key and create relation
                            Node targetRefColumnNode = dbService.findNode(DatabaseNodeType.TABLE_COLUMN, "fullName", fkRef.getPrimaryKeyColumn().getFullName());
                            //Relationship foreignKeyColumnReference = targetRefColumnNode.createRelationshipTo(fkNode, SchemaRelationShips.IS_REFERENCED_BY);
                            Relationship foreignKeyColumnReference = fkNode.createRelationshipTo(targetRefColumnNode, SchemaRelationShips.REFERENCES);
                        }
                    }
                }
                tx.success();
            }
        }
    }

    public void putFKs(final Catalog catalog) {
        try (Transaction tx = getDbService().beginTx()) {
            for (final Schema schema : catalog.getSchemas()) {
                for (final Table table : catalog.getTables(schema)) {
                    int i = 0;
                    for (final ForeignKey fk : table.getForeignKeys()) {
                        // get the fkNode from the (existing) graph
                        //Node fkNode = getDbService().findNode(DatabaseNodeType.FOREIGN_KEY, "fullName", fk.getFullName());
                        Node fkNode;
                        if (getDbService().findNode(DatabaseNodeType.FOREIGN_KEY, "fullName", fk.getFullName()) == null) {
                            fkNode = dbService.createNode(DatabaseNodeType.FOREIGN_KEY);
                            fkNode.setProperty("fullName", fk.getFullName());
                        }
                        // now, the fkNode exists but is not linked to its table columns
                        // find the table column used by this fk

                        // Add fk is it does not yest exist
                        //getDbService().findNode(DatabaseNodeType.FOREIGN_KEY, "fullName", fk.getFullName());
                        //TODO : attach the fk to the table it belongs to (for easier queries)
                        /*
                         for (final ForeignKeyColumnReference fkReference : fk.getColumnReferences()){
                         //Node fkReferenceNode = getDbService().findNode(DatabaseNodeType.TABLE_COLUMN, "fullName", fkReference.getForeignKeyColumn().getFullName());
                         // make the relation
                         // be sure that the fk attaches the good table column
                         if(fkReference.getForeignKeyColumn().getParent().getFullName().equals(table.getFullName())){
                         Relationship fkRelation =  getDbService().findNode(DatabaseNodeType.TABLE_COLUMN, "fullName", fkReference.getForeignKeyColumn().getFullName()).createRelationshipTo(getDbService().findNode(DatabaseNodeType.FOREIGN_KEY, "fullName", fk.getFullName()), SchemaRelationShips.IS_COLUMN_OF_FK);
                         fkRelation.setProperty("nbItem", i);
                         fkRelation.setProperty("fkFullName", fk.getFullName());
                         fkRelation.setProperty("tableFullName", table.getFullName());
                         i++;    
                         }
                            
                            
                         }*/
                    }
                }
            }
            tx.success();
        }
    }

    public void attachColumnsToFk(final Catalog catalog) {
        try (Transaction tx = getDbService().beginTx()) {
            // get all columns
            for (final Schema schema : catalog.getSchemas()) {
                for (final Table table : catalog.getTables(schema)) {
                    for (final ForeignKey fk : table.getImportedForeignKeys()) {
                        //fk.
                        //get the node of the fk
                        dbService.findNode(DatabaseNodeType.FOREIGN_KEY, "fullName", fk.getFullName());
                        // get the node of the table
                        dbService.findNode(DatabaseNodeType.TABLE, "fullName", table.getFullName());
                        // attach FK to table
                        Relationship fkBelongsToTable = dbService.findNode(DatabaseNodeType.FOREIGN_KEY, "fullName", fk.getFullName()).createRelationshipTo(dbService.findNode(DatabaseNodeType.TABLE, "fullName", table.getFullName()), SchemaRelationShips.BELONGS_TO_TABLE);
                        // fetch the columns of the fk
                        for (final ForeignKeyColumnReference fkColRef : fk.getColumnReferences()) {
                            // get the node of the column
                            dbService.findNode(DatabaseNodeType.TABLE_COLUMN, "fullName", fkColRef.getForeignKeyColumn().getFullName());
                            // attach column to fk
                            Relationship rel = dbService.findNode(DatabaseNodeType.TABLE_COLUMN, "fullName", fkColRef.getForeignKeyColumn().getFullName()).createRelationshipTo(dbService.findNode(DatabaseNodeType.FOREIGN_KEY, "fullName", fk.getFullName()), SchemaRelationShips.IS_COLUMN_OF_FK);
                        }

                        //dbService.findNodes(DatabaseNodeType.TABLE_COLUMN, "fullName", fk.)
                    }
                }
            }
            tx.success();
        }
    }

    public void putSynonyms(final Catalog catalog) {
        try (Transaction tx = getDbService().beginTx()) {
            for (final Schema schema : catalog.getSchemas()) {
                
                for(final Synonym synonym : catalog.getSynonyms(schema)){
                    // add synonym to nodes
                    // feed node with datas
                    
                    // attach synonym to schema
                    
                    // attach synonym to database object (?)
                    //synonym.getReferencedObject().getClass();
                }
            }
            tx.success();
        }
    }

    // put sequences
    public void putSequences(final Catalog catalog) {
        try (Transaction tx = getDbService().beginTx()) {
            for (final Schema schema : catalog.getSchemas()) {

                for (final Sequence sequence : catalog.getSequences(schema)) {
                    Node seqNode = dbService.createNode(DatabaseNodeType.SEQUENCE);
                    seqNode.setProperty("fullName", sequence.getFullName());
                    seqNode.setProperty("increment", sequence.getIncrement());
                    seqNode.setProperty("lookupKey", sequence.getLookupKey());
                    seqNode.setProperty("maximumValue", sequence.getMaximumValue()+"");
                    seqNode.setProperty("minimumValue", sequence.getMinimumValue()+"");
                    seqNode.setProperty("name", sequence.getName());
                    if(sequence.getRemarks() != null){
                        seqNode.setProperty("remarks", sequence.getRemarks());
                    }
                    
                    seqNode.setProperty("isCycle", sequence.isCycle());
                    // Attach sequence to schema
                    //dbService.findNode(DatabaseNodeType.SCHEMA, "fullName", schema.getFullName());
                    Relationship belongsToSchema = seqNode.createRelationshipTo(dbService.findNode(DatabaseNodeType.SCHEMA, "fullName", schema.getFullName()), SchemaRelationShips.BELONGS_TO_SCHEMA);
                    // attach the sequence to a column (if applicable)
                }
            }
            tx.success();
        }
    }
    
    // put routines (should fail on pgsql jdbc driver)
    
    @Override
    public void executeOn(final Catalog catalog, final Connection connection)
            throws Exception {
        try (final PrintWriter writer = new PrintWriter(outputOptions.openNewOutputWriter());) {
            setOutputDir(additionalConfiguration.getStringValue("outputDir", "neo4j"));
            init();
            feedTables(catalog);
            putFKs(catalog);
            attachColumnsToFk(catalog);
            putSynonyms(catalog);
            putSequences(catalog);
        }
    }

    /**
     * @return the outputDir
     */
    public String getOutputDir() {
        return outputDir;
    }

    /**
     * @param outputDir the outputDir to set
     */
    public void setOutputDir(String outputDir) {
        this.outputDir = outputDir;
    }

    /**
     * @return the dbFactory
     */
    public GraphDatabaseFactory getDbFactory() {
        return dbFactory;
    }

    /**
     * @param dbFactory the dbFactory to set
     */
    public void setDbFactory(GraphDatabaseFactory dbFactory) {
        this.dbFactory = dbFactory;
    }

    /**
     * @return the dbService
     */
    public GraphDatabaseService getDbService() {
        return dbService;
    }

    /**
     * @param dbService the dbService to set
     */
    public void setDbService(GraphDatabaseService dbService) {
        this.dbService = dbService;
    }

}
