package nl.knaw.huygens.timbuctoo.tools.importer.database;

import nl.knaw.huygens.timbuctoo.model.dwcbia.DWCPlace;
import nl.knaw.huygens.timbuctoo.model.dwcbia.DWCScientist;
import nl.knaw.huygens.timbuctoo.model.raa.RAACivilServant;

/**
 * Extracts data from database and transforms it to json-files.
 * @author martijnm
 *
 */
public class BulkDataTransformer {
  public static void main(String[] args) throws Exception {

    GenericDataHandler importer = new GenericJsonFileWriter();

    long start = System.currentTimeMillis();
    importer.importData("resources/DWCPlaceMapping.properties", DWCPlace.class);
    importer.importData("resources/DWCScientistMapping.properties", DWCScientist.class);
    importer.importData("resources/RAACivilServantMapping.properties", RAACivilServant.class);

    long time = (System.currentTimeMillis() - start) / 1000;
    System.out.printf("%n=== Transform took %d seconds", time);
  }
}
