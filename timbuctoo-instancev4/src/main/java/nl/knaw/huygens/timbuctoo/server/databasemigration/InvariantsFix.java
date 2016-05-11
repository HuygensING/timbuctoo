package nl.knaw.huygens.timbuctoo.server.databasemigration;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import nl.knaw.huygens.timbuctoo.model.Change;
import nl.knaw.huygens.timbuctoo.model.vre.Collection;
import nl.knaw.huygens.timbuctoo.model.vre.Vre;
import nl.knaw.huygens.timbuctoo.model.vre.Vres;
import nl.knaw.huygens.timbuctoo.server.TinkerpopGraphManager;
import org.apache.tinkerpop.gremlin.neo4j.structure.Neo4jGraph;
import org.apache.tinkerpop.gremlin.neo4j.structure.Neo4jVertex;
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
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

import static nl.knaw.huygens.timbuctoo.model.GraphReadUtils.getEntityTypes;
import static nl.knaw.huygens.timbuctoo.model.GraphReadUtils.getEntityTypesOrDefault;

public class InvariantsFix implements DatabaseMigration {
  public static final Logger LOG = LoggerFactory.getLogger(InvariantsFix.class);
  private final Vres vres;
  private final Set<String> processedTypes;
  private final Change change;
  private final ObjectMapper objectMapper;

  public InvariantsFix(Vres vres) {
    this.vres = vres;
    processedTypes = Sets.newHashSet();
    change = Change.newInternalInstance();
    objectMapper = new ObjectMapper();
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
  public void beforeMigration(TinkerpopGraphManager graphManager) {
    // before hook not needed
  }

  @Override
  public void applyToVertex(Vertex vertex) throws IOException {
    String[] vertexTypes = getEntityTypesOrDefault(vertex);
    String adminVertexType = getAbstractType(vertexTypes);

    vertex.edges(Direction.BOTH).forEachRemaining(edge -> {
      if (!Objects.equals(edge.label(), "VERSION_OF")) { // ignore the VERSION_OF relations
        String[] edgeTypes = getEntityTypesOrDefault(edge);

        for (String edgeType : edgeTypes) {
          Optional<Collection> collectionOptional = vres.getCollectionForType(edgeType);

          collectionOptional.ifPresent(collection -> {

            if (collection.getVre().getOwnType(vertexTypes) == null) {
              if (Objects.equals("language", adminVertexType) || Objects.equals("location", adminVertexType)) {
                duplicateDefaultInformationToMissingVariant(vertex, collection, adminVertexType);
              } else {
                List<String> types = Lists.newArrayList(edgeTypes);
                types.remove(edgeType);
                try {
                  edge.property("types", objectMapper.writeValueAsString(types));
                } catch (JsonProcessingException e) {
                  throw new RuntimeException("Types could not be set to edge", e);
                }
              }
            }
          });
        }
      }
    });
    processedTypes.clear(); // clear processed types, each vertex should be process all the types

  }

  private String getAbstractType(String[] vertexTypes) {
    Vre admin = vres.getVre("Admin"); // Admin VRE contains all the types without prefix like person, document, etc.
    return admin.getOwnType(vertexTypes);
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
        addType(vertex, entityTypeName);

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

  private void addType(Vertex vertex, String entityTypeName) {
    // add label
    if (vertex instanceof Neo4jVertex) {
      ((Neo4jVertex) vertex).addLabel(entityTypeName);
    }

    // add type to types property
    getEntityTypes(vertex).ifPresent(x -> {
      if (x.isFailure()) {
        LOG.error("vertex id {} with wrong types property", vertex.<String>value("tim_id"));
      }
    });
    String[] typesArray = getEntityTypesOrDefault(vertex);
    List<String> types = Lists.newArrayList(typesArray);
    types.add(entityTypeName);
    vertex.property("types").remove();
    try {
      vertex.property("types", objectMapper.writeValueAsString(types));
    } catch (JsonProcessingException e) {
      throw new RuntimeException("Cannot write types property", e);
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
