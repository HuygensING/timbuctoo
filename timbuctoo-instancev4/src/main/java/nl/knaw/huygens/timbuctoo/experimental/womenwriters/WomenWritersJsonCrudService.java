package nl.knaw.huygens.timbuctoo.experimental.womenwriters;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import nl.knaw.huygens.timbuctoo.crud.InvalidCollectionException;
import nl.knaw.huygens.timbuctoo.crud.NotFoundException;
import nl.knaw.huygens.timbuctoo.crud.UrlGenerator;
import nl.knaw.huygens.timbuctoo.database.DataAccess;
import nl.knaw.huygens.timbuctoo.database.dto.ReadEntity;
import nl.knaw.huygens.timbuctoo.database.dto.RelationRef;
import nl.knaw.huygens.timbuctoo.database.dto.WwReadEntity;
import nl.knaw.huygens.timbuctoo.database.dto.WwRelationRef;
import nl.knaw.huygens.timbuctoo.database.dto.property.JsonPropertyConverter;
import nl.knaw.huygens.timbuctoo.model.Change;
import nl.knaw.huygens.timbuctoo.model.vre.Collection;
import nl.knaw.huygens.timbuctoo.model.vre.Vres;
import nl.knaw.huygens.timbuctoo.security.AuthenticationUnavailableException;
import nl.knaw.huygens.timbuctoo.security.UserStore;
import nl.knaw.huygens.timbuctoo.util.Tuple;
import org.neo4j.helpers.Strings;
import org.slf4j.Logger;

import java.io.IOException;
import java.util.List;
import java.util.UUID;

import static java.util.stream.Collectors.groupingBy;
import static nl.knaw.huygens.timbuctoo.logging.Logmarkers.databaseInvariant;
import static nl.knaw.huygens.timbuctoo.util.JsonBuilder.jsn;
import static nl.knaw.huygens.timbuctoo.util.JsonBuilder.jsnA;
import static nl.knaw.huygens.timbuctoo.util.JsonBuilder.jsnO;
import static nl.knaw.huygens.timbuctoo.util.Tuple.tuple;

public class WomenWritersJsonCrudService {

  private static final Logger LOG = org.slf4j.LoggerFactory.getLogger(WomenWritersJsonCrudService.class);

  private final Vres mappings;
  private final UrlGenerator relationUrlFor;
  private final UserStore userStore;
  private final DataAccess dataAccess;

  public WomenWritersJsonCrudService(Vres mappings,
                                     UserStore userStore,
                                     UrlGenerator relationUrlFor,
                                     DataAccess dataAccess) {
    this.mappings = mappings;
    this.relationUrlFor = relationUrlFor;
    this.userStore = userStore;
    this.dataAccess = dataAccess;

  }


  public JsonNode get(String collectionName, UUID id) throws InvalidCollectionException, NotFoundException {
    return get(collectionName, id, null);
  }

  public JsonNode get(String collectionName, UUID id, Integer rev)
    throws InvalidCollectionException, NotFoundException {

    final Collection collection = mappings.getCollection(collectionName)
                                          .orElseThrow(() -> new InvalidCollectionException(collectionName));

    if (collection.isRelationCollection()) {
      return jsnO("message", jsn("Getting a wwrelation is not yet supported"));
    } else {
      return getEntity(id, rev, collection);
    }
  }

  private JsonNode getEntity(UUID id, Integer rev, Collection collection) throws NotFoundException {
    ObjectNode result;
    try (DataAccess.DataAccessMethods db = dataAccess.start()) {
      try {
        WwReadEntity entity = db.getWwEntity(id, rev, collection);
        result = mapEntity(collection, entity, true);
        result.set("@authorLanguages", jsnA(entity.getLanguages().stream().map(lang -> jsn(lang))));
        db.success();
      } catch (NotFoundException e) {
        db.rollback();
        throw e;
      }
    }

    return result;
  }

  private ObjectNode mapEntity(Collection collection, ReadEntity entity, boolean withRelations) {
    final ObjectNode mappedEntity = JsonNodeFactory.instance.objectNode();
    String id = entity.getId().toString();

    mappedEntity.set("@type", jsn(collection.getEntityTypeName()));
    mappedEntity.set("_id", jsn(id));

    mappedEntity.set("^rev", jsn(entity.getRev()));
    mappedEntity.set("^deleted", jsn(entity.getDeleted()));
    mappedEntity.set("^pid", jsn(entity.getPid()));

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

    if (withRelations) {
      mappedEntity.set("@relationCount", jsn(entity.getRelations().size()));
      mappedEntity.set("@relations", mapRelations(entity.getRelations()));
    }
    return mappedEntity;
  }

  private JsonNode mapRelations(List<RelationRef> relations) {
    ObjectNode relationsNode = jsnO();
    relations.stream().map(relationRef -> mapRelationRef(relationRef)
    ).collect(groupingBy(x -> x.get("relationType").asText())).entrySet().forEach(relationsType ->
      relationsNode.set(relationsType.getKey(), jsnA(relationsType.getValue().stream())));

    return relationsNode;
  }

  private ObjectNode mapRelationRef(RelationRef relationRef) {
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

    if (relationRef instanceof WwRelationRef) {
      result.set("gender", jsn(((WwRelationRef) relationRef).getGender()));
      result.set("authors", jsnA(((WwRelationRef) relationRef)
        .getAuthors().stream()
        .map(author -> jsnO("displayName", jsn(author.getLeft()), "gender", jsn(author.getRight())))
      ));
    }

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
