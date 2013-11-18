package nl.knaw.huygens.timbuctoo.tools.importer.database;

import java.util.List;

import nl.knaw.huygens.timbuctoo.model.Role;
import nl.knaw.huygens.timbuctoo.model.dwcbia.DWCPerson;
import nl.knaw.huygens.timbuctoo.model.dwcbia.DWCPlace;
import nl.knaw.huygens.timbuctoo.model.dwcbia.DWCScientist;

import com.google.common.collect.Lists;

/**
 * Extracts data from database and transforms it to json-files.
 * @author martijnm
 */
public class BulkDataTransformer {

  public static void main(String[] args) throws Exception {
    GenericDataHandler importer = new GenericJsonFileWriter("src/main/resources/testdata/");

    long start = System.currentTimeMillis();
    String resourceDir = "src/main/resources/";
    importer.importData(resourceDir + "DWCPlaceMapping.properties", DWCPlace.class, null);
    List<Class<? extends Role>> allowedRoles = Lists.newArrayList();
    allowedRoles.add(DWCScientist.class);

    importer.importData(resourceDir + "DWCScientistMapping.properties", DWCPerson.class, allowedRoles);
    //importer.importData(resourceDir + "RAACivilServantMapping.properties", RAACivilServant.class, null);

    long time = (System.currentTimeMillis() - start) / 1000;
    System.out.printf("%n=== Transform took %d seconds", time);
  }

}
