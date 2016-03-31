package nl.knaw.huygens.timbuctoo.search.description.facet;

import nl.knaw.huygens.timbuctoo.search.description.PropertyParser;
import org.apache.tinkerpop.gremlin.structure.Graph;
import org.junit.Before;
import org.junit.Test;

import static nl.knaw.huygens.timbuctoo.util.TestGraphBuilder.newGraph;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasProperty;
import static org.hamcrest.Matchers.is;
import static org.mockito.BDDMockito.given;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

public class DerivedListFacetDescriptionTest {

  public static final String RELATION_NAME = "hasKeyword";
  public static final String FACET_NAME = "facetName";
  public static final String RELATION = "relation";
  public static final String PROPERTY = "property";
  public static final String VALUE1 = "value1";
  public static final String VALUE2 = "value2";
  public static final String RELATION_2 = "relation2";
  private PropertyParser parser;

  @Before
  public void setUp() throws Exception {
    parser = mock(PropertyParser.class);
    given(parser.parse(anyString())).willAnswer(invocation -> invocation.getArguments()[0]);
  }
}
