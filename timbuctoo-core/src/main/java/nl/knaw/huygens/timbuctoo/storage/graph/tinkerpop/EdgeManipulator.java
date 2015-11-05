package nl.knaw.huygens.timbuctoo.storage.graph.tinkerpop;

import com.tinkerpop.blueprints.Direction;
import com.tinkerpop.blueprints.Edge;
import com.tinkerpop.blueprints.Vertex;

import static nl.knaw.huygens.timbuctoo.storage.graph.tinkerpop.ElementFields.IS_LATEST;
import static nl.knaw.huygens.timbuctoo.storage.graph.tinkerpop.ElementHelper.sourceOfEdge;
import static nl.knaw.huygens.timbuctoo.storage.graph.tinkerpop.ElementHelper.targetOfEdge;

public class EdgeManipulator {

  public EdgeManipulator() {
    // TODO Auto-generated constructor stub
  }

  public void duplicate(Edge edgeToDuplicate) {
    Edge duplicate = createDuplicate(edgeToDuplicate);

    addProperties(edgeToDuplicate, duplicate);

    changeLatest(duplicate, edgeToDuplicate);
  }

  private void changeLatest(Edge duplicate, Edge edgeToDuplicate) {
    duplicate.setProperty(IS_LATEST, true);
    edgeToDuplicate.setProperty(IS_LATEST, false);
  }

  private Edge createDuplicate(Edge edgeToDuplicate) {
    Vertex soureOfEdge = edgeToDuplicate.getVertex(Direction.OUT);
    Vertex targetOfEdge = edgeToDuplicate.getVertex(Direction.IN);

    Edge duplicate = soureOfEdge.addEdge(edgeToDuplicate.getLabel(), targetOfEdge);
    return duplicate;
  }

  private void addProperties(Edge edgeToDuplicate, Edge duplicate) {
    for (String key : edgeToDuplicate.getPropertyKeys()) {
      duplicate.setProperty(key, edgeToDuplicate.getProperty(key));
    }
  }

  public void changeSource(Edge original, Vertex newSource) {
    Edge newEdge = newSource.addEdge(original.getLabel(), targetOfEdge(original));

    addProperties(original, newEdge);

    original.remove();
  }

  public void changeTarget(Edge original, Vertex newTarget) {
    Edge newEdge = sourceOfEdge(original).addEdge(original.getLabel(), newTarget);

    addProperties(original, newEdge);

    original.remove();
  }
}
