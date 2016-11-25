package nl.knaw.huygens.timbuctoo.crud;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import nl.knaw.huygens.timbuctoo.database.AlreadyUpdatedException;
import nl.knaw.huygens.timbuctoo.database.NotFoundException;
import nl.knaw.huygens.timbuctoo.database.TimbuctooActions;
import nl.knaw.huygens.timbuctoo.database.converters.json.EntityToJsonMapper;
import nl.knaw.huygens.timbuctoo.database.converters.json.JsonToEntityMapper;
import nl.knaw.huygens.timbuctoo.database.dto.CreateRelation;
import nl.knaw.huygens.timbuctoo.database.dto.DataStream;
import nl.knaw.huygens.timbuctoo.database.dto.ReadEntity;
import nl.knaw.huygens.timbuctoo.database.dto.UpdateEntity;
import nl.knaw.huygens.timbuctoo.database.dto.UpdateRelation;
import nl.knaw.huygens.timbuctoo.database.dto.dataset.Collection;
import nl.knaw.huygens.timbuctoo.database.dto.property.TimProperty;
import nl.knaw.huygens.timbuctoo.model.vre.Vres;
import nl.knaw.huygens.timbuctoo.security.AuthorizationException;
import nl.knaw.huygens.timbuctoo.security.AuthorizationUnavailableException;
import nl.knaw.huygens.timbuctoo.security.UserStore;

import java.io.IOException;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static nl.knaw.huygens.timbuctoo.util.JsonBuilder.jsn;
import static nl.knaw.huygens.timbuctoo.util.JsonBuilder.jsnO;

public class JsonCrudService {

  private final Vres mappings;
  private final TimbuctooActions timDbAccess;
  private final EntityToJsonMapper entityToJsonMapper;
  private final JsonToEntityMapper jsonToEntityMapper;

  public JsonCrudService(Vres mappings, UserStore userStore, UrlGenerator relationUrlFor,
                         TimbuctooActions timDbAccess) {
    this.mappings = mappings;
    this.timDbAccess = timDbAccess;
    entityToJsonMapper = new EntityToJsonMapper(userStore, relationUrlFor);
    jsonToEntityMapper = new JsonToEntityMapper();
  }

  public UUID create(String collectionName, ObjectNode input, String userId)
    throws InvalidCollectionException, IOException, AuthorizationException {

    final Collection collection = mappings.getCollection(collectionName)
                                          .orElseThrow(() -> new InvalidCollectionException(collectionName));

    try {
      if (collection.isRelationCollection()) {
        return createRelation(collection, input, userId);
      } else {
        return createEntity(collection, input, userId);
      }
    } catch (AuthorizationUnavailableException e) {
      throw new IOException(e.getMessage());
    }
  }

  private UUID asUuid(ObjectNode input, String fieldName) throws IOException {
    try {
      return UUID.fromString(input.get(fieldName).asText(""));
    } catch (IllegalArgumentException | NullPointerException e) {
      throw new IOException(fieldName + " must contain a valid UUID", e);
    }
  }

  private UUID createRelation(Collection collection, ObjectNode input, String userId)
    throws IOException, AuthorizationException, AuthorizationUnavailableException {

    UUID sourceId = asUuid(input, "^sourceId");
    UUID targetId = asUuid(input, "^targetId");
    UUID typeId = asUuid(input, "^typeId");


    CreateRelation createRelation = new CreateRelation(sourceId, typeId, targetId);

    return timDbAccess.createRelation(collection, createRelation, userId);
  }

  private UUID createEntity(Collection collection, ObjectNode input, String userId)
    throws IOException, AuthorizationException, AuthorizationUnavailableException {

    List<TimProperty<?>> timProperties = jsonToEntityMapper.getDataProperties(collection, input);

    Optional<Collection> baseCollection = mappings.getCollectionForType(collection.getAbstractType());

    UUID id = timDbAccess.createEntity(collection, baseCollection, timProperties, userId);

    return id;
  }

  public JsonNode get(String collectionName, UUID id) throws InvalidCollectionException, NotFoundException {
    return get(collectionName, id, null);
  }

