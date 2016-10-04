package nl.knaw.huygens.timbuctoo.database.tinkerpop;

import com.google.common.collect.Lists;
import nl.knaw.huygens.timbuctoo.database.CustomEntityProperties;
import nl.knaw.huygens.timbuctoo.database.CustomRelationProperties;
import nl.knaw.huygens.timbuctoo.database.DataAccessMethods;
import nl.knaw.huygens.timbuctoo.database.dto.ReadEntity;
import nl.knaw.huygens.timbuctoo.database.dto.dataset.Collection;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InOrder;
import org.mockito.Mockito;

import java.util.List;
import java.util.function.Function;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;

public class TinkerPopGetCollectionTest {

  public static final int START = 1;
  public static final int ROWS = 2;
  public static final boolean WITH_RELATIONS = false;
  private Collection collection;
  private CustomEntityProperties entityProps;
  private CustomRelationProperties relationProps;
  private DataAccessMethods dataAccessMethods;
  private TinkerPopGetCollection instance;
  private Function<ReadEntity, String> toStringFunction;
  private ReadEntity readEntity1;
  private ReadEntity readEntity2;

  @Before
  public void setUp() throws Exception {
    collection = mock(Collection.class);
    entityProps = mock(CustomEntityProperties.class);
    relationProps = mock(CustomRelationProperties.class);
    dataAccessMethods = mock(DataAccessMethods.class);
    readEntity2 = mock(ReadEntity.class);
    readEntity1 = mock(ReadEntity.class);
    Mockito.when(dataAccessMethods.getCollection(collection, ROWS, START, WITH_RELATIONS, entityProps, relationProps))
           .thenReturn(Lists.newArrayList(readEntity1, readEntity2).stream());
    instance =
      new TinkerPopGetCollection(collection, START, ROWS, WITH_RELATIONS, entityProps, relationProps,
        dataAccessMethods);
    toStringFunction = readEntity -> readEntity.toString();
  }

  @Test
  public void mapRetrievesTheDataAndThenClosesTheTransaction() {
    instance.map(toStringFunction);

    InOrder inOrder = inOrder(dataAccessMethods);
    inOrder.verify(dataAccessMethods)
           .getCollection(collection, ROWS, START, WITH_RELATIONS, entityProps, relationProps);
    inOrder.verify(dataAccessMethods).success();
    inOrder.verify(dataAccessMethods).close();
  }

  @Test
  public void mapLetsTheInputFunctionMapTheData() {
    List<String> entities = instance.map(toStringFunction);

    assertThat(entities, containsInAnyOrder(readEntity1.toString(), readEntity2.toString()));
  }

}
