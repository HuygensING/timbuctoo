package nl.knaw.huygens.timbuctoo.vre;

import static nl.knaw.huygens.timbuctoo.vre.VREManagerMatcher.matchesVREManager;
import static nl.knaw.huygens.timbuctoo.vre.VREMockBuilder.newVRE;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Map;

import nl.knaw.huygens.timbuctoo.index.Index;
import nl.knaw.huygens.timbuctoo.index.IndexMapCreator;
import nl.knaw.huygens.timbuctoo.index.IndexNameCreator;
import nl.knaw.huygens.timbuctoo.index.model.ExplicitlyAnnotatedModel;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

public class VREManagerTest {

  @Mock
  private Map<String, Index> indexMapMock;

  @Mock
  private Map<String, VRE> vreMapMock;

  private IndexNameCreator indexNameCreatorMock;

  private VREManager instance;

  @Before
  public void setUp() {
    MockitoAnnotations.initMocks(this);

    indexNameCreatorMock = mock(IndexNameCreator.class);
    instance = new VREManager(vreMapMock, indexMapMock, indexNameCreatorMock);
  }

  @Test
  public void testGetIndexFor() {
    // mock
    VRE vreMock = mock(VRE.class);

    Class<ExplicitlyAnnotatedModel> type = ExplicitlyAnnotatedModel.class;
    String indexName = "indexName";

    Index indexMock = mock(Index.class);

    // when
    when(indexNameCreatorMock.getIndexNameFor(vreMock, type)).thenReturn(indexName);
    when(indexMapMock.get(indexName)).thenReturn(indexMock);

    // action
    Index actualIndex = instance.getIndexFor(vreMock, type);

    // verify
    verify(indexMapMock).get(indexName);
    assertThat(actualIndex, equalTo(indexMock));
  }

  @Test
  public void testGetIndexForWhenIndexDoesNotExist() {
    // mock
    VRE vreMock = mock(VRE.class);

    Class<ExplicitlyAnnotatedModel> type = ExplicitlyAnnotatedModel.class;
    String indexName = "unknownIndex";

    // when
    when(indexNameCreatorMock.getIndexNameFor(vreMock, type)).thenReturn(indexName);

    // action
    Index index = instance.getIndexFor(vreMock, type);

    // verify
    verify(indexMapMock).get(indexName);

    assertThat(index, is(instanceOf(VREManager.NoOpIndex.class)));
  }

  @Test
  public void getVREByIdShouldReturnTheVREWhenFound() {
    // setup
    VRE vreMock = mock(VRE.class);
    String vreId = "vreId";

    when(vreMapMock.get(vreId)).thenReturn(vreMock);

    // action
    VRE actualVRE = instance.getVREById(vreId);

    //verify
    assertThat(actualVRE, equalTo(vreMock));
  }

  @Test
  public void getVREByIdShouldReturnNullWhenVRENotFound() {
    // setup
    String vreId = "vreId";

    // action
    VRE actualVRE = instance.getVREById(vreId);

    //verify
    assertThat(actualVRE, equalTo(null));
  }

  @SuppressWarnings("unchecked")
  @Test
  public void createInstanceCreatesAnInstanceWithGeneratedVREAndIndexMaps() {
    // setup
    String vreName1 = "VRE1";
    String vreName2 = "VRE2";

    Map<String, VRE> vres = Maps.newHashMap();

    VRE vre1 = newVRE().withName(vreName1).create();
    vres.put(vreName1, vre1);
    VRE vre2 = newVRE().withName(vreName2).create();
    vres.put(vreName2, vre2);

    String indexName1 = "VRE1Index1";
    String indexName2 = "VRE1Index2";
    String indexName3 = "VRE2Index1";
    String indexName4 = "VRE2Index2";
    Map<String, Index> vre1Indexes = createIndexMap(indexName1, indexName2);
    Map<String, Index> vre2Indexes = createIndexMap(indexName3, indexName4);

    Map<String, Index> combinedIndexes = Maps.newHashMap();
    combinedIndexes.putAll(vre1Indexes);
    combinedIndexes.putAll(vre2Indexes);

    IndexMapCreator indexFactoryMock = mock(IndexMapCreator.class);
    when(indexFactoryMock.createIndexesFor(vre1)).thenReturn(vre1Indexes);
    when(indexFactoryMock.createIndexesFor(vre2)).thenReturn(vre2Indexes);

    VREManager expectedVREManager = new VREManager(vres, combinedIndexes, indexNameCreatorMock);

    // action
    VREManager actualVREManager = VREManager.createInstance(//
        Lists.newArrayList(vre1, vre2), //
        indexNameCreatorMock, //
        indexFactoryMock);

    //verify
    assertThat(actualVREManager, matchesVREManager(expectedVREManager));
  }

  private Map<String, Index> createIndexMap(String firstIndexName, String... indexNames) {
    Map<String, Index> indexMap = Maps.newHashMap();

    addMockIndexToMap(indexMap, firstIndexName);

    for (String indexName : indexNames) {
      addMockIndexToMap(indexMap, indexName);
    }

    return indexMap;
  }

  private void addMockIndexToMap(Map<String, Index> indexMap, String indexName) {
    indexMap.put(indexName, mock(Index.class));
  }

}
