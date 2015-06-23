package nl.knaw.huygens.timbuctoo.tools.conversion;

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

    Runnable converter1 = createConverterFor(TYPE, ID1);
    Runnable converter2 = createConverterFor(TYPE, ID2);

    StorageIterator<Person> iterator = StorageIteratorStub.newInstance(person1, person2);
    when(mongoStorage.getDomainEntities(TYPE)).thenReturn(iterator);

    // action
    instance.convert();

    // verify
    verify(converter1).run();
    verify(converter2).run();

  }

  @SuppressWarnings("unchecked")
  private Runnable createConverterFor(Class<Person> type, String id) {
    Runnable runnable = mock(Runnable.class);
    when(entityConverterFactory.createConverterRunnable(type, id)).thenReturn(runnable);
    return runnable;
  }

  private Person createPersonWithId(String id1) {
    Person person1 = new Person();
    person1.setId(id1);
    return person1;
  }
}
