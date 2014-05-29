package nl.knaw.huygens.timbuctoo.tools.importer.dwcbia;

/*
 * #%L
 * Timbuctoo tools
 * =======
 * Copyright (C) 2012 - 2014 Huygens ING
 * =======
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as
 * published by the Free Software Foundation, either version 3 of the 
 * License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public 
 * License along with this program.  If not, see
 * <http://www.gnu.org/licenses/gpl-3.0.html>.
 * #L%
 */

import java.util.List;

import nl.knaw.huygens.timbuctoo.Repository;
import nl.knaw.huygens.timbuctoo.config.BasicInjectionModule;
import nl.knaw.huygens.timbuctoo.config.Configuration;
import nl.knaw.huygens.timbuctoo.index.IndexException;
import nl.knaw.huygens.timbuctoo.index.IndexManager;
import nl.knaw.huygens.timbuctoo.model.DomainEntity;
import nl.knaw.huygens.timbuctoo.model.Role;
import nl.knaw.huygens.timbuctoo.model.dwcbia.DWCPerson;
import nl.knaw.huygens.timbuctoo.model.dwcbia.DWCPlace;
import nl.knaw.huygens.timbuctoo.model.dwcbia.DWCScientist;
import nl.knaw.huygens.timbuctoo.model.util.Change;
import nl.knaw.huygens.timbuctoo.storage.StorageIterator;
import nl.knaw.huygens.timbuctoo.tools.importer.GenericImporter;
import nl.knaw.huygens.timbuctoo.tools.other.MongoAdmin;

import com.google.common.collect.Lists;
import com.google.inject.Guice;
import com.google.inject.Injector;

/**
 * Performs bulk import of test data, which is stored and indexed.
 * The data is imported from a database using properties files that contain connection settings, a query and an object mapping.
 */
public class BulkImporter {

  public static void main(String[] args) throws Exception {
    Change change = new Change("timbuctoo", "timbuctoo");

    Configuration config = new Configuration("config.xml");
    Injector injector = Guice.createInjector(new BasicInjectionModule(config));

    // TODO Remove non-persistent items only
    MongoAdmin admin = new MongoAdmin(config);
    admin.dropDatabase();

    Repository repository = null;
    IndexManager indexManager = null;

    try {

      repository = injector.getInstance(Repository.class);

      indexManager = injector.getInstance(IndexManager.class);
      indexManager.deleteAllEntities();

      GenericImporter importer = new GenericImporter(repository);

      long start = System.currentTimeMillis();

      System.out.println();
      System.out.println("---------------------------------");
      System.out.println("--  Pass 1 - basic properties  --");
      System.out.println("---------------------------------");

      String resourceDir = "src/main/resources/";
      importer.importData(resourceDir + "DWCPlaceMapping.properties", DWCPlace.class, null, change);

      List<Class<? extends Role>> allowedRoles = Lists.newArrayList();
      allowedRoles.add(DWCScientist.class);

      importer.importData(resourceDir + "DWCScientistMapping.properties", DWCPerson.class, allowedRoles, change);
      //importer.importData(resourceDir + "RAACivilServantMapping.properties", RAACivilServant.class);
      //CKCCPersonImporter csvImporter = new CKCCPersonImporter(storageManager);
      //csvImporter.handleFile(resourceDir + "testdata/ckcc-persons.txt", 9, false);

      System.out.println();
      System.out.println("--------------------------");
      System.out.println("--  Pass 2 - relations  --");
      System.out.println("--------------------------");

      // Nothing to do

      System.out.println();
      System.out.println("-------------------------");
      System.out.println("--  Pass 3 - indexing  --");
      System.out.println("-------------------------");
      System.out.println();

      indexEntities(repository, indexManager, DWCPlace.class);
      indexEntities(repository, indexManager, DWCPerson.class);
      //indexEntities(storageManager, indexManager, RAACivilServant.class);

      long time = (System.currentTimeMillis() - start) / 1000;
      System.out.printf("%n=== Used %d seconds%n%n", time);

    } finally {
      // Close resources
      if (indexManager != null) {
        indexManager.close();
      }
      if (repository != null) {
        repository.close();
      }
    }
  }

  private static <T extends DomainEntity> void indexEntities(Repository storageManager, IndexManager indexManager, Class<T> type) throws IndexException {
    System.out.println(".. " + type.getSimpleName());
    StorageIterator<T> iterator = null;
    try {
      iterator = storageManager.getEntities(type);
      while (iterator.hasNext()) {
        T entity = iterator.next();
        indexManager.addEntity(type, entity.getId());
      }
    } finally {
      if (iterator != null) {
        iterator.close();
      }
    }
  }

}