  public JsonNode get(String collectionName, UUID id, Integer rev)
    throws InvalidCollectionException, NotFoundException {

    final Collection collection = mappings.getCollection(collectionName)
                                          .orElseThrow(() -> new InvalidCollectionException(collectionName));

    if (collection.isRelationCollection()) {
      return jsnO("message", jsn("Getting a relation is not yet supported"));
    } else {
      return getEntity(id, rev, collection);
    }
  }

  private JsonNode getEntity(UUID id, Integer rev, Collection collection) throws NotFoundException {

    ReadEntity entity = timDbAccess.getEntity(collection, id, rev,
      (entity1, entityVertex) -> {

      },
      (traversalSource, vre, target, relationRef) -> {

      });

    ObjectNode result = entityToJsonMapper.mapEntity(collection, entity, true,
      (readEntity, resultJson) -> {
      },
      (relationRef, resultJson) -> {
      }
    );

    return result;
  }

  public List<ObjectNode> getCollection(String collectionName, int rows, int start, boolean withRelations)
    throws InvalidCollectionException {
    final Collection collection = mappings.getCollection(collectionName)
                                          .orElseThrow(() -> new InvalidCollectionException(collectionName));

    DataStream<ReadEntity> entities = timDbAccess.getCollection(collection, start, rows, withRelations,
      (traversalSource, vre) -> {

      },
      (entity1, entityVertex, target, relationRef) -> {

      }
    );
    List<ObjectNode> result = entities.map(entity ->
      entityToJsonMapper.mapEntity(collection, entity, withRelations,
        (readEntity, resultJson) -> {
        },
        (relationRef, resultJson) -> {
        })
    );
    return result;
  }


  public void replace(String collectionName, UUID id, ObjectNode data, String userId)
    throws InvalidCollectionException, IOException, NotFoundException, AlreadyUpdatedException, AuthorizationException,
    AuthorizationUnavailableException {

    final Collection collection = mappings.getCollection(collectionName)
                                          .orElseThrow(() -> new InvalidCollectionException(collectionName));


    if (collection.isRelationCollection()) {
      replaceRelation(collection, id, data, userId);
    } else {
      replaceEntity(collection, id, data, userId);
    }
  }

  private void replaceRelation(Collection collection, UUID id, ObjectNode data, String userId)
    throws IOException, NotFoundException, AuthorizationException, AuthorizationUnavailableException {

    JsonNode accepted = data.get("accepted");
    if (accepted == null || !accepted.isBoolean()) {
      throw new IOException("Only the property accepted can be updated. It must be a boolean");
    }
    JsonNode rev = data.get("^rev");
    if (rev == null || !rev.isNumber()) {
      throw new IOException("^rev must be a number");
    }

    for (Iterator<String> fieldNames = data.fieldNames(); fieldNames.hasNext(); ) {
      String name = fieldNames.next();
      if (!name.startsWith("^") && !name.startsWith("@") && !name.equals("_id") && !name.equals("accepted")) {
        throw new IOException("Only 'accepted' is a changeable property");
      }
    }

    UpdateRelation updateRelation = new UpdateRelation(id, rev.asInt(), accepted.asBoolean());

    timDbAccess.replaceRelation(collection, updateRelation, userId);

  }

  private void replaceEntity(Collection collection, UUID id, ObjectNode data, String userId)
    throws NotFoundException, IOException, AlreadyUpdatedException, AuthorizationException,
    AuthorizationUnavailableException {

    UpdateEntity updateEntity = jsonToEntityMapper.newUpdateEntity(collection, id, data);

    timDbAccess.replaceEntity(collection, updateEntity, userId);
  }

  public void delete(String collectionName, UUID id, String userId)
    throws InvalidCollectionException, NotFoundException, AuthorizationException, IOException {

    final Collection collection = mappings.getCollection(collectionName)
                                          .orElseThrow(() -> new InvalidCollectionException(collectionName));
    try {
      timDbAccess.deleteEntity(collection, id, userId);
    } catch (AuthorizationUnavailableException e) {
      throw new IOException(e.getMessage());
    }

  }
}
