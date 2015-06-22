package nl.knaw.huygens.timbuctoo.tools.conversion;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;

import nl.knaw.huygens.timbuctoo.model.DomainEntity;
import nl.knaw.huygens.timbuctoo.model.Person;
import nl.knaw.huygens.timbuctoo.storage.StorageException;
import nl.knaw.huygens.timbuctoo.storage.graph.IdGenerator;
import nl.knaw.huygens.timbuctoo.storage.graph.SystemRelationType;
import nl.knaw.huygens.timbuctoo.storage.graph.tinkerpop.VertexDuplicator;

import org.junit.Before;
import org.junit.Test;

import com.google.common.collect.Lists;
import com.tinkerpop.blueprints.Vertex;

public class DomainEntityConverterTest {
  private static final String NEW_ID = "newId";
  private static final String OLD_ID = "oldId";
  private static final Class<Person> TYPE = Person.class;
  private MongoConversionStorage mongoStorage;
  private IdGenerator idGenerator;
  private RevisionConverter revisionConverter;
  private DomainEntityConverter instance;
  private int revision1;
  private int revision2;
  private List<Person> variationsOfRevision1;
  private List<Person> variationsOfRevision2;
  private AllVersionVariationMap<Person> map;
  private VertexDuplicator vertexDuplicator;
  private Vertex vertexRev1;
  private Vertex vertexRev2;

  @Before
  public void setup() throws StorageException, IllegalAccessException {
    mongoStorage = mock(MongoConversionStorage.class);
    idGenerator = mock(IdGenerator.class);
    revisionConverter = mock(RevisionConverter.class);
    vertexDuplicator = mock(VertexDuplicator.class);
    instance = new DomainEntityConverter(mongoStorage, idGenerator, revisionConverter, vertexDuplicator);

    setupRevisionVariationMap();
    setupIdGenerator();
    setupRevisionConverter();
  }

  @SuppressWarnings("unchecked")
  private void setupRevisionVariationMap() throws StorageException {
    map = mock(AllVersionVariationMap.class);
    revision1 = 1;
    revision2 = 2;
    when(map.revisionsInOrder()).thenReturn(Lists.newArrayList(revision1, revision2));

    variationsOfRevision1 = Lists.<Person> newArrayList(new Person());
    when(map.get(revision1)).thenReturn(variationsOfRevision1);

    variationsOfRevision2 = Lists.<Person> newArrayList(new Person(), new Person());
    when(map.get(revision2)).thenReturn(variationsOfRevision2);

    when(mongoStorage.getAllVersionVariationsMapOf(TYPE, OLD_ID)).thenReturn(map);
  }

  private void setupIdGenerator() {
    when(idGenerator.nextIdFor(TYPE)).thenReturn(NEW_ID);
  }

  private void setupRevisionConverter() throws IllegalAccessException, StorageException {
    vertexRev1 = mock(Vertex.class);
    when(revisionConverter.convert(OLD_ID, NEW_ID, variationsOfRevision1, revision1)).thenReturn(vertexRev1);
    vertexRev2 = mock(Vertex.class);
    when(vertexRev2.getProperty(DomainEntity.PID)).thenReturn("pid");
    when(revisionConverter.convert(OLD_ID, NEW_ID, variationsOfRevision2, revision2)).thenReturn(vertexRev2);
  }

  @Test
  public void convertRetrievesTheVersionsAndLetsTheVersionConverterHandleEachOne() throws Exception {
    String actualNewId = instance.convert(TYPE, OLD_ID);

    // verify
    assertThat(actualNewId, is(NEW_ID));

    verify(revisionConverter).convert(OLD_ID, NEW_ID, variationsOfRevision1, revision1);
    verify(revisionConverter).convert(OLD_ID, NEW_ID, variationsOfRevision2, revision2);
  }

  @Test
  public void convertLinksTheVersionsAndDuplicatesTheLatestNode() throws IllegalArgumentException, IllegalAccessException, StorageException {
    // action
    instance.convert(TYPE, OLD_ID);

    // verify
    verify(vertexRev1).addEdge(SystemRelationType.VERSION_OF.name(), vertexRev2);
    verify(vertexDuplicator).duplicate(vertexRev2);
  }

}
