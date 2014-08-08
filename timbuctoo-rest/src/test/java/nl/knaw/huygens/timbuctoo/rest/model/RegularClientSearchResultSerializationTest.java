package nl.knaw.huygens.timbuctoo.rest.model;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;

import java.util.ArrayList;
import java.util.Map;

import nl.knaw.huygens.facetedsearch.model.DefaultFacet;
import nl.knaw.huygens.facetedsearch.model.Facet;
import nl.knaw.huygens.timbuctoo.model.ClientEntityRepresentation;
import nl.knaw.huygens.timbuctoo.model.ClientSearchResult;
import nl.knaw.huygens.timbuctoo.model.RegularClientSearchResult;

import org.junit.Test;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.google.common.collect.Lists;

public class RegularClientSearchResultSerializationTest extends ClientSearchResultTest {
  private String[] keysWhenEmpty = new String[] { "sortableFields", "numFound", "results", "ids", "start", "rows", "term", "facets", "refs" };
  private String[] keysWhenFilled = new String[] { "sortableFields", "numFound", "results", "ids", "start", "rows", "term", "facets", "refs", "_next", "_prev" };

  @Test
  public void testWhenObjectHasAllEmptyProperties() throws JsonProcessingException {
    // setup
    ClientSearchResult searchResult = createEmptySearchResult();

    // action
    Map<String, Object> jsonMap = createJsonMap(searchResult);

    // verify
    assertThat(jsonMap.keySet(), contains(getKeysWhenEmpty()));
  }

  @Test
  public void testPropertiesWhenAllPropertiesContainAValue() {
    // setup
    ClientSearchResult searchResult = createFilledSearchResult();

    // action
    Map<String, Object> jsonMap = createJsonMap(searchResult);

    // verify
    assertThat(jsonMap.keySet(), contains(getKeysWhenFilled()));
  }

  @Override
  protected RegularClientSearchResult createEmptySearchResult() {
    RegularClientSearchResult searchResult = new RegularClientSearchResult();
    return searchResult;
  }

  @Override
  protected String[] getKeysWhenEmpty() {
    return keysWhenEmpty;
  }

  @Override
  protected String[] getKeysWhenFilled() {
    return keysWhenFilled;
  }

  @Override
  protected RegularClientSearchResult createFilledSearchResult() {
    RegularClientSearchResult searchResult = createEmptySearchResult();
    setClientRelationSearchResultProperties(searchResult);
    searchResult.setRefs(createRefList());
    searchResult.setFacets(createFacetList());
    searchResult.setTerm(STRING_PLACEHOLDER);
    return searchResult;
  }

  private ArrayList<ClientEntityRepresentation> createRefList() {
    ClientEntityRepresentation clientRef = new ClientEntityRepresentation(STRING_PLACEHOLDER, STRING_PLACEHOLDER, STRING_PLACEHOLDER, STRING_PLACEHOLDER);
    return Lists.newArrayList(clientRef);
  }

  private ArrayList<Facet> createFacetList() {
    Facet facet = new DefaultFacet(STRING_PLACEHOLDER, STRING_PLACEHOLDER);
    return Lists.newArrayList(facet);
  }

}
