package nl.knaw.huygens.timbuctoo.index;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.notNullValue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Set;

import nl.knaw.huygens.timbuctoo.index.IndexCollection.NoOpIndex;
import nl.knaw.huygens.timbuctoo.model.DomainEntity;
import nl.knaw.huygens.timbuctoo.vre.VRE;

import org.junit.Before;
import org.junit.Test;

import test.timbuctoo.index.model.BaseType1;
import test.timbuctoo.index.model.Type1;
import test.timbuctoo.index.model.Type2;

import com.google.common.collect.Sets;

public class IndexCollectionTest {

  private static final Class<? extends DomainEntity> TYPE_WITHOUT_INDEX = Type2.class;
  private static final Class<? extends DomainEntity> TYPE_WITH_INDEX = Type1.class;
  private static final Class<? extends DomainEntity> BASE_TYPE_WITH_INDEX = BaseType1.class;
  private IndexCollection instance;

  @Before
  public void setUp() {
    instance = new IndexCollection();
    instance.addIndex(TYPE_WITH_INDEX, mock(Index.class));
  }

  @Test
  public void whenIndexCollectionHasAnIndexForTheRequestedTypeItShouldReturnIt() {
    Index index = instance.getIndexByType(TYPE_WITH_INDEX);

    assertThatIndexIsNotNullAndNotNoOpIndex(index);
  }

  @Test
  public void theIndexCollectionShouldMakeNoDifferenceBetweenPrimitivesAndProjectSpecificTypesWhenRetrievingAnIndex() {
    Index index = instance.getIndexByType(BASE_TYPE_WITH_INDEX);

    assertThatIndexIsNotNullAndNotNoOpIndex(index);
  }

  @Test
  public void whenIndexCollectionDoesNotHaveAnIndexForTheRequestedTypeItShouldReturnANoOPIndex() {
    Index index = instance.getIndexByType(TYPE_WITHOUT_INDEX);

    assertThat(index, is(notNullValue(Index.class)));
    assertThat(index, is(instanceOf(NoOpIndex.class)));
  }

  @Test
  public void whenAnIndexCollectionIsCreatedWithACollectionOfDomainEntitiesItShouldContainAnIndexForEachOne() {
    // setup
    VRE vreMock = mock(VRE.class);

    IndexFactory indexFactoryMock = mock(IndexFactory.class);

    Class<? extends DomainEntity> type1 = Type1.class;
    Class<? extends DomainEntity> type2 = Type2.class;
    Set<Class<? extends DomainEntity>> types = Sets.newHashSet();
    types.add(type1);
    types.add(type2);

    when(vreMock.getEntityTypes()).thenReturn(types);
    when(indexFactoryMock.createIndexFor(vreMock, type1)).thenReturn(mock(Index.class));
    when(indexFactoryMock.createIndexFor(vreMock, type2)).thenReturn(mock(Index.class));

    // action
    instance = IndexCollection.create(indexFactoryMock, vreMock);

    // verify
    assertThatIndexIsNotNullAndNotNoOpIndex(instance.getIndexByType(type1));
    assertThatIndexIsNotNullAndNotNoOpIndex(instance.getIndexByType(type2));
  }

  private void assertThatIndexIsNotNullAndNotNoOpIndex(Index index) {
    assertThat(index, is(notNullValue(Index.class)));
    assertThat(index, is(not(instanceOf(NoOpIndex.class))));
  }
}
