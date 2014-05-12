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

import nl.knaw.huygens.timbuctoo.model.Role;
import nl.knaw.huygens.timbuctoo.model.dwcbia.DWCPerson;
import nl.knaw.huygens.timbuctoo.model.dwcbia.DWCPlace;
import nl.knaw.huygens.timbuctoo.model.dwcbia.DWCScientist;
import nl.knaw.huygens.timbuctoo.model.util.Change;
import nl.knaw.huygens.timbuctoo.tools.importer.GenericDataHandler;
import nl.knaw.huygens.timbuctoo.tools.importer.GenericJsonFileWriter;

import com.google.common.collect.Lists;

/**
 * Extracts data from database and transforms it to json-files.
 * The data is imported from a database using properties files that contain connection settings, a query and an object mapping.
 * @author martijnm
 */
public class BulkDataTransformer {

  public static void main(String[] args) throws Exception {
    Change change = new Change("timbuctoo", "timbuctoo");

    String resourceDir = args[0] + "/";

    GenericDataHandler importer = new GenericJsonFileWriter(resourceDir + "testdata/");

    long start = System.currentTimeMillis();
    importer.importData(resourceDir + "DWCPlaceMapping.properties", DWCPlace.class, null, change);
    List<Class<? extends Role>> allowedRoles = Lists.newArrayList();
    allowedRoles.add(DWCScientist.class);

    importer.importData(resourceDir + "DWCScientistMapping.properties", DWCPerson.class, allowedRoles, change);
    //importer.importData(resourceDir + "RAACivilServantMapping.properties", RAACivilServant.class, null);

    long time = (System.currentTimeMillis() - start) / 1000;
    System.out.printf("%n=== Transform took %d seconds", time);
  }

}
