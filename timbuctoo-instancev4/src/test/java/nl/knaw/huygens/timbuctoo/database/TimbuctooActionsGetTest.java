package nl.knaw.huygens.timbuctoo.database;

import nl.knaw.huygens.timbuctoo.crud.NotFoundException;
import nl.knaw.huygens.timbuctoo.database.dto.DataStream;
import nl.knaw.huygens.timbuctoo.database.dto.ReadEntity;
import nl.knaw.huygens.timbuctoo.database.dto.dataset.Collection;
import org.junit.Before;
import org.junit.Test;

import java.util.List;
import java.util.UUID;
import java.util.function.Function;

import static nl.knaw.huygens.timbuctoo.database.GetMessage.notFound;
import static nl.knaw.huygens.timbuctoo.database.GetMessage.success;
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
  private TransactionEnforcer transactionEnforcer;
  private TimbuctooActions instance;
  private Collection collection;
  private boolean withRelations = false;

  @Before
  public void setUp() throws Exception {
    entityProps = mock(CustomEntityProperties.class);
    relationProps = mock(CustomRelationProperties.class);
    transactionEnforcer = mock(TransactionEnforcer.class);
    instance = new TimbuctooActions(null, transactionEnforcer, null, null);
    collection = mock(Collection.class);
  }

  @Test(expected = NotFoundException.class)
  public void getEntityThrowsANotFoundExceptionWhenTheEntityCannotBeFound() throws Exception {
    when(transactionEnforcer.getEntity(collection, ID, rev, entityProps, relationProps)).thenReturn(notFound());

    instance.getEntity(collection, ID, rev, entityProps, relationProps);
  }

  @Test
  public void getEntityReturnsTheReadEntity() throws Exception {
    ReadEntity readEntity = mock(ReadEntity.class);
    when(transactionEnforcer.getEntity(collection, ID, rev, entityProps, relationProps))
      .thenReturn(success(readEntity));

    ReadEntity actualEntity = instance.getEntity(collection, ID, rev, entityProps, relationProps);

    assertThat(actualEntity, is(sameInstance(readEntity)));
  }

  @Test
  public void getCollectionLetsReturnsACollection() {
    DataStream<ReadEntity> entities = mockDataStream();
    int start = 0;
    int rows = 10;
    when(transactionEnforcer.getCollection(collection, start, rows, withRelations, entityProps, relationProps))
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


}
