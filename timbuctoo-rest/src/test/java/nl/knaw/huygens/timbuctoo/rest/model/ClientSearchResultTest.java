package nl.knaw.huygens.timbuctoo.rest.model;

import java.util.ArrayList;
import java.util.Map;

import nl.knaw.huygens.timbuctoo.model.ClientSearchResult;
import nl.knaw.huygens.timbuctoo.model.DomainEntity;

import org.junit.Before;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

public abstract class ClientSearchResultTest {

  private static final int INT_PLACEHOLDER = 10;
  protected static final String STRING_PLACEHOLDER = "test";

  protected abstract ClientSearchResult createFilledSearchResult();

  protected abstract String[] getKeysWhenFilled();

  protected abstract String[] getKeysWhenEmpty();

  protected abstract ClientSearchResult createEmptySearchResult();

  private ObjectMapper objectMapper;

  @Before
  public void setUp() {
    objectMapper = new ObjectMapper();
  }

  protected void setClientRelationSearchResultProperties(ClientSearchResult searchResult) {
    searchResult.setIds(Lists.newArrayList(STRING_PLACEHOLDER));
    searchResult.setNextLink(STRING_PLACEHOLDER);
    searchResult.setPrevLink(STRING_PLACEHOLDER);
    searchResult.setNumFound(INT_PLACEHOLDER);
    searchResult.setResults(createResultList());
    searchResult.setRows(INT_PLACEHOLDER);
    searchResult.setSortableFields(Sets.newHashSet(STRING_PLACEHOLDER));
    searchResult.setStart(INT_PLACEHOLDER);
  }

  private ArrayList<DomainEntity> createResultList() {
    DomainEntity entity = new DomainEntity() {

      @Override
      public String getDisplayName() {
        // TODO Auto-generated method stub
        return null;
      }

    };
    return Lists.newArrayList(entity);
  }

  protected Map<String, Object> createJsonMap(ClientSearchResult searchResult) {
    @SuppressWarnings("unchecked")
    Map<String, Object> jsonMap = objectMapper.convertValue(searchResult, Map.class);
    return jsonMap;
  }

}