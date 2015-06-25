package nl.knaw.huygens.timbuctoo.tools.conversion;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;

import nl.knaw.huygens.timbuctoo.model.Relation;
import nl.knaw.huygens.timbuctoo.storage.StorageException;
import nl.knaw.huygens.timbuctoo.storage.graph.IdGenerator;

import org.junit.Before;
import org.junit.Test;

import com.google.common.collect.Lists;

public class RelationConverterTest {
  private static final Class<Relation> TYPE = Relation.class;
  private static final String OLD_ID = "id";
  private static final String NEW_ID = "newId";
  private AllVersionVariationMap<Relation> map;
  private List<Relation> variationsOfRevision1;
  private List<Relation> variationsOfRevision2;
  private MongoConversionStorage mongoStorage;
  private RelationRevisionConverter revisionConverter;
  private RelationConverter instance;
  private IdGenerator idGenerator;

  @Before
  public void setup() throws StorageException {
    revisionConverter = mock(RelationRevisionConverter.class);
    mongoStorage = mock(MongoConversionStorage.class);
    setupIdGenerator();

    instance = new RelationConverter(mongoStorage, revisionConverter, idGenerator);

    setupRevisionVariationMap();
  }

  @SuppressWarnings("unchecked")
  private void setupRevisionVariationMap() throws StorageException {
    map = mock(AllVersionVariationMap.class);
    when(map.revisionsInOrder()).thenReturn(Lists.newArrayList(1, 2));

    variationsOfRevision1 = Lists.newArrayList(new Relation());
    when(map.get(1)).thenReturn(variationsOfRevision1);

    variationsOfRevision2 = Lists.newArrayList(new Relation(), new Relation());
    when(map.get(2)).thenReturn(variationsOfRevision2);

    when(mongoStorage.getAllVersionVariationsMapOf(TYPE, OLD_ID)).thenReturn(map);
  }

  private void setupIdGenerator() {
    idGenerator = mock(IdGenerator.class);
    when(idGenerator.nextIdFor(TYPE)).thenReturn(NEW_ID);
  }

  @Test
  public void convertRetrievesAllTheVersionsAndLetsTheRevisionConverterHandleThem() throws Exception {

    // action
    instance.convert(OLD_ID);

    // verify
    verify(revisionConverter).convert(OLD_ID, NEW_ID, variationsOfRevision1);
    verify(revisionConverter).convert(OLD_ID, NEW_ID, variationsOfRevision2);

  }

}
