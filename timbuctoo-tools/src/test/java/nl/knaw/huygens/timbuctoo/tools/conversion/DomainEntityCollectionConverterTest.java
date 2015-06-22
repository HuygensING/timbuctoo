package nl.knaw.huygens.timbuctoo.tools.conversion;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Map;

import nl.knaw.huygens.timbuctoo.model.Person;
import nl.knaw.huygens.timbuctoo.storage.StorageIterator;
import nl.knaw.huygens.timbuctoo.storage.StorageIteratorStub;
import nl.knaw.huygens.timbuctoo.storage.graph.IdGenerator;

import org.junit.Before;
import org.junit.Test;

import com.google.common.collect.Maps;

public class DomainEntityCollectionConverterTest {

  private static final String ID1 = "id1";
  private static final String ID2 = "id2";
  private static final String NEW_ID2 = "newId2";
  private static final String NEW_ID1 = "newId1";
  private static final Class<Person> TYPE = Person.class;
  private IdGenerator idGenerator;
  private MongoConversionStorage mongoStorage;
  private Map<String, String> oldIdNewIdMap;
  private DomainEntityConverter entityConverter;
  private DomainEntityCollectionConverter<Person> instance;

  @Before
  public void setup() {
    idGenerator = new IdGenerator();
    mongoStorage = mock(MongoConversionStorage.class);
    oldIdNewIdMap = Maps.newHashMap();
    entityConverter = mock(DomainEntityConverter.class);
    instance = new DomainEntityCollectionConverter<Person>(TYPE, idGenerator, mongoStorage, oldIdNewIdMap, entityConverter);
  }

  @Test
  public void convertConvertsAllTheDomainEntitiesOfACertainType() throws Exception {
    Person person1 = createPersonWithId(ID1);

    Person person2 = createPersonWithId(ID2);

    StorageIterator<Person> iterator = StorageIteratorStub.newInstance(person1, person2);
    when(mongoStorage.getDomainEntities(TYPE)).thenReturn(iterator);

    when(entityConverter.convert(TYPE, ID1)).thenReturn(NEW_ID1);
    when(entityConverter.convert(TYPE, ID2)).thenReturn(NEW_ID2);

    // action
    instance.convert();

    // verify
    assertThat(oldIdNewIdMap.keySet(), containsInAnyOrder(ID1, ID2));
    assertThat(oldIdNewIdMap.get(ID1), is(NEW_ID1));
    assertThat(oldIdNewIdMap.get(ID2), is(NEW_ID2));
  }

  private Person createPersonWithId(String id1) {
    Person person1 = new Person();
    person1.setId(id1);
    return person1;
  }
}
