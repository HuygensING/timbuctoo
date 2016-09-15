package nl.knaw.huygens.timbuctoo.database.dto;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.Lists;
import javaslang.control.Try;
import nl.knaw.huygens.timbuctoo.database.dto.property.TimProperty;
import nl.knaw.huygens.timbuctoo.database.dto.property.TinkerPopPropertyConverter;
import nl.knaw.huygens.timbuctoo.database.dto.property.UnknownPropertyException;
import nl.knaw.huygens.timbuctoo.model.Change;
import nl.knaw.huygens.timbuctoo.model.properties.ReadableProperty;
import nl.knaw.huygens.timbuctoo.model.vre.Collection;
import nl.knaw.huygens.timbuctoo.model.vre.Vre;
import nl.knaw.huygens.timbuctoo.model.vre.Vres;
import org.apache.tinkerpop.gremlin.neo4j.process.traversal.LabelP;
import org.apache.tinkerpop.gremlin.process.traversal.P;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.GraphTraversal;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.GraphTraversalSource;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.__;
import org.apache.tinkerpop.gremlin.structure.Edge;
import org.apache.tinkerpop.gremlin.structure.T;
import org.apache.tinkerpop.gremlin.structure.Vertex;
import org.apache.tinkerpop.gremlin.structure.util.empty.EmptyGraph;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static java.util.stream.Collectors.toList;
import static nl.knaw.huygens.timbuctoo.logging.Logmarkers.databaseInvariant;
import static nl.knaw.huygens.timbuctoo.model.GraphReadUtils.getEntityTypesOrDefault;
import static nl.knaw.huygens.timbuctoo.model.GraphReadUtils.getProp;
import static nl.knaw.huygens.timbuctoo.util.StreamIterator.stream;

public class TinkerPopToEntityMapper {

  private static final Logger LOG = LoggerFactory.getLogger(TinkerPopToEntityMapper.class);
  private final Collection collection;
  private final GraphTraversalSource traversalSource;
  private final Vres mappings;

  public TinkerPopToEntityMapper(Collection collection, GraphTraversalSource traversalSource, Vres mappings) {
    this.collection = collection;
    this.traversalSource = traversalSource;
    this.mappings = mappings;
  }

  public ReadEntity mapEntity(Vertex next) {
    return mapEntity(traversalSource.V(next.id()));
  }

  public ReadEntity mapEntity(GraphTraversal<Vertex, Vertex> entityT) {
    final List<TimProperty<?>> properties = Lists.newArrayList();
    TinkerPopPropertyConverter dbPropertyConverter = new TinkerPopPropertyConverter(collection);
    String entityTypeName = collection.getEntityTypeName();

    GraphTraversal[] propertyGetters = collection
      .getReadableProperties().entrySet().stream()
      //append error handling and resulting to the traversal
      .map(prop -> prop.getValue().traversalRaw().sideEffect(x ->
        x.get()
         .onSuccess(value -> {
           try {
             properties.add(dbPropertyConverter.from(prop.getKey(), value));
           } catch (UnknownPropertyException e) {
             LOG.error("Unknown property", e);
           } catch (IOException e) {
             LOG.error(
               databaseInvariant,
               "Property '" + prop.getKey() + "' is not encoded correctly",
               e.getCause()
             );
           }
         })
         .onFailure(e -> {
           if (e.getCause() instanceof IOException) {
             LOG.error(
               databaseInvariant,
               "Property '" + prop.getKey() + "' is not encoded correctly",
               e.getCause()
             );
           } else {
             LOG.error("Something went wrong while reading the property '" + prop.getKey() + "'.", e.getCause());
           }
         })
      ))
      .toArray(GraphTraversal[]::new);

    entityT.asAdmin().clone().union(propertyGetters).forEachRemaining(x -> {
      //Force side effects to happen
    });

    ReadEntity entity = new ReadEntity();
    entity.setProperties(properties);

    Vertex entityVertex = entityT.asAdmin().clone().next();
    // TODO make use converters for the types
    entity.setRev(getProp(entityVertex, "rev", Integer.class).orElse(-1));
    entity.setDeleted(getProp(entityVertex, "deleted", Boolean.class).orElse(false));
    entity.setPid(getProp(entityVertex, "pid", String.class).orElse(null));


    Optional<String> typesOptional = getProp(entityVertex, "types", String.class);
    if (typesOptional.isPresent()) {
      try {
        List<String> types = new ObjectMapper().readValue(typesOptional.get(), new TypeReference<List<String>>() {
        });
        entity.setTypes(types);
      } catch (Exception e) {
        LOG.error(databaseInvariant, "Error while generating variation refs", e);
        entity.setTypes(Lists.newArrayList(entityTypeName));
      }
    } else {
      entity.setTypes(Lists.newArrayList(entityTypeName));
    }

    Optional<String> modifiedStringOptional = getProp(entityVertex, "modified", String.class);
    if (modifiedStringOptional.isPresent()) {
      try {
        entity.setModified(new ObjectMapper().readValue(modifiedStringOptional.get(), Change.class));
      } catch (IOException e) {
        LOG.error(databaseInvariant, "Change cannot be converted", e);
        entity.setModified(new Change());
      }
    } else {
      entity.setModified(new Change());
    }

    Optional<String> createdStringOptional = getProp(entityVertex, "created", String.class);
    if (createdStringOptional.isPresent()) {
      try {
        entity.setCreated(new ObjectMapper().readValue(createdStringOptional.get(), Change.class));
      } catch (IOException e) {
        LOG.error(databaseInvariant, "Change cannot be converted", e);
        entity.setCreated(new Change());
      }
    } else {
      entity.setCreated(new Change());
    }

    entity.setDisplayName(getDisplayname(traversalSource, entityVertex, collection).orElse(""));
    entity.setId(UUID.fromString(entityVertex.value("tim_id")));

    entity.setRelations(getRelations(entityVertex, traversalSource, collection));
    return entity;
  }

