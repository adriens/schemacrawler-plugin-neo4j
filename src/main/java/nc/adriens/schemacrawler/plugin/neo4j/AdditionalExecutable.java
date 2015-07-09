package nc.adriens.schemacrawler.plugin.neo4j;
/*
 http://www.tutorialspoint.com/neo4j/neo4j_native_java_api_example.htm
 http://www.sportsstandards.org/oc

 Search nodes :

 http://stackoverflow.com/questions/15368579/select-a-node-by-name-in-neo4j-in-java

 schemacrawler -host=localhost -port=5432 -database=sportsdb -user=sports_adm -password=user_adm  -schemas=sports_adm -c=neo4j -infolevel=maximum -server=postgresql -loglevel=CONFIG

 MATCH (n)
 RETURN n;

 MATCH (a)-[r:CONTAINS_SCHEMA]->(b)
 RETURN r


 */

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;

import schemacrawler.schema.Catalog;
import schemacrawler.schema.Column;
import schemacrawler.schema.Schema;
import schemacrawler.schema.Table;
import schemacrawler.tools.executable.BaseStagedExecutable;

public class AdditionalExecutable extends BaseStagedExecutable {

    static final String COMMAND = "neo4j";

    private String outputFilename;
    private FileWriter cypherWriter;

    protected AdditionalExecutable() {
        super(COMMAND);
    }

    public void init() throws IOException {
        // recursively delete directory contents
        System.out.println("Output cypher file : <" + getOutputFilename() + "> ...");
        File cypherFile = new File(getOutputFilename());
        cypherWriter = new FileWriter(cypherFile.getAbsoluteFile());
        cypherWriter.write("// Schemacrawler neo4j cyphe commands\n");
        cypherWriter.flush();
    }

    public void feedSchemas(final Catalog catalog) throws IOException {
        for (final Schema schema : catalog.getSchemas()) {
            System.out.println("Processing schema <" + schema.getName() + ">");
            cypherWriter.write("CREATE (" + schema + ":SCHEMA{fullName:'" + schema.getFullName() + "', lookupKey:'" + schema.getLookupKey() + "', name:'" + schema.getName() + "', remarks:'" + schema.getRemarks() + "'})\n");
            cypherWriter.flush();
        }
    }

    public void feedTables(final Catalog catalog) throws IOException {
        String lCypher = "";
        String lFullTablename = "";
        String lFullColumnName = "";
        for (final Schema schema : catalog.getSchemas()) {
            for (final Table table : catalog.getTables(schema)) {
                //cypherWriter.write("CREATE (" + schema +":SCHEMA{fullName:'" + schema.getFullName() + "', lookupKey:'" + schema.getLookupKey()+ "', name:'" + schema.getName() + "', remarks:'" + schema.getRemarks() + "'})\n");
                System.out.println("Processing table <" + table.getName() + ">");
                lFullTablename = table.getFullName().replace(".", "_");
                lCypher = "CREATE (" + lFullTablename + ":TABLE{";
                lCypher += "fullTableName:'" + lFullTablename + "'";
                lCypher += ", nbColumns:" + table.getColumns().size();
                if (lCypher != null) {
                    if (lCypher.length() > 0) {
                        lCypher += ", definition:'" + table.getDefinition() + "'";
                    }
                }

                if (table.getLookupKey() != null) {
                    if (table.getLookupKey().length() > 0) {
                        lCypher += ", lookupKey:'" + table.getLookupKey() + "'";
                    }
                }

                lCypher += ", tableName:'" + table.getName() + "'";

                if (table.getRemarks() != null) {
                    if (table.getRemarks().length() > 0) {
                        lCypher += ", remarks:'" + table.getRemarks() + "'";
                    }
                }

                lCypher += ", schemaName:'" + table.getSchema().getName() + "'";
                lCypher += ", schemaFullName:'" + table.getSchema().getFullName() + "'";
                lCypher += ", tableType:'" + table.getTableType().toString() + "'";
                
                
                    // tableType:'" + table.getTableType().toString() + "'}");
                lCypher += "})\n";
                cypherWriter.write(lCypher);
                //cypherWriter.write("CREATE (" + table.getFullName() + ":TABLE{nbColumns:" + table.getColumns().size() + ", definition:'" + table.getDefinition()+ "', lookupKey:'" + table.getLookupKey() + "', name:'" + table.getName() + "', remarks:'" + table.getRemarks() + "', schemaname:'" + table.getSchema().getName() + "', schemaFullName:'" + table.getSchema().getFullName() + "', tableType:'" + table.getTableType().toString() + "'}");
                cypherWriter.flush();

                //writer.println("o--> " + table);
                for (final Column column : table.getColumns()) {
                    lFullColumnName = column.getFullName().replace(".", "_");
                    lFullColumnName = lFullColumnName.replace("\"", "");
                    System.out.println("Putting column nodes of <" + table.getFullName() + "> table...");
                    // Create the node
                    lCypher = "CREATE ( " + lFullColumnName + ":COLUMN{name:'" + column.getName() + "', fullParentTableName:'" + lFullTablename + "'})\n";
                    //lCypher += "RETURN p\n";
                    cypherWriter.write(lCypher);
                    cypherWriter.flush();
                    // colum created.
                    // Create the relation bewteen column and parent table
                    //lCypher = "\n\nWITH " + lFullColumnName + ", " + lFullTablename + "\n";
                    //lCypher +="\n\n\nMATCH (a:TABLE),(b:COLUMN)\n";
                    //lCypher += "WHERE a.fullTableName = '" + lFullTablename + "' and b.fullParentTableName = '" + lFullTablename + "'\n";
                    //lCypher += "CREATE (a)-[r:IS_COLUMN_OF { name : a.fullTableName + '<->' + b.fullParentTableName}]->(b)\n";
                    //lCypher += "CREATE (b)-[r:IS_COLUMN_OF { name : a.fullTableName + '<->' + b.fullParentTableName}]->(a)\n\n";
                    //lCypher += "RETURN r\n";
                    //cypherWriter.write(lCypher);
                    //cypherWriter.flush();
                    
                }

            }

        }
    }

    @Override
    public void executeOn(final Catalog catalog, final Connection connection)
            throws Exception {
        try (final PrintWriter writer = new PrintWriter(outputOptions.openNewOutputWriter());) {
            setOutputFilename(additionalConfiguration.getStringValue("outputFilename", "neo4j.cypher"));
            init();
            feedSchemas(catalog);
            feedTables(catalog);
            //cypherWriter.write("MATCH (n) RETURN n\n");
            cypherWriter.flush();
            cypherWriter.close();

        }
    }

    /**
     * @return the outputFilename
     */
    public String getOutputFilename() {
        return outputFilename;
    }

    /**
     * @param outputFilename the outputFilename to set
     */
    public void setOutputFilename(String outputFilename) {
        this.outputFilename = outputFilename;
    }

    /**
     * @return the cypherWriter
     */
    public FileWriter getCypherWriter() {
        return cypherWriter;
    }

    /**
     * @param cypherWriter the cypherWriter to set
     */
    public void setCypherWriter(FileWriter cypherWriter) {
        this.cypherWriter = cypherWriter;
    }

}
