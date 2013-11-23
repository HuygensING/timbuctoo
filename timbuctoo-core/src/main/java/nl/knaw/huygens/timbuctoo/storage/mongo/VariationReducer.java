package nl.knaw.huygens.timbuctoo.storage.mongo;

import static com.google.common.base.Preconditions.checkNotNull;

import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import nl.knaw.huygens.timbuctoo.config.TypeRegistry;
import nl.knaw.huygens.timbuctoo.model.DomainEntity;
import nl.knaw.huygens.timbuctoo.model.Entity;
import nl.knaw.huygens.timbuctoo.model.Role;
import nl.knaw.huygens.timbuctoo.model.Variable;
import nl.knaw.huygens.timbuctoo.storage.StorageException;

import org.mongojack.internal.stream.JacksonDBObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.google.common.base.Objects;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.mongodb.DBObject;

class VariationReducer extends VariationConverter {

  private static final Logger LOG = LoggerFactory.getLogger(VariationReducer.class);
  private static final String VERSIONS_FIELD = "versions";

  public VariationReducer(TypeRegistry registry) {
    super(registry);
  }

  protected Logger getLogger() {
    return LOG;
  }

  public <T extends Entity> MongoChanges<T> reduceMultipleRevisions(Class<T> type, DBObject obj) throws IOException {
    if (obj == null) {
      return null;
    }
    JsonNode tree = convertToTree(obj);
    ArrayNode versionsNode = (ArrayNode) tree.get(VERSIONS_FIELD);
    MongoChanges<T> changes = null;

    for (int i = 0; versionsNode.hasNonNull(i); i++) {
      T item = reduce(type, versionsNode.get(i));
      if (i == 0) {
        changes = new MongoChanges<T>(item.getId(), item);
      } else {
        changes.getRevisions().add(item);
      }
    }

    return changes;
  }

  public <T extends Entity> T reduceRevision(Class<T> type, DBObject obj) throws IOException {
    if (obj == null) {
      return null;
    }

    JsonNode tree = convertToTree(obj);
    ArrayNode versionsNode = (ArrayNode) tree.get(VERSIONS_FIELD);
    JsonNode objectToReduce = versionsNode.get(0);

    return reduce(type, objectToReduce);
  }

  public <T extends Entity> T reduceDBObject(Class<T> type, DBObject obj) throws IOException {
    return reduceDBObject(obj, type, null);
  }

  public <T extends Entity> T reduceDBObject(DBObject obj, Class<T> type, String variation) throws IOException {
    if (obj == null) {
      return null;
    }
    JsonNode tree = convertToTree(obj);
    return reduce(type, tree, variation);
  }

  public <T extends Entity> T reduce(Class<T> type, JsonNode node) throws StorageException, JsonProcessingException {
    return reduce(type, node, null);
  }

  @SuppressWarnings("unchecked")
  public <T extends Entity> T reduce(Class<T> type, JsonNode node, String requestedVariation) throws StorageException, JsonProcessingException {
    T returnObject = null;
    try {
      returnObject = type.newInstance();

      if (requestedVariation == null && DomainEntity.class.isAssignableFrom(type)) {
        requestedVariation = typeRegistry.getClassVariation((Class<? extends DomainEntity>) type);
      }

      setFields(type, Entity.class, returnObject, node, requestedVariation);

      String typeVariation = TypeRegistry.isDomainEntity(type) ? typeRegistry.getClassVariation((Class<? extends DomainEntity>) type) : null;

      Set<Class<? extends Role>> rolesInNode = getRolesInNode(node, typeVariation);
      List<Role> roles = Lists.newLinkedList();
      for (Class<? extends Role> roleType : rolesInNode) {
        roles.add(createRole(roleType, node, requestedVariation));
      }

      if (!roles.isEmpty()) {
        try {
          Method setRolesMethod = type.getMethod("setRoles", List.class);
          setRolesMethod.invoke(returnObject, roles);
        } catch (SecurityException e) {
          LOG.error("Could not access method setRoles of type {}.", type);
          LOG.debug("exception", e);
        } catch (NoSuchMethodException e) {
          LOG.error("Could not get method setRoles of type {}.", type);
          LOG.debug("exception", e);
        } catch (IllegalArgumentException e) {
          LOG.error("Method setRoles of type {} got a wrong parameter.", type);
          LOG.debug("exception", e);
        } catch (InvocationTargetException e) {
          LOG.error("Could not invoke method setRoles of type {}.", type);
          LOG.debug("exception", e);
        }
      }

    } catch (InstantiationException e) {
      LOG.error("Could not initialize object of type {}.", type);
      LOG.debug("exception", e);
    } catch (IllegalAccessException e) {
      LOG.error("Could not initialize object of type {}.", type);
      LOG.debug("exception", e);
    }

    return returnObject;
  }

