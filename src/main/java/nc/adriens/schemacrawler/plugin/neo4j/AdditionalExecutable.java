package nc.adriens.schemacrawler.plugin.neo4j;
/*
schemacrawler -host=localhost -port=5432 -database=sportsdb -user=sc -password=sc  -schemas=public -c=neo4j -infolevel=maximum -server=postgresql -loglevel=CONFIG -outputDir=c:/tmp
*/

import java.io.File;
import java.io.PrintWriter;
import java.sql.Connection;
import org.apache.commons.io.FileUtils;

import schemacrawler.schema.Catalog;
import schemacrawler.schema.Column;
import schemacrawler.schema.Schema;
import schemacrawler.schema.Table;
import schemacrawler.tools.executable.BaseStagedExecutable;


public class AdditionalExecutable extends BaseStagedExecutable
{

  static final String COMMAND = "neo4j";

  private String outputDir;
  
  protected AdditionalExecutable()
  {
    super(COMMAND);
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
    
  @Override
  public void executeOn(final Catalog catalog, final Connection connection)
    throws Exception
  {
    try (final PrintWriter writer = new PrintWriter(outputOptions.openNewOutputWriter());)
    {
        System.out.println("NEO4J plugin !!!");
        System.out.println("NEO4J output dir : " + additionalConfiguration.getStringValue("outputDir", "neo4j"));
        setOutputDir(additionalConfiguration.getStringValue("outputDir", "neo4j"));
        // recursively delete directory contents
        System.out.println("Deleting directory <" + getOutputDir() + "> ...");
        FileUtils.deleteDirectory(new File(getOutputDir()));
        System.out.println("Output directory cleaned");
      for (final Schema schema: catalog.getSchemas())
      {
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

    

}
