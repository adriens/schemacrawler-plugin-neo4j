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
import schemacrawler.schema.Index;
import schemacrawler.schema.IndexColumn;
import schemacrawler.schema.Schema;
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
                schemaNode.setProperty("Name", schema.getName());
                schemaNode.setProperty("FullName", schema.getFullName());
                schemaNode.setProperty("LookupKey", schema.getLookupKey());
                schemaNode.setProperty("Remarks", schema.getRemarks());
                
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
                    tableNode.setProperty("tableFullname", table.getFullName());
                    tableNode.setProperty("tableType", table.getTableType().toString());
                    table.getDefinition();
                    
                    // attach the table to its schema
                    Relationship schemaRelationShip = tableNode.createRelationshipTo(schemaNode, SchemaRelationShips.BELONGS_TO_SCHEMA);
                    
                    //writer.println("o--> " + table);
                    for (final Column column : table.getColumns()) {
                        Node columnNode = dbService.createNode(DatabaseNodeType.TABLE_COLUMN);
                        columnNode.setProperty("columnOrdinalPosition", column.getOrdinalPosition());
                        columnNode.setProperty("columnDataType", column.getColumnDataType().toString());
                        columnNode.setProperty("name", column.getName() );
                        columnNode.setProperty("fullName", column.getFullName());
                        if(column.getDefaultValue() != null){
                         columnNode.setProperty("defaultValue", column.getDefaultValue());   
                        }

                        if(column.getLookupKey() != null){
                        columnNode.setProperty("lookupKey", column.getLookupKey());
                        }
                        
                        if(column.getRemarks() != null){
                            columnNode.setProperty("remarks", column.getRemarks());
                        }

                        if(column.getShortName() != null){
                        columnNode.setProperty("shortName", column.getShortName());
                        }
                        columnNode.setProperty("size", column.getSize());
                        columnNode.setProperty("width", column.getWidth());

                        Relationship relationship = columnNode.createRelationshipTo(tableNode, SchemaRelationShips.IS_COLUMN_OF_TABLE);
                    }
                    // end of columns
                    for (final Index index : table.getIndices()) {
                        Node indexNode = dbService.createNode(DatabaseNodeType.INDEX);
                        indexNode.setProperty("schema", index.getSchema().getName());
                        indexNode.setProperty("name", index.getName());
                        indexNode.setProperty("shortName", index.getShortName());
                        indexNode.setProperty("fullName", index.getFullName());
                        indexNode.setProperty("cardinality", index.getCardinality());
                        indexNode.setProperty("indexPages", index.getPages());
                        indexNode.setProperty("definition", index.getDefinition());
                        
                        indexNode.setProperty("indexTypeName", index.getIndexType().name());
                        indexNode.setProperty("indexTypeId", index.getIndexType().getId());
                        indexNode.setProperty("indexTypeOrdinal", index.getIndexType().ordinal());
                        
                        //indexNode.setProperty("lookupKey", index.getLookupKey());
                        //indexNode.setProperty("remarks", index.getRemarks());
                        
                        
                        // attach index to table
                        Relationship indexBelongsToTable = indexNode.createRelationshipTo(tableNode, SchemaRelationShips.IS_INDEX_OF_TABLE);
                        indexBelongsToTable.setProperty("cardinality", index.getCardinality());
                        // attach index to table columns
                        for (final IndexColumn indexColumn : index.getColumns()) {
                            //indexColumn.getFullName()
                            // Attach index to table column
                            // get the target column to attach by its fullname
                            Node targetColumnNode = dbService.findNode(DatabaseNodeType.TABLE_COLUMN, "fullName", indexColumn.getFullName());
                            Relationship indexPointsToColumns = indexNode.createRelationshipTo(targetColumnNode, SchemaRelationShips.INDEXES_COLUMN);
                        }
                        
                    }
                    //table.getIndices()
                    //table.getPrimaryKey()
                    //table.getPrivileges()
                    //table.getTriggers();
                    
                }
                tx.success();
            }
        }
    }

    @Override
    public void executeOn(final Catalog catalog, final Connection connection)
            throws Exception {
        try (final PrintWriter writer = new PrintWriter(outputOptions.openNewOutputWriter());) {
            setOutputDir(additionalConfiguration.getStringValue("outputDir", "neo4j"));
            init();
            feedTables(catalog);

            for (final Schema schema : catalog.getSchemas()) {
//        System.out.println(schema);
//        for (final Table table: catalog.getTables(schema))
//        {
//          writer.println("o--> " + table);
//          for (final Column column: table.getColumns())
//          {
//            writer.println("     o--> " + column);
//          }
                //}
            }
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
