package nl.knaw.huygens.timbuctoo.storage.mongo;

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

import org.mongojack.internal.stream.JacksonDBObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.google.common.base.Objects;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.mongodb.DBObject;

class VariationReducer extends VariationConverter {

  private static final Logger LOG = LoggerFactory.getLogger(VariationReducer.class);
  private static final String VERSIONS_FIELD = "versions";
  private final MongoFieldMapper mongoFieldMapper;

  public VariationReducer(TypeRegistry registry, MongoObjectMapper mongoObjectMapper, MongoFieldMapper mongoFieldMapper) {
    super(registry, mongoObjectMapper);
    this.mongoFieldMapper = mongoFieldMapper;
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

  public <T extends Entity> T reduce(Class<T> type, JsonNode node) throws VariationException, JsonProcessingException {
    return reduce(type, node, null);
  }

  @SuppressWarnings("unchecked")
  public <T extends Entity> T reduce(Class<T> type, JsonNode node, String requestedVariation) throws VariationException, JsonProcessingException {
    T returnObject = null;
    try {
      returnObject = type.newInstance();

      if (requestedVariation == null && DomainEntity.class.isAssignableFrom(type)) {
        requestedVariation = typeRegistry.getClassVariation((Class<? extends DomainEntity>) type);
      }

      setFields(type, Entity.class, returnObject, node, requestedVariation);

      /* TODO get all roles of variation and check if they are stored in the database.
       * Then fill generate the roles from the data in the database.
       */

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
      String typeName = mongoFieldMapper.getTypeNameOfFieldName(fieldName);

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

    Map<String, String> fieldMap = mongoFieldMapper.getFieldMap(type);

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
    Object value = convertValue(field.getType(), originalValue);

    if (TypeRegistry.isVariable(type)) {
      @SuppressWarnings("unchecked")
      Class<? extends Variable> subClass = typeRegistry.getVariationClass((Class<? extends Variable>) type, variation);

      if (subClass != null) {
        String overridenDBKey = mongoFieldMapper.getFieldName(subClass, field);
        if (record.has(overridenDBKey)) {
          value = convertValue(field.getType(), record.get(overridenDBKey));
        }
      }
    }

    return value;
  }

  private Object convertValue(Class<?> type, JsonNode value) {
    if (type == Integer.class || type == int.class) {
      return value.asInt();
    } else if (type == Boolean.class || type == boolean.class) {
      return value.asBoolean();
    } else if (type == Character.class || type == char.class) {
      return value.asText().charAt(0);
    } else if (type == Double.class || type == double.class) {
      return value.asDouble();
    } else if (type == Float.class || type == float.class) {
      return (float) value.asDouble();
    } else if (type == Long.class || type == long.class) {
      return value.asLong();
    } else if (type == Short.class || type == short.class) {
      return (short) value.asInt();
    }

    return value.asText();
  }

  @SuppressWarnings("unchecked")
  private JsonNode convertToTree(DBObject obj) throws IOException {
    JsonNode tree;
    if (obj instanceof JacksonDBObject) {
      tree = ((JacksonDBObject<JsonNode>) obj).getObject();
    } else if (obj instanceof DBJsonNode) {
      tree = ((DBJsonNode) obj).getDelegate();
    } else {
      throw new IOException("Huh? DB didn't generate the right type of object out of the data stream...");
    }
    return tree;
  }

  /*
   * This method generates a list of all the types of a type hierarchy, that are found in the DBObject.
   * Example1: if type is Person.class, it will retrieve Person, Scientist, CivilServant and their project related subtypes.
   * Example2:  if type is Scientist.class, it will retrieve Person, Scientist, CivilServant and their project related subtypes.
   * Example3:  if type is ProjectAScientist.class, it will retrieve Person, Scientist, CivilServant and their project related subtypes.
   */
  public <T extends Entity> List<T> getAllForDBObject(DBObject item, Class<T> type) throws IOException {
    JsonNode node = convertToTree(item);
    List<T> rv = Lists.newArrayList();
    for (String name : ImmutableList.copyOf(node.fieldNames())) {
      if (!name.startsWith("^") && !name.startsWith("_")) {
        JsonNode subNode = node.get(name);
        if (subNode != null && subNode.isObject()) {
          Class<? extends T> indicatedClass = variationNameToType(name);
          rv.add(reduce(indicatedClass, node));
        }
      }
    }
    return rv;
  }

}
