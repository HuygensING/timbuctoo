package nl.knaw.huygens.timbuctoo.tools.conversion;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;

import nl.knaw.huygens.timbuctoo.model.Person;
import nl.knaw.huygens.timbuctoo.storage.graph.IdGenerator;

import org.junit.Before;
import org.junit.Test;

import com.google.common.collect.Lists;

public class DomainEntityConverterTest {
  private static final String OLD_ID = "oldId";
  private static final Class<Person> TYPE = Person.class;
  private MongoConversionStorage mongoStorage;
  private IdGenerator idGenerator;
  private RevisionConverter revisionConverter;
  private DomainEntityConverter instance;

  @Before
  public void setup() {
    mongoStorage = mock(MongoConversionStorage.class);
    idGenerator = mock(IdGenerator.class);
    revisionConverter = mock(RevisionConverter.class);
    instance = new DomainEntityConverter(mongoStorage, idGenerator, revisionConverter);
  }

  @SuppressWarnings("unchecked")
  @Test
  public void convertRetrievesTheVersionsAndLetsTheVersionConverterHandleEachOne() throws Exception {
    // setup
    AllVersionVariationMap<Person> map = mock(AllVersionVariationMap.class);
    int revision1 = 1;
    int revision2 = 2;
    when(map.revisionsInOrder()).thenReturn(Lists.newArrayList(revision1, revision2));

    List<Person> variationsOfRevision1 = Lists.<Person> newArrayList(new Person());
    when(map.get(revision1)).thenReturn(variationsOfRevision1);

    List<Person> variationsOfRevision2 = Lists.<Person> newArrayList(new Person(), new Person());
    when(map.get(revision2)).thenReturn(variationsOfRevision2);

    when(mongoStorage.getAllVersionVariationsMapOf(TYPE, OLD_ID)).thenReturn(map);

    String newId = "newId";
    when(idGenerator.nextIdFor(TYPE)).thenReturn(newId);

    // action
    String actualNewId = instance.convert(TYPE, OLD_ID);

    // verify
    assertThat(actualNewId, is(newId));

    verify(revisionConverter).convert(OLD_ID, newId, variationsOfRevision1, revision1);
    verify(revisionConverter).convert(OLD_ID, newId, variationsOfRevision2, revision2);
  }

}
