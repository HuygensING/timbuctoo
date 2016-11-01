package nl.knaw.huygens.timbuctoo.crud;

import nl.knaw.huygens.timbuctoo.util.JsonBuilder;
import org.apache.tinkerpop.gremlin.structure.Graph;
import org.junit.Test;

import static nl.knaw.huygens.timbuctoo.crud.JsonCrudServiceBuilder.newJsonCrudService;
import static nl.knaw.huygens.timbuctoo.util.TestGraphBuilder.newGraph;

public class JsonCrudServiceCreateTest {

  @Test (expected = InvalidCollectionException.class)
  public void throwsOnUnknownMappings() throws Exception {
    Graph graph = newGraph().build();
    JsonCrudService instance = newJsonCrudService().forGraph(graph);

    instance.create("not_wwpersons", JsonBuilder.jsnO(), "");
  }

}
