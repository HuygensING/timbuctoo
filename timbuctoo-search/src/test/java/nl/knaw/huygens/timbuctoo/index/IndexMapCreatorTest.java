package nl.knaw.huygens.timbuctoo.index;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import nl.knaw.huygens.timbuctoo.model.DomainEntity;
import nl.knaw.huygens.timbuctoo.vre.VRE;

import org.junit.Before;
import org.junit.Test;
import org.mockito.MockitoAnnotations;

import test.timbuctoo.index.model.BaseType1;
import test.timbuctoo.index.model.BaseType2;

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

public class IndexMapCreatorTest {
  private IndexMapCreator instance;

  private IndexFactory indexFactoryMock;
  private IndexNameCreator indexNameCreatorMock;

  @Before
  public void setUp() {
    MockitoAnnotations.initMocks(this);

    indexNameCreatorMock = mock(IndexNameCreator.class);
    indexFactoryMock = mock(IndexFactory.class);

    instance = new IndexMapCreator(indexNameCreatorMock, indexFactoryMock);
  }

  @Test
  public void createIndexesForCreatesAMapOfIndexesWithEachBaseTypeOfTheVRE() {
    // setup
    Set<Class<? extends DomainEntity>> baseTypes = Sets.newHashSet();
    baseTypes.add(BaseType1.class);
    baseTypes.add(BaseType2.class);
    VRE vreMock = mock(VRE.class);
    when(vreMock.getBaseEntityTypes()).thenReturn(baseTypes);

    String name1 = "basetype1";
    when(indexNameCreatorMock.getIndexNameFor(vreMock, BaseType1.class)).thenReturn(name1);
    String name2 = "basetype2";
    when(indexNameCreatorMock.getIndexNameFor(vreMock, BaseType2.class)).thenReturn(name2);

    Index indexMock1 = mock(Index.class);
    Index indexMock2 = mock(Index.class);

    Map<String, Index> expectedMap = Maps.newHashMap();
    expectedMap.put(name1, indexMock1);
    expectedMap.put(name2, indexMock2);

    when(indexFactoryMock.createIndexFor(name1)).thenReturn(indexMock1);
    when(indexFactoryMock.createIndexFor(name2)).thenReturn(indexMock2);

    // action
    Map<String, Index> actualMap = instance.createIndexesFor(vreMock);

    // verify
    assertThat(actualMap.entrySet(), equalTo(expectedMap.entrySet()));
  }

  @Test
  public void createIndexesForCreatesAnEmptyMapIfTheVREHasNoBaseTypes() {
    VRE vreMock = mock(VRE.class);
    HashSet<Class<? extends DomainEntity>> emptyBaseTypeSet = Sets.newHashSet();
    when(vreMock.getBaseEntityTypes()).thenReturn(emptyBaseTypeSet);

    Map<String, Index> actualIndexMap = instance.createIndexesFor(vreMock);

    assertThat(actualIndexMap.entrySet(), is(empty()));
  }
}
