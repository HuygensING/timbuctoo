package nl.knaw.huygens.timbuctoo.model;

import com.google.common.collect.Lists;
import nl.knaw.huygens.timbuctoo.model.vre.Collection;
import nl.knaw.huygens.timbuctoo.model.vre.Vre;
import nl.knaw.huygens.timbuctoo.model.vre.Vres;
import nl.knaw.huygens.timbuctoo.server.GraphWrapper;
import org.apache.tinkerpop.gremlin.structure.Edge;
import org.apache.tinkerpop.gremlin.structure.Vertex;

import java.time.Clock;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import static nl.knaw.huygens.timbuctoo.model.GraphReadUtils.getEntityTypesOrDefault;
import static nl.knaw.huygens.timbuctoo.util.StreamIterator.stream;

public class DatabaseInvariantValidator {
  private final GraphWrapper graphWrapper;
  private final int timeoutInHours;
  private final Clock clock;
  private final Vres vres;
  private ValidationResult previousResult;
  private Instant lastCheck;

  public DatabaseInvariantValidator(GraphWrapper graphWrapper, int timeoutInHours, Clock clock, Vres vres) {
    this.graphWrapper = graphWrapper;
    this.timeoutInHours = timeoutInHours;
    this.clock = clock;
    this.vres = vres;
    this.previousResult = null;
  }

  /**
   * Forced check will always recheck regardless of the timeout. This method is blocking.
   *
   * @return whether the database meets its invariants
   */
  public ValidationResult forcedCheck() {
    Map<Vre, List<Vertex>> verticesLinkedAcrossVres = stream(graphWrapper.getGraph().traversal()
      .E()
      .flatMap(x -> Lists.newArrayList(
        new EdgeWithVertex(x.get(), x.get().inVertex()),
        new EdgeWithVertex(x.get(), x.get().outVertex()))
        .iterator()
      )
    )
      .flatMap(x ->
        Arrays.stream(getEntityTypesOrDefault(x.edge))
          .map(type -> new EdgeTypeWithVertex(type, x.vertex))
      )
      .filter(x -> {
        String edgeType = x.edgeType;
        Vertex vertex = x.vertex;
        String[] vertexTypes = getEntityTypesOrDefault(vertex);
        Optional<Collection> coll = vres.getCollectionForType(edgeType);
        if (coll.isPresent()) { //ignore vertices with unknown collections
          if (coll.get().getVre().getOwnType(vertexTypes) == null) {
            return true;
          }
        }
        return false;
      })
      .collect(Collectors.groupingBy(
        x -> vres.getCollectionForType(x.edgeType).get().getVre(),
        Collectors.mapping(x -> x.vertex, Collectors.toList())
      ));

    this.lastCheck = clock.instant();
    this.previousResult = new ValidationResult(verticesLinkedAcrossVres);
    return previousResult;
  }

  /**
   * Gentle check won't run more often then the delay that is specified in the constructor.
   *
   * @return whether the database meets its invariants.
   */
  public ValidationResult lazyCheck() {
    if (previousResult == null) {
      return forcedCheck();
    } else if (ChronoUnit.HOURS.between(lastCheck, clock.instant()) >= timeoutInHours) {
      return forcedCheck();
    } else {
      return previousResult;
    }
  }

  public class ValidationResult {
    public final Map<Vre, List<Vertex>> verticesLinkedAcrossVres;

    public ValidationResult(Map<Vre, List<Vertex>> verticesLinkedAcrossVres) {
      this.verticesLinkedAcrossVres = verticesLinkedAcrossVres;
    }

    public boolean isValid() {
      return verticesLinkedAcrossVres.isEmpty();
    }
  }

  private class EdgeWithVertex {
    public final Edge edge;
    public final Vertex vertex;

    private EdgeWithVertex(Edge edge, Vertex vertex) {
      this.edge = edge;
      this.vertex = vertex;
    }
  }

  private class EdgeTypeWithVertex {
    public final String edgeType;
    public final Vertex vertex;

    private EdgeTypeWithVertex(String edgeType, Vertex vertex) {
      this.edgeType = edgeType;
      this.vertex = vertex;
    }
  }

}
