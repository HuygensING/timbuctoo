package nl.knaw.huygens.timbuctoo.tools.importer;

import java.util.List;

import nl.knaw.huygens.timbuctoo.config.Configuration;
import nl.knaw.huygens.timbuctoo.config.TypeRegistry;
import nl.knaw.huygens.timbuctoo.model.Role;
import nl.knaw.huygens.timbuctoo.model.dwcbia.DWCPerson;
import nl.knaw.huygens.timbuctoo.model.dwcbia.DWCPlace;
import nl.knaw.huygens.timbuctoo.model.dwcbia.DWCScientist;
import nl.knaw.huygens.timbuctoo.tools.config.ToolsInjectionModule;

import com.google.common.collect.Lists;
import com.google.inject.Guice;
import com.google.inject.Injector;

/**
 * Extracts data from database and transforms it to json-files.
 * The data is imported from a database using properties files that contain connection settings, a query and an object mapping.
 * @author martijnm
 */
public class BulkDataTransformer {

  public static void main(String[] args) throws Exception {
    Configuration config = new Configuration("config.xml");
    Injector injector = Guice.createInjector(new ToolsInjectionModule(config));

    GenericDataHandler importer = new GenericJsonFileWriter("src/main/resources/testdata/", injector.getInstance(TypeRegistry.class));

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
