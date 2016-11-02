package nl.knaw.huygens.timbuctoo.database;

import nl.knaw.huygens.timbuctoo.crud.NotFoundException;
import nl.knaw.huygens.timbuctoo.database.dto.DataStream;
import nl.knaw.huygens.timbuctoo.database.dto.ReadEntity;
import nl.knaw.huygens.timbuctoo.database.dto.dataset.Collection;
import nl.knaw.huygens.timbuctoo.model.vre.Vres;
import org.junit.Before;
import org.junit.Test;

import java.util.List;
import java.util.UUID;
import java.util.function.Function;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.sameInstance;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class TimbuctooActionsGetTest {

  private static final UUID ID = UUID.randomUUID();
  private static final Integer rev = 1;
  private CustomEntityProperties entityProps;
  private CustomRelationProperties relationProps;
  private TimbuctooActions instance;
  private Collection collection;
  private boolean withRelations = false;
  private DataStoreOperations dataStoreOperations;

  @Before
  public void setUp() throws Exception {
    entityProps = mock(CustomEntityProperties.class);
    relationProps = mock(CustomRelationProperties.class);
    dataStoreOperations = mock(DataStoreOperations.class);
    instance = new TimbuctooActions(null, null, null, dataStoreOperations, null);
    collection = mock(Collection.class);
  }

  @Test(expected = NotFoundException.class)
  public void getEntityThrowsANotFoundExceptionWhenTheEntityCannotBeFound() throws Exception {
    when(dataStoreOperations.getEntity(ID, rev, collection, entityProps, relationProps))
      .thenThrow(new NotFoundException());

    instance.getEntity(collection, ID, rev, entityProps, relationProps);
  }

  @Test
  public void getEntityReturnsTheReadEntity() throws Exception {
    ReadEntity readEntity = mock(ReadEntity.class);
    when(dataStoreOperations.getEntity(ID, rev, collection, entityProps, relationProps)).thenReturn(readEntity);

    ReadEntity actualEntity = instance.getEntity(collection, ID, rev, entityProps, relationProps);

    assertThat(actualEntity, is(sameInstance(readEntity)));
  }

  @Test
  public void getCollectionLetsReturnsACollection() {
    DataStream<ReadEntity> entities = mockDataStream();
    int start = 0;
    int rows = 10;
    when(dataStoreOperations.getCollection(collection, start, rows, withRelations, entityProps, relationProps))
      .thenReturn(entities);

    DataStream<ReadEntity> result =
      instance.getCollection(collection, start, rows, withRelations, entityProps, relationProps);

    assertThat(result, is(sameInstance(entities)));
  }

  private DataStream<ReadEntity> mockDataStream() {
    return new DataStream<ReadEntity>() {
      @Override
      public <U> List<U> map(Function<ReadEntity, U> mapping) {
        throw new UnsupportedOperationException("Not implemented yet");
      }
    };
  }

  @Test
  public void loadVresDelegatesToDataStoreOperationsLoadVres() {
    Vres vres = mock(Vres.class);
    when(dataStoreOperations.loadVres()).thenReturn(vres);

    Vres actualVres = instance.loadVres();

    assertThat(actualVres, is(sameInstance(vres)));
  }

}
