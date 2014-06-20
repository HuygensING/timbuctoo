package nl.knaw.huygens.timbuctoo.search.converters;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsInAnyOrder;
import nl.knaw.huygens.solr.SearchParameters;
import nl.knaw.huygens.solr.SearchParametersV1;

import org.junit.Test;

public class FacetFieldConveterTest {
  @Test
  public void testAddToV1() {
    // setup
    SearchParametersV1 searchParametersV1 = new SearchParametersV1();
    String name1 = "name1";
    String name2 = "name2";

    SearchParameters searchParameters = new SearchParameters();
    searchParameters.setFacetFields(new String[] { name1, name2 });

    FacetFieldConverter instance = new FacetFieldConverter();

    // action
    instance.addToV1(searchParameters, searchParametersV1);

    // verify
    assertThat(searchParametersV1.getFacetFields(), containsInAnyOrder(name1, name2));

  }
}
