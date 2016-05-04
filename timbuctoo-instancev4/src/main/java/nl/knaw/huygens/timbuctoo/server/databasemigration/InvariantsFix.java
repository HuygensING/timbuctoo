package nl.knaw.huygens.timbuctoo.server.databasemigration;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import nl.knaw.huygens.timbuctoo.model.Change;
import nl.knaw.huygens.timbuctoo.model.vre.Collection;
import nl.knaw.huygens.timbuctoo.model.vre.Vre;
import nl.knaw.huygens.timbuctoo.model.vre.Vres;
import org.apache.tinkerpop.gremlin.neo4j.structure.Neo4jGraph;
import org.apache.tinkerpop.gremlin.structure.Direction;
import org.apache.tinkerpop.gremlin.structure.Element;
import org.apache.tinkerpop.gremlin.structure.Transaction;
import org.apache.tinkerpop.gremlin.structure.Vertex;
import org.apache.tinkerpop.shaded.jackson.databind.node.JsonNodeFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static nl.knaw.huygens.timbuctoo.model.GraphReadUtils.TYPES_PROP;
import static nl.knaw.huygens.timbuctoo.model.GraphReadUtils.getEntityTypesOrDefault;

public class InvariantsFix implements DatabaseMigration {
  public static final Logger LOG = LoggerFactory.getLogger(InvariantsFix.class);
  private final Vres vres;
  private final List<String> processedTypes;
  private final Change change;

  public InvariantsFix(Vres vres) {
    this.vres = vres;
    processedTypes = Lists.newArrayList();
    change = Change.newInternalInstance();
  }

  @Override
  public String getName() {
    return this.getClass().getName();
  }

  @Override
  public void generateIndexes(Neo4jGraph neo4jGraph, Transaction transaction) {
    // no indices to generate
  }

  @Override
  public void applyToVertex(Vertex vertex) throws IOException {
    String[] vertexTypes = getEntityTypesOrDefault(vertex);
    String id = vertex.value("tim_id");

    vertex.edges(Direction.BOTH).forEachRemaining(edge -> {
      if (edge.keys().contains("tim_id")) { // ignore the VERSION_OF relations
        String[] edgeTypes = getEntityTypesOrDefault(edge);
        String edgeId = edge.value("tim_id");
        for (String edgeType : edgeTypes) {
          Optional<Collection> collection = vres.getCollectionForType(edgeType);

          if (!collection.isPresent()) {
            // ignore unknown collections of edges
            continue;
          }

          Collection edgeCollection = collection.get();

          if (vertexTypes[0].contains("language")) {
            duplicateDefaultInformationToMissingVariant(vertex, edgeCollection, "language");
          } else if (vertexTypes[0].contains("location")) {
            duplicateDefaultInformationToMissingVariant(vertex, edgeCollection, "location");
          } else {
            List<String> types = Lists.newArrayList(edgeTypes);
            types.remove(edgeType);
            edge.property(TYPES_PROP, types.toArray(new String[types.size()]));
          }
        }
      }
    });

  }

  /**
   * Update the vertex and it's modification information. Only use for languages and locations.
   */
  private void duplicateDefaultInformationToMissingVariant(Vertex vertex,
                                                           Collection edgeTypeToGetVariantFor,
                                                           String vertexAbstractType) {
    if (!processedTypes.contains(edgeTypeToGetVariantFor.getEntityTypeName())) {
      Vre vre = edgeTypeToGetVariantFor.getVre();

      Optional<Collection> col = vre.getImplementerOf(vertexAbstractType);
      vre.getImplementerOf(vertexAbstractType);

      if (col.isPresent()) {
        Collection collection = col.get();
        String entityTypeName = collection.getEntityTypeName();

        Map<String, Object> propertiesToSet = getPropertiesToSet(vertex, vertexAbstractType, entityTypeName);
        propertiesToSet.entrySet().forEach(entry -> vertex.property(entry.getKey(), entry.getValue()));
        setModified(vertex, change);
        vertex.property("pid").remove();
        int rev = vertex.<Integer>value("rev") + 1;
        vertex.property("rev", rev);

        // Languages and Locations do not have to be persisted or duplicated, because these entities should not be
        // edited by users or exposed to the world. They are just helper entities at this moment.
      } else {
        // should never happen
        String exception = String.format("Vre '%s' has no subtype of '%s'", vre.getVreName(), vertexAbstractType);
        LOG.error(exception);
        throw new RuntimeException(exception);
      }
      processedTypes.add(edgeTypeToGetVariantFor.getEntityTypeName());
    }
  }

  private void setModified(Element element, Change change) {
    String value = String.format("{\"timeStamp\":%s, \"userId\":%s, \"vreId\":\"%s\"}",
      change.getTimeStamp(),
      JsonNodeFactory.instance.textNode(change.getUserId()),
      JsonNodeFactory.instance.textNode(change.getVreId())
    );
    element.property("modified", value);
  }

  private Map<String, Object> getPropertiesToSet(Vertex vertex, String vertexAbstractType, String entityTypeName) {
    Map<String, Object> propertyValues = Maps.newHashMap();

    vertex.properties().forEachRemaining(prop -> {
      if (prop.key().contains(vertexAbstractType)) {
        propertyValues.put(prop.key().replace(vertexAbstractType, entityTypeName), prop.value());
      }
    });
    return propertyValues;
  }


}
