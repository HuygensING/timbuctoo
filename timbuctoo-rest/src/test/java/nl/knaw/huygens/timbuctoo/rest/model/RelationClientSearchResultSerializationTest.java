package nl.knaw.huygens.timbuctoo.rest.model;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;

import java.util.List;
import java.util.Map;

import nl.knaw.huygens.timbuctoo.model.ClientRelationRepresentation;
import nl.knaw.huygens.timbuctoo.model.ClientSearchResult;
import nl.knaw.huygens.timbuctoo.model.RelationClientSearchResult;

import org.junit.Test;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.google.common.collect.Lists;

public class RelationClientSearchResultSerializationTest extends ClientSearchResultTest {
  String[] keysWhenEmpty = new String[] { "sortableFields", "numFound", "results", "ids", "start", "rows", "refs" };
  String[] keysWhenFilled = new String[] { "sortableFields", "numFound", "results", "ids", "start", "rows", "refs", "_next", "_prev" };

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
  protected ClientSearchResult createFilledSearchResult() {
    RelationClientSearchResult searchResult = new RelationClientSearchResult();
    setClientRelationSearchResultProperties(searchResult);
    searchResult.setRefs(createRefs());

    return searchResult;
  }

  private List<ClientRelationRepresentation> createRefs() {
    ClientRelationRepresentation ref = new ClientRelationRepresentation(STRING_PLACEHOLDER, STRING_PLACEHOLDER, STRING_PLACEHOLDER, STRING_PLACEHOLDER, STRING_PLACEHOLDER, STRING_PLACEHOLDER);

    return Lists.newArrayList(ref);
  }

  @Override
  protected String[] getKeysWhenFilled() {
    return keysWhenFilled;
  }

  @Override
  protected String[] getKeysWhenEmpty() {
    return keysWhenEmpty;
  }

  @Override
  protected ClientSearchResult createEmptySearchResult() {
    return new RelationClientSearchResult();
  }
}
