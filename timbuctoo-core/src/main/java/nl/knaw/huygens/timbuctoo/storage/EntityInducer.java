package nl.knaw.huygens.timbuctoo.storage;

import static com.google.common.base.Preconditions.checkArgument;
import static nl.knaw.huygens.timbuctoo.config.TypeRegistry.isDomainEntity;
import static nl.knaw.huygens.timbuctoo.config.TypeRegistry.isSystemEntity;

import java.io.IOException;
import java.util.Map;

import nl.knaw.huygens.timbuctoo.config.BusinessRules;
import nl.knaw.huygens.timbuctoo.config.TypeRegistry;
import nl.knaw.huygens.timbuctoo.model.DomainEntity;
import nl.knaw.huygens.timbuctoo.model.Entity;
import nl.knaw.huygens.timbuctoo.model.SystemEntity;
import nl.knaw.huygens.timbuctoo.storage.mongo.MongoObjectMapper;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.Maps;

public class EntityInducer {

  private static final Logger LOG = LoggerFactory.getLogger(EntityInducer.class);

  protected final TypeRegistry typeRegistry;
  protected final ObjectMapper jsonMapper;
  protected final FieldMapper fieldMapper;
  protected final MongoObjectMapper propertyMapper;

  public EntityInducer(TypeRegistry registry) {
    typeRegistry = registry;
    jsonMapper = new ObjectMapper();
    fieldMapper = new FieldMapper();
    propertyMapper = new MongoObjectMapper(fieldMapper);
  }

  // --- public API ----------------------------------------------------

  /**
   * Converts an entity to a JsonTree.
   */
  @SuppressWarnings("unchecked")
  public <T extends Entity> JsonNode induceNewEntity(Class<? super T> type, T entity) throws IOException {
    checkArgument(type != null && (isSystemEntity(type) || isDomainEntity(type)));
    checkArgument(entity != null);

    if (isSystemEntity(type)) {
      return induceSystemEntity((Class<SystemEntity>) type, (SystemEntity) entity);
    } else {
      return induceDomainEntity((Class<DomainEntity>) type, (DomainEntity) entity);
    }
  }

  /**
   * Converts an entity to a Json tree and combines it with an existing Json tree.
   */
  @SuppressWarnings("unchecked")
  public <T extends Entity> JsonNode induceOldEntity(Class<T> type, T entity, JsonNode node) throws IOException {
    checkArgument(entity != null);
    checkArgument(node != null);

    if (isSystemEntity(type)) {
      return induceSystemEntity((Class<SystemEntity>) type, (SystemEntity) entity, node);
    } else {
      return induceDomainEntity(type, entity, node);
    }
  }

  // -------------------------------------------------------------------

  private <T extends SystemEntity> JsonNode induceSystemEntity(Class<T> type, T entity) {
    checkArgument(BusinessRules.allowSystemEntityAdd(type));

    Map<String, Object> map = Maps.newTreeMap();

    // Add (primitive) system entity
    Class<? super T> viewType = type;
    while (Entity.class.isAssignableFrom(viewType)) {
      propertyMapper.addObject(type, viewType, entity, map);
      viewType = viewType.getSuperclass();
    }

    return jsonMapper.valueToTree(map);
  }

  private <T extends SystemEntity> JsonNode induceSystemEntity(final Class<? super T> type, T entity, JsonNode existingItem) {
    Map<String, Object> map = Maps.newTreeMap();

    // TODO implement

    return jsonMapper.valueToTree(map);
  }

  private <T extends DomainEntity> JsonNode induceDomainEntity(final Class<T> type, T entity) {
    checkArgument(BusinessRules.allowDomainEntityAdd(type));

    Map<String, Object> map = Maps.newTreeMap();

    // Add (derived) domain entity
    Class<? super T> viewType = type;
    while (Entity.class.isAssignableFrom(viewType)) {
      propertyMapper.addObject(type, viewType, entity, map);
      viewType = viewType.getSuperclass();
    }

    // Add primitive domain entity
    Class<? super T> baseType = type.getSuperclass();
    propertyMapper.addObject(baseType, baseType, entity, map);

    return jsonMapper.valueToTree(map);
  }

  private <T extends Entity> JsonNode induceDomainEntity(Class<T> type, T entity, JsonNode existingItem) {
    checkArgument(isDomainEntity(type));
    checkArgument(existingItem != null);

    Map<String, Object> map = Maps.newHashMap();

    // TODO implement

    return jsonMapper.valueToTree(map);
  }

}
