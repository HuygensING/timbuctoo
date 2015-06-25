package nl.knaw.huygens.timbuctoo.tools.conversion;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import nl.knaw.huygens.timbuctoo.model.Relation;
import nl.knaw.huygens.timbuctoo.storage.StorageException;
import nl.knaw.huygens.timbuctoo.storage.StorageIterator;
import nl.knaw.huygens.timbuctoo.storage.StorageIteratorStub;

import org.junit.Test;

import com.google.common.collect.Lists;

public class RelationCollectionConverterTest {
  private static final String ID1 = "id1";
  private static final String ID2 = "id2";

  @Test
  public void converLetsTheRelationVersionConverterConvertEachRelation() throws Exception {
    // setup
    RelationConverter relationConverter = mock(RelationConverter.class);
    MongoConversionStorage mongoStorage = mock(MongoConversionStorage.class);

    Relation relation1 = aRelationWithId(ID1);
    Relation relation2 = aRelationWithId(ID2);

    storageReturnsAllRelations(mongoStorage, relation1, relation2);

    RelationCollectionConverter instance = new RelationCollectionConverter(relationConverter, mongoStorage);

    // action
    instance.convert();

    // verify
    verify(relationConverter).convert(ID1);
    verify(relationConverter).convert(ID2);
  }

  private void storageReturnsAllRelations(MongoConversionStorage mongoStorage, Relation relation1, Relation relation2) throws StorageException {
    StorageIterator<Relation> iterator = StorageIteratorStub.newInstance(Lists.newArrayList(relation1, relation2));
    when(mongoStorage.getDomainEntities(Relation.class)).thenReturn(iterator);
  }

  private Relation aRelationWithId(String id) {
    Relation relation = new Relation();
    relation.setId(id);
    return relation;
  }
}
