package nl.knaw.huygens.timbuctoo.crud;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.common.collect.Lists;
import nl.knaw.huygens.timbuctoo.database.DataAccess;
import nl.knaw.huygens.timbuctoo.database.DataAccessMethods;
import nl.knaw.huygens.timbuctoo.database.TimbuctooDbAccess;
import nl.knaw.huygens.timbuctoo.database.converters.json.EntityToJsonMapper;
import nl.knaw.huygens.timbuctoo.database.converters.json.JsonPropertyConverter;
import nl.knaw.huygens.timbuctoo.database.dto.CreateEntity;
import nl.knaw.huygens.timbuctoo.database.dto.DataStream;
import nl.knaw.huygens.timbuctoo.database.dto.ReadEntity;
import nl.knaw.huygens.timbuctoo.database.dto.UpdateEntity;
import nl.knaw.huygens.timbuctoo.database.dto.dataset.Collection;
import nl.knaw.huygens.timbuctoo.database.dto.property.TimProperty;
import nl.knaw.huygens.timbuctoo.database.exceptions.RelationNotPossibleException;
import nl.knaw.huygens.timbuctoo.database.exceptions.UnknownPropertyException;
import nl.knaw.huygens.timbuctoo.model.properties.LocalProperty;
import nl.knaw.huygens.timbuctoo.model.vre.Vres;
import nl.knaw.huygens.timbuctoo.security.AuthorizationException;
import nl.knaw.huygens.timbuctoo.security.AuthorizationUnavailableException;
import nl.knaw.huygens.timbuctoo.security.UserStore;
import org.slf4j.Logger;

import java.io.IOException;
import java.time.Clock;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.toSet;
import static nl.knaw.huygens.timbuctoo.util.JsonBuilder.jsn;
import static nl.knaw.huygens.timbuctoo.util.JsonBuilder.jsnO;
import static nl.knaw.huygens.timbuctoo.util.StreamIterator.stream;

public class JsonCrudService {

  private static final Logger LOG = org.slf4j.LoggerFactory.getLogger(JsonCrudService.class);

  private final Vres mappings;
  private final Clock clock;
  private final DataAccess dataAccess;
  private final TimbuctooDbAccess timDbAccess;
  private final EntityToJsonMapper entityToJsonMapper;

  public JsonCrudService(Vres mappings, UserStore userStore, UrlGenerator relationUrlFor, Clock clock,
                         DataAccess dataAccess, TimbuctooDbAccess timDbAccess) {
    this.mappings = mappings;
    this.clock = clock;
    this.dataAccess = dataAccess;
    this.timDbAccess = timDbAccess;
    entityToJsonMapper = new EntityToJsonMapper(userStore, relationUrlFor);
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

    try (DataAccessMethods db = dataAccess.start()) {
      try {
        UUID relationId = db.acceptRelation(
          sourceId,
          typeId,
          targetId,
          collection,
          userId,
          clock.instant()
        );
        db.success();
        return relationId;
      } catch (RelationNotPossibleException e) {
        db.rollback();
        throw new IOException(e.getMessage(), e);
      } catch (AuthorizationException | AuthorizationUnavailableException e) {
        db.rollback();
        throw e;
      }
    }
  }

  private UUID createEntity(Collection collection, ObjectNode input, String userId)
    throws IOException, AuthorizationException, AuthorizationUnavailableException {

    List<TimProperty<?>> properties = getDataProperties(collection, input);
    CreateEntity createEntity = new CreateEntity(properties);

    Optional<Collection> baseCollection = mappings.getCollectionForType(collection.getAbstractType());

    UUID id = timDbAccess.createEntity(collection, baseCollection, createEntity, userId);

    return id;
  }

  /**
   * Retrieve all the properties that contain client data.
   */
  private List<TimProperty<?>> getDataProperties(Collection collection, ObjectNode input) throws IOException {
    JsonPropertyConverter converter = new JsonPropertyConverter(collection);

    Set<String> fieldNames = getDataFields(input);
    List<TimProperty<?>> properties = Lists.newArrayList();
    for (String fieldName : fieldNames) {
      try {
        properties.add(converter.from(fieldName, input.get(fieldName)));
      } catch (UnknownPropertyException e) {
        LOG.error("Property with name '{}' is unknown for collection '{}'.", fieldName,
          collection.getCollectionName());
        throw new IOException(
          String.format("Items of %s have no property %s", collection.getCollectionName(), fieldName));
      } catch (IOException e) {
        LOG.error("Property '{}' with value '{}' could not be converted", fieldName, input.get(fieldName));
        throw new IOException(
          String.format("Property '%s' could not be converted. %s", fieldName, e.getMessage()),
          e
        );
      }
    }
    return properties;
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

    DataStream<ReadEntity> entities = timDbAccess.getCollection(collection, rows, start,withRelations,
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

    try (DataAccessMethods db = dataAccess.start()) {
      try {
        db.replaceRelation(collection, id, rev.asInt(), accepted.asBoolean(), userId, clock.instant());
        db.success();
      } catch (NotFoundException | AuthorizationUnavailableException | AuthorizationException e) {
        db.rollback();
        throw e;
      }
    }
  }

  private void replaceEntity(Collection collection, UUID id, ObjectNode data, String userId)
    throws NotFoundException, IOException, AlreadyUpdatedException, AuthorizationException,
    AuthorizationUnavailableException {

    if (data.get("^rev") == null) {
      throw new IOException("data object should have a ^rev property indicating the revision this update was based on");
    }
    int rev = data.get("^rev").asInt();

    List<TimProperty<?>> properties =
      getDataProperties(collection, data);

    UpdateEntity updateEntity = new UpdateEntity(id, properties, rev, clock.instant());

    final Map<String, LocalProperty> collectionProperties = collection.getWriteableProperties();

    Set<String> propertyNames =
      updateEntity.getProperties().stream().map(prop -> prop.getName()).collect(Collectors.toSet());

    for (String name : propertyNames) {
      if (!collectionProperties.containsKey(name)) {
        throw new IOException(name + " is not a valid property");
      }
    }

    timDbAccess.replaceEntity(collection, updateEntity, userId);
  }

  private Set<String> getDataFields(ObjectNode data) {
    return stream(data.fieldNames())
      .filter(x -> !x.startsWith("@"))
      .filter(x -> !x.startsWith("^"))
      .filter(x -> !Objects.equals(x, "_id"))
      .collect(toSet());
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
