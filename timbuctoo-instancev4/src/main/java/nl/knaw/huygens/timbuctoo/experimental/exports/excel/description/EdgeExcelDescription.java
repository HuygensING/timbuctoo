package nl.knaw.huygens.timbuctoo.experimental.exports.excel.description;

import com.google.common.collect.Lists;
import nl.knaw.huygens.timbuctoo.logging.Logmarkers;
import nl.knaw.huygens.timbuctoo.model.vre.Collection;
import nl.knaw.huygens.timbuctoo.model.vre.Vres;
import org.apache.tinkerpop.gremlin.structure.Edge;
import org.slf4j.Logger;

import java.util.List;
import java.util.Optional;

import static nl.knaw.huygens.timbuctoo.model.GraphReadUtils.getCollectionByVreId;

public class EdgeExcelDescription implements ExcelDescription {
  private static final Logger LOG = org.slf4j.LoggerFactory.getLogger(EdgeExcelDescription.class);

  private final List<Edge> edges;
  private final Optional<Collection> targetCollection;

  public EdgeExcelDescription(List<Edge> edges, Vres mappings, String vreId) {
    if (edges.size() < 1) {
      throw new IllegalArgumentException("Cannot instantiate EdgeExcelDescription without edges");
    }

    this.edges = edges;
    this.targetCollection = getCollectionByVreId(edges.get(0).inVertex(), mappings, vreId);

    if (!targetCollection.isPresent()) {
      LOG.error(Logmarkers.databaseInvariant, "Edge {} points to entity of incorrect collection", this.edges.get(0));
    }

  }

  @Override
  public int getRows() {
    return 1;
  }

  @Override
  public int getCols() {
    return edges.size();
  }

  @Override
  public String getType() {
    return "relation";
  }

  @Override
  public String[][] getCells() {

    return new String[][] {
      edges.stream().<String>map(edge -> edge.inVertex().value("tim_id")).toArray(String[]::new)
    };
  }

  @Override
  public int getValueWidth() {
    return 1;
  }

  @Override
  public List<String> getValueDescriptions() {
    final String value = targetCollection.isPresent() ? targetCollection.get().getCollectionName() : "?";
    List<String> result = Lists.newArrayList();

    for (Edge edge : edges) {
      result.add(value);
    }

    return result;
  }

}
