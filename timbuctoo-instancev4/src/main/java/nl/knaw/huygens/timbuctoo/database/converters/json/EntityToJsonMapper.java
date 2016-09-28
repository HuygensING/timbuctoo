package nl.knaw.huygens.timbuctoo.database.converters.json;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import nl.knaw.huygens.timbuctoo.crud.UrlGenerator;
import nl.knaw.huygens.timbuctoo.database.dto.ReadEntity;
import nl.knaw.huygens.timbuctoo.database.dto.RelationRef;
import nl.knaw.huygens.timbuctoo.database.dto.dataset.Collection;
import nl.knaw.huygens.timbuctoo.model.Change;
import nl.knaw.huygens.timbuctoo.security.AuthenticationUnavailableException;
import nl.knaw.huygens.timbuctoo.security.UserStore;
import nl.knaw.huygens.timbuctoo.util.Tuple;
import org.neo4j.helpers.Strings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.List;
import java.util.UUID;

import static java.util.stream.Collectors.groupingBy;
import static nl.knaw.huygens.timbuctoo.logging.Logmarkers.databaseInvariant;
import static nl.knaw.huygens.timbuctoo.util.JsonBuilder.jsn;
import static nl.knaw.huygens.timbuctoo.util.JsonBuilder.jsnA;
import static nl.knaw.huygens.timbuctoo.util.JsonBuilder.jsnO;
import static nl.knaw.huygens.timbuctoo.util.Tuple.tuple;

public class EntityToJsonMapper {
  private static final Logger LOG = LoggerFactory.getLogger(EntityToJsonMapper.class);

  private final UserStore userStore;
  private final UrlGenerator relationUrlFor;

  public EntityToJsonMapper(UserStore userStore, UrlGenerator relationUrlFor) {
    this.userStore = userStore;
    this.relationUrlFor = relationUrlFor;
  }


  public interface ExtraEntityMappingOptions {
    void execute(ReadEntity readEntity, ObjectNode resultJson);
  }

  public interface ExtraRelationMappingOptions {
    void execute(RelationRef relationRef, ObjectNode resultJson);
  }

  public ObjectNode mapEntity(Collection collection, ReadEntity entity, boolean withRelations,
                              ExtraEntityMappingOptions extraEntityMappingOptions,
                              ExtraRelationMappingOptions relationMappingOptions) {
    final ObjectNode mappedEntity = JsonNodeFactory.instance.objectNode();
    String id = entity.getId().toString();

    mappedEntity.set("@type", jsn(collection.getEntityTypeName()));
    mappedEntity.set("_id", jsn(id));

    mappedEntity.set("^rev", jsn(entity.getRev()));
    mappedEntity.set("^deleted", jsn(entity.getDeleted()));
    mappedEntity.set("^pid", jsn(entity.getPid()));
    if (entity.getRdfUri() != null) {
      mappedEntity.set("^rdfUri", jsn(entity.getRdfUri().toString()));
    }

    JsonNode variationRefs = jsnA(entity.getTypes()
                                        .stream()
                                        .map(type -> {
                                          ObjectNode variationRef = jsnO();
                                          variationRef.set("id", jsn(id));
                                          variationRef.set("type", jsn(type));
                                          return variationRef;
                                        }));
    mappedEntity.set("@variationRefs", variationRefs);

    Change modified = entity.getModified();
    mappedEntity.set("^modified", mapChange(modified));
    Change created = entity.getCreated();
    mappedEntity.set("^created", mapChange(created));

    // translate TimProperties to Json
    JsonPropertyConverter jsonPropertyConverter = new JsonPropertyConverter(collection);
    entity.getProperties().forEach(prop -> {
      Tuple<String, JsonNode> convertedProperty = null;
      try {
        convertedProperty = prop.convert(jsonPropertyConverter);
      } catch (IOException e) {
        LOG.error(
          databaseInvariant,
          "Property '" + prop.getName() + "' is not encoded correctly",
          e.getCause()
        );
      }
      mappedEntity.set(convertedProperty.getLeft(), convertedProperty.getRight());
    });

    if (!Strings.isBlank(entity.getDisplayName())) {
      mappedEntity.set("@displayName", jsn(entity.getDisplayName()));
    }

    extraEntityMappingOptions.execute(entity, mappedEntity);

    if (withRelations) {
      mappedEntity.set("@relationCount", jsn(entity.getRelations().size()));
      mappedEntity.set("@relations", mapRelations(entity.getRelations(), relationMappingOptions));
    }
    return mappedEntity;
  }

  private JsonNode mapRelations(List<RelationRef> relations,
                                ExtraRelationMappingOptions relationMappingOptions) {
    ObjectNode relationsNode = jsnO();
    relations.stream().map(relationRef -> mapRelationRef(relationRef, relationMappingOptions)
    ).collect(groupingBy(x -> x.get("relationType").asText())).entrySet().forEach(relationsType ->
      relationsNode.set(relationsType.getKey(), jsnA(relationsType.getValue().stream())));

    return relationsNode;
  }

  private ObjectNode mapRelationRef(RelationRef relationRef,
                                    ExtraRelationMappingOptions relationMappingOptions) {
    ObjectNode result = jsnO(
      tuple("id", jsn(relationRef.getEntityId())),
      tuple("path", jsn(relationUrlFor.apply(relationRef.getCollectionName(),
        UUID.fromString(relationRef.getEntityId()), null).toString())),
      tuple("relationType", jsn(relationRef.getRelationType())),
      tuple("type", jsn(relationRef.getEntityType())),
      tuple("accepted", jsn(relationRef.isRelationAccepted())),
      tuple("relationId", jsn(relationRef.getRelationId())),
      tuple("rev", jsn(relationRef.getRelationRev())),
      tuple("displayName", jsn(relationRef.getDisplayName()))
    );

    relationMappingOptions.execute(relationRef, result);

    return result;
  }

  private JsonNode mapChange(Change change) {
    String userId = change.getUserId();
    ObjectNode changeNode = new ObjectMapper().valueToTree(change);

    try {
      userStore.userForId(userId).ifPresent(user -> changeNode.set("username", jsn(user.getDisplayName())));
    } catch (AuthenticationUnavailableException e) {
      LOG.error("Could not retrieve user store", e);
    }

    return changeNode;
  }

}
