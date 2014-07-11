package nl.knaw.huygens.timbuctoo.rest.resources;

import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import nl.knaw.huygens.timbuctoo.Repository;
import nl.knaw.huygens.timbuctoo.model.DomainEntity;
import nl.knaw.huygens.timbuctoo.search.SortableFieldFinder;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

public class ClientSearchResultCreatorTest {

  protected static final String NEXT_LINK = "http://www.test.com/next";
  protected static final String PREV_LINK = "http://www.test.com/prev";
  protected static final ArrayList<String> ID_LIST_WITH_TEN_IDS = Lists.newArrayList("id1", "id2", "id3", "id4", "id5", "id6", "id7", "id8", "id9", "id10");
  protected static final int NUMBER_OF_RESULTS_FOUND = ID_LIST_WITH_TEN_IDS.size();
  protected static final String QUERY_ID = "queryId";
  protected static final HashSet<String> SORTABLE_FIELDS = Sets.newHashSet();
  protected Repository repositoryMock;
  protected SortableFieldFinder sortableFieldFinderMock;
  protected String nullPrevLink;
  protected String nullNextLink;
  protected HATEOASURICreator hateoasURICreatorMock;

  public void initializeRepository() {
    repositoryMock = mock(Repository.class);
  }

  public void initializeHATEOASURICreator() {
    hateoasURICreatorMock = mock(HATEOASURICreator.class);
  }

  public void initilizeSortableFieldFinder() {
    sortableFieldFinderMock = mock(SortableFieldFinder.class);
  }

  protected <T extends DomainEntity> List<T> setupRepository(Class<T> type, List<String> idList) throws InstantiationException, IllegalAccessException {
    List<T> domainEntities = Lists.newArrayList();

    for (String id : idList) {

      final T domainEntity = type.newInstance();
      domainEntities.add(domainEntity);
      doReturn(domainEntity).when(repositoryMock).getEntity(type, id);
    }

    return domainEntities;

  }

}