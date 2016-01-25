package nl.knaw.huygens.timbuctoo.search.description;

import nl.knaw.huygens.timbuctoo.search.EntityRef;
import nl.knaw.huygens.timbuctoo.search.MockVertexBuilder;
import org.apache.tinkerpop.gremlin.structure.Vertex;
import org.junit.Before;
import org.junit.Test;


import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

public class WwDocumentSearchDescriptionTest {
  private WwDocumentSearchDescription instance;

  @Before
  public void setUp() throws Exception {
    instance = new WwDocumentSearchDescription();
  }

  @Test
  public void createRefCreatesARefWithTheIdOfTheVertexAndTheTypeOfTheDescription() {
    String id = "id";

    Vertex vertex = MockVertexBuilder.vertexWithId(id).build();

    EntityRef actualRef = instance.createRef(vertex);

    assertThat(actualRef.getId(), is(id));
    assertThat(actualRef.getType(), is(instance.getType()));
  }
}
