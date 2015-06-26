package nl.knaw.huygens.timbuctoo.tools.conversion;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Map;

import nl.knaw.huygens.timbuctoo.model.Relation;
import nl.knaw.huygens.timbuctoo.storage.StorageException;
import nl.knaw.huygens.timbuctoo.storage.graph.IdGenerator;

import org.junit.Before;
import org.junit.Test;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

public class RelationConverterTest {
  private static final int REVISION2 = 2;
  private static final int REVISION1 = 1;
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
  private Map<String, String> oldIdNewIdMap;

  @Before
  public void setup() throws StorageException {
    oldIdNewIdMap = Maps.newHashMap();
    revisionConverter = mock(RelationRevisionConverter.class);
    mongoStorage = mock(MongoConversionStorage.class);
    setupIdGenerator();

    instance = new RelationConverter(mongoStorage, revisionConverter, idGenerator, oldIdNewIdMap);

    setupRevisionVariationMap();
  }

  @SuppressWarnings("unchecked")
  private void setupRevisionVariationMap() throws StorageException {
    map = mock(AllVersionVariationMap.class);
    when(map.revisionsInOrder()).thenReturn(Lists.newArrayList(1, 2));

    variationsOfRevision1 = Lists.newArrayList(new Relation());
    when(map.get(REVISION1)).thenReturn(variationsOfRevision1);

    variationsOfRevision2 = Lists.newArrayList(new Relation(), new Relation());
    when(map.get(REVISION2)).thenReturn(variationsOfRevision2);

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
    verify(revisionConverter).convert(OLD_ID, NEW_ID, variationsOfRevision1, REVISION1);
    verify(revisionConverter).convert(OLD_ID, NEW_ID, variationsOfRevision2, REVISION2);

    verifyTheRelationIdsAreMapped();

  }

  private void verifyTheRelationIdsAreMapped() {
    assertThat(oldIdNewIdMap.keySet(), contains(OLD_ID));
    assertThat(oldIdNewIdMap.get(OLD_ID), is(NEW_ID));
  }

}
