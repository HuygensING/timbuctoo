package nl.knaw.huygens.timbuctoo.tools.other;

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

import nl.knaw.huygens.timbuctoo.Repository;
import nl.knaw.huygens.timbuctoo.model.Person;
import nl.knaw.huygens.timbuctoo.storage.StorageIterator;
import nl.knaw.huygens.timbuctoo.tools.config.ToolsInjectionModule;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.inject.Injector;

public class PersonAnalyzer {

  public static void main(String[] args) throws Exception {
    Injector injector = ToolsInjectionModule.createInjector();
    Repository repository = injector.getInstance(Repository.class);
    ObjectMapper mapper = new ObjectMapper();
    try {
      StorageIterator<Person> iterator = repository.getDomainEntities(Person.class);
      while (iterator.hasNext()) {
        Person person = iterator.next();
        System.out.println(mapper.writeValueAsString(person));
      }
      iterator.close();
    } finally {
      if (repository != null) {
        repository.close();
      }
      // TODO close index manager, even if it's not used...
    }
  }

}
