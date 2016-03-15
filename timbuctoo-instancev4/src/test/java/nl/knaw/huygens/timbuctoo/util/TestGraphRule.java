package nl.knaw.huygens.timbuctoo.util;

import org.apache.tinkerpop.gremlin.structure.Graph;
import org.junit.rules.ExternalResource;

public class TestGraphRule extends ExternalResource {

  private TestGraphBuilder builder;
  private Graph instance = null;

  public TestGraphBuilder newGraph() {
    this.builder = TestGraphBuilder.newGraph();
    return builder;
  }

  public Graph build() {
    instance = builder.build();
    return instance;
  }

  @Override
  protected void after() {
    super.after();
    if (instance != null) {
      try {
        instance.close();
      } catch (Exception e) {
        System.out.println("exception occurred during closing of database" + e);
      }
    }
  }
}