  private List<RelationRef> getRelations(Vertex entity, GraphTraversalSource traversalSource,
                                         Collection collection) {
    final Vre vre = collection.getVre();
    Vre adminVre = mappings.getVre("Admin");
    Map<String, Collection> collectionsOfVre = vre.getCollections();

    Object[] relationTypes = traversalSource.V().has(T.label, LabelP.of("relationtype")).id().toList().toArray();

    GraphTraversal<Vertex, RelationRef> realRelations = collectionsOfVre
      .values().stream()
      .filter(Collection::isRelationCollection)
      .findAny()
      .map(Collection::getEntityTypeName)
      .map(ownRelationType -> traversalSource
        .V(entity.id())
        .union(
          __.outE()
            .as("edge")
            .label().as("label")
            .select("edge"),
          __.inE()
            .as("edge")
            .label().as("edgeLabel")
            .V(relationTypes)
            .has("relationtype_regularName",
              __.where(P.eq("edgeLabel")))
            .properties("relationtype_inverseName").value()
            .as("label")
            .select("edge")
        )
        .where(
          //FIXME move to strategy
          __.has("isLatest", true)
            .not(__.has("deleted", true))
            .not(__.hasLabel("VERSION_OF"))
            //The old timbuctoo showed relations from all
            // VRE's.
            // Changing that behaviour caused breakage in
            // the
            //frontend and exposed errors in the database
            // that
            //.has("types", new P<>((val, def) -> val
            // .contains
            // ("\"" + ownRelationType + "\""), ""))
            // FIXME: string concatenating methods like this
            // should be delegated to a configuration clas
            .not(
              __.has(ownRelationType + "_accepted", false))
        )
        .otherV().as("vertex")
        .select("edge", "vertex", "label")
        .map(r -> {
          try {
            Map<String, Object> val = r.get();
            Edge edge = (Edge) val.get("edge");
            Vertex target = (Vertex) val.get("vertex");
            String label = (String) val.get("label");

            String targetEntityType = vre.getOwnType(getEntityTypesOrDefault(target));
            Collection targetCollection = vre.getCollectionForTypeName(targetEntityType);
            if (targetEntityType == null) {
              //this means that the edge is of this VRE, but the
              // Vertex it points to is of another VRE
              //In that case we use the admin vre
              targetEntityType = adminVre.getOwnType(getEntityTypesOrDefault(target));
              targetCollection = adminVre.getCollectionForTypeName(targetEntityType);
            }

            String displayName = getDisplayname(traversalSource, target, targetCollection)
              .orElse("<No displayname found>");
            String uuid = getProp(target, "tim_id", String.class).orElse("");
            boolean accepted = getProp(edge, "accepted", Boolean.class).orElse(true);
            String relationId =
              getProp(edge, "tim_id", String.class)
                .orElse("");
            int relationRev = getProp(edge, "rev", Integer.class).orElse(1);

            return new RelationRef(uuid, targetCollection.getCollectionName(), targetEntityType, accepted, relationId,
              relationRev, label, displayName);
          } catch (Exception e) {
            LOG.error(databaseInvariant,
              "Something went wrong while formatting the entity",
              e);
            return null;
          }
        })
      )
      .orElse(EmptyGraph.instance().traversal().V()
                        .map(x -> null));

    List<RelationRef> relations = stream(realRelations)
      .filter(x -> x != null).collect(toList());

    return relations;
  }

  private Optional<String> getDisplayname(GraphTraversalSource traversalSource, Vertex vertex,
                                          Collection targetCollection) {
    ReadableProperty displayNameProperty = targetCollection.getDisplayName();
    if (displayNameProperty != null) {
      GraphTraversal<Vertex, Try<JsonNode>> displayNameGetter = traversalSource.V(vertex.id()).union(
        targetCollection.getDisplayName().traversalJson()
      );
      if (displayNameGetter.hasNext()) {
        Try<JsonNode> traversalResult = displayNameGetter.next();
        if (!traversalResult.isSuccess()) {
          LOG.error(databaseInvariant, "Retrieving displayname failed", traversalResult.getCause());
        } else {
          if (traversalResult.get() == null) {
            LOG.error(databaseInvariant, "Displayname was null");
          } else {
            if (!traversalResult.get().isTextual()) {
              LOG.error(databaseInvariant, "Displayname was not a string but " + traversalResult.get().toString());
            } else {
              return Optional.of(traversalResult.get().asText());
            }
          }
        }
      } else {
        LOG.error(databaseInvariant, "Displayname traversal resulted in no results: " + displayNameGetter);
      }
    } else {
      LOG.warn("No displayname configured for " + targetCollection.getEntityTypeName());
    }
    return Optional.empty();
  }
}