  private <T extends Role> T createRole(Class<T> type, JsonNode node, String variation) throws InstantiationException, IllegalAccessException {
    T returnValue = type.newInstance();
    setFields(type, Role.class, returnValue, node, variation);
    return returnValue;
  }

  private Set<Class<? extends Role>> getRolesInNode(JsonNode node, String typeVariation) {
    Set<Class<? extends Role>> roles = Sets.newHashSet();

    Iterator<String> iterator = node.fieldNames();
    while (iterator.hasNext()) {
      String fieldName = iterator.next();
      String typeName = fieldMapper.getTypeNameOfFieldName(fieldName);

      if (typeRegistry.isRole(typeName)) {
        Class<? extends Role> role = typeRegistry.getRoleForIName(typeName);

        if (role != null && typeRegistry.getClassVariation(role) == null) {
          @SuppressWarnings("unchecked")
          Class<? extends Role> variationRole = (Class<? extends Role>) typeRegistry.getVariationClass(role, typeVariation);

          if (variationRole != null) {
            roles.add(variationRole);
          } else {
            roles.add(role);
          }
        } else if (role != null && Objects.equal(typeRegistry.getClassVariation(role), typeVariation)) {
          roles.add(role);
        }
      }
    }

    return roles;
  }

  @SuppressWarnings("unchecked")
  private <T> void setFields(Class<T> type, Class<? super T> classToStop, T instance, JsonNode node, String requestedVariation) {
    if (type != classToStop) {
      setFields((Class<T>) type.getSuperclass(), classToStop, instance, node, requestedVariation);
    }

    Map<String, String> fieldMap = fieldMapper.getFieldMap(type);

    for (String javaFieldName : fieldMap.keySet()) {

      String dbFieldName = fieldMap.get(javaFieldName);

      try {
        if (node.has(dbFieldName)) {
          Field field = type.getDeclaredField(javaFieldName);
          field.setAccessible(true);

          Object value = getVariationValue(type, field, requestedVariation, node, node.get(dbFieldName));

          field.set(instance, value);
        }
      } catch (SecurityException e) {
        LOG.error("Field {} of type {} could not be retrieved.", javaFieldName, type);
        LOG.debug("exception", e);
      } catch (NoSuchFieldException e) {
        LOG.error("Field {} of type {} could not be retrieved.", javaFieldName, type);
        LOG.debug("exception", e);
      } catch (IllegalArgumentException e) {
        LOG.error("Field {} of type {} received the wrong value.", javaFieldName, type);
        LOG.debug("exception", e);
      } catch (IllegalAccessException e) {
        LOG.error("Field {} of type {} could not be accessed.", javaFieldName, type);
        LOG.debug("exception", e);
      }
    }
  }

  private Object getVariationValue(Class<?> type, Field field, String variation, JsonNode record, JsonNode originalValue) {
    Object value = null;
    try {
      value = convertValue(field.getType(), originalValue);
    } catch (IOException e) {
      LOG.error("Original value \"{}\" cannot be converted.", originalValue);
    }

    // TODO this is a very ugly partial fix for [#1919]
    if (type != Entity.class && type != DomainEntity.class && TypeRegistry.isVariable(type)) {
      @SuppressWarnings("unchecked")
      Class<? extends Variable> subClass = typeRegistry.getVariationClass((Class<? extends Variable>) type, variation);

      if (subClass != null) {
        String overridenDBKey = fieldMapper.getFieldName(subClass, field);

        if (record.has(overridenDBKey)) {

          try {
            value = convertValue(field.getType(), record.get(overridenDBKey));
          } catch (IOException e) {
            LOG.error("Variation value \"{}\" cannot be converted.", record.get(overridenDBKey));
          }
        }
      }
    }

    return value;
  }

  @SuppressWarnings("unchecked")
  private JsonNode convertToTree(DBObject object) throws IOException {
    JsonNode tree;
    if (object instanceof JacksonDBObject) {
      tree = ((JacksonDBObject<JsonNode>) object).getObject();
    } else if (object instanceof DBJsonNode) {
      tree = ((DBJsonNode) object).getDelegate();
    } else {
      throw new IOException("Huh? DB didn't generate the right type of object out of the data stream...");
    }
    return tree;
  }

  public <T extends Entity> List<T> getAllForDBObject(DBObject object, Class<T> type) throws IOException {
    List<T> entities = Lists.newArrayList();

    JsonNode tree = convertToTree(object);

    JsonNode node = tree.findValue(DomainEntity.VARIATIONS);
    if (node != null) {
      Iterator<JsonNode> iterator = node.elements();
      while (iterator.hasNext()) {
        String variation = iterator.next().textValue();
        Class<? extends Entity> varType = typeRegistry.getTypeForIName(variation);
        checkNotNull(varType, variation);
        T entity = type.cast(reduce(varType, tree));
        entities.add(entity);
      }
    }

    return entities;
  }

}
