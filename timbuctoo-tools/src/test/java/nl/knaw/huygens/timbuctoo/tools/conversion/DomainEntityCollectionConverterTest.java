package nl.knaw.huygens.timbuctoo.tools.conversion;

/*
 * #%L
 * Timbuctoo tools
 * =======
 * Copyright (C) 2012 - 2015 Huygens ING
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

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import nl.knaw.huygens.timbuctoo.model.Person;
import nl.knaw.huygens.timbuctoo.storage.StorageIterator;
import nl.knaw.huygens.timbuctoo.storage.StorageIteratorStub;

import org.junit.Before;
import org.junit.Test;

import com.tinkerpop.blueprints.Graph;

public class DomainEntityCollectionConverterTest {

  private static final String ID1 = "id1";
  private static final String ID2 = "id2";
  private static final Class<Person> TYPE = Person.class;
  private MongoConversionStorage mongoStorage;
  private DomainEntityCollectionConverter<Person> instance;
  private DomainEntityConverterFactory entityConverterFactory;

  @Before
  public void setup() {
    mongoStorage = mock(MongoConversionStorage.class);
    entityConverterFactory = mock(DomainEntityConverterFactory.class);
    instance = new DomainEntityCollectionConverter<Person>(TYPE, mongoStorage, entityConverterFactory, mock(Graph.class));
  }

  @Test
  public void convertCreatesAJobForEachEntity() throws Exception {
    // setup
    Person person1 = createPersonWithId(ID1);
    Person person2 = createPersonWithId(ID2);

    DomainEntityConverter<Person> converter1 = createConverterFor(TYPE, ID1);
    DomainEntityConverter<Person> converter2 = createConverterFor(TYPE, ID2);

    StorageIterator<Person> iterator = StorageIteratorStub.newInstance(person1, person2);
    when(mongoStorage.getDomainEntities(TYPE)).thenReturn(iterator);

    // action
    instance.convert();

    // verify
    verify(converter1).convert();
    verify(converter2).convert();

  }

  @SuppressWarnings("unchecked")
  private DomainEntityConverter<Person> createConverterFor(Class<Person> type, String id) {
    DomainEntityConverter<Person> converter = mock(DomainEntityConverter.class);
    when(entityConverterFactory.create(type, id)).thenReturn(converter);
    return converter;
  }

  private Person createPersonWithId(String id1) {
    Person person1 = new Person();
    person1.setId(id1);
    return person1;
  }
}
