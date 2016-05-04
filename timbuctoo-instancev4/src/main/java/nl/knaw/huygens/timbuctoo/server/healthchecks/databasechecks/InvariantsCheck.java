package nl.knaw.huygens.timbuctoo.server.healthchecks.databasechecks;

import com.google.common.collect.Lists;
import nl.knaw.huygens.timbuctoo.model.vre.Collection;
import nl.knaw.huygens.timbuctoo.model.vre.Vres;
import nl.knaw.huygens.timbuctoo.server.healthchecks.CompositeValidationResult;
import nl.knaw.huygens.timbuctoo.server.healthchecks.DatabaseCheck;
import nl.knaw.huygens.timbuctoo.server.healthchecks.ElementValidationResult;
import nl.knaw.huygens.timbuctoo.server.healthchecks.ValidationResult;
import org.apache.tinkerpop.gremlin.structure.Direction;
import org.apache.tinkerpop.gremlin.structure.Vertex;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Optional;

import static nl.knaw.huygens.timbuctoo.model.GraphReadUtils.getEntityTypesOrDefault;

/**
 * A database health check to checks that all vertices only relate to vertices from the same VRE.
 */
public class InvariantsCheck implements DatabaseCheck {

  public static final Logger LOG = LoggerFactory.getLogger(InvariantsCheck.class);
  private final Vres vres;

  public InvariantsCheck(Vres vres) {
    this.vres = vres;
  }

  @Override
  public ValidationResult check(Vertex vertex) {
    List<ValidationResult> validationResults = Lists.newArrayList();
    String[] vertexTypes = getEntityTypesOrDefault(vertex);
    String id = vertex.value("tim_id");

    vertex.edges(Direction.BOTH).forEachRemaining(edge -> {
      if (edge.keys().contains("tim_id")) { // ignore the VERSION_OF relations
        String[] edgeTypes = getEntityTypesOrDefault(edge);
        for (String edgeType : edgeTypes) {
          Optional<Collection> collection = vres.getCollectionForType(edgeType);
          String edgeId = edge.value("tim_id");

          if (!collection.isPresent()) {
            validationResults.add(
              new ElementValidationResult(false,
                String.format("Edge with tim_id '%s' has contains unknown variant '%s'", edgeId, edgeType)));
            continue;
          }

          if (collection.get().getVre().getOwnType(vertexTypes) == null) {
            addInvalidVertex(validationResults, id, vertexTypes, edgeType, edgeId);
          }
        }
      }
    });
    return new CompositeValidationResult(validationResults);
  }

  private void addInvalidVertex(List<ValidationResult> validationResults, String id, String[] vertexTypes,
                                String edgeType, String edgeId) {
    if (vertexTypes[0].contains("language") || vertexTypes[0].contains("location")) {
      validationResults.add(new ElementValidationResult(false,
        String
          .format(
            "Vertex with tim_id '%s' with variants '%s' should have a variant to match variant '%s' of edge with " +
              "tim_id '%s'",
            id, Lists.newArrayList(vertexTypes), edgeType, edgeId)));
    } else {
      validationResults.add(new ElementValidationResult(false,
        String
          .format(
            "Vertex with tim_id '%s' with types '%s' has no variant to match '%s' of edge with tim_id '%s'",
            id, Lists.newArrayList(vertexTypes), edgeType, edgeId)));
    }
  }
}
