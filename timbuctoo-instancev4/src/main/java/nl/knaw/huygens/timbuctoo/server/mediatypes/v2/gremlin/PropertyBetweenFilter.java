package nl.knaw.huygens.timbuctoo.server.mediatypes.v2.gremlin;

import nl.knaw.huygens.timbuctoo.model.vre.Vres;
import org.apache.tinkerpop.gremlin.process.traversal.P;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.GraphTraversal;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.GraphTraversalSource;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.__;

public class PropertyBetweenFilter extends NumberPropertyValueFilter {
  private static final String TYPE = "between";


  public String getType() {
    return TYPE;
  }

  @Override
  public GraphTraversal getTraversal(GraphTraversalSource traversalSource) {
    return __.has(getPropertyName()).where(prepareTraversal()
            .is(P.between(Integer.parseInt(getValues().get(0)), Integer.parseInt(getValues().get(1)))));

  }

  @Override
  public void setVres(Vres vres) {

  }
}
