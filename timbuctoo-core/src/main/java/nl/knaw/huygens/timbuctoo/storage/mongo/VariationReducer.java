package nl.knaw.huygens.timbuctoo.storage.mongo;

import java.io.IOException;
import java.lang.reflect.Field;
import java.util.List;
import java.util.Map;

import nl.knaw.huygens.timbuctoo.config.TypeRegistry;
import nl.knaw.huygens.timbuctoo.model.Entity;

import org.mongojack.internal.stream.JacksonDBObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.mongodb.DBObject;

class VariationReducer extends VariationConverter {

  private static final Logger LOG = LoggerFactory.getLogger(VariationReducer.class);
  private static final String VERSIONS_FIELD = "versions";

  public VariationReducer(TypeRegistry registry, MongoObjectMapper mongoMapper) {
    super(registry, mongoMapper);
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

  public <T extends Entity> T reduce(Class<T> type, JsonNode node, String requestedVariation) throws VariationException, JsonProcessingException {
    T returnObject = null;
    try {
      returnObject = type.newInstance();

      setFields(type, returnObject, node);

    } catch (InstantiationException e) {
      LOG.error("Could not initialize object of type {} ", type);
      LOG.debug("exception", e);
    } catch (IllegalAccessException e) {
      LOG.error("Could not initialize object of type {} ", type);
      LOG.debug("exception", e);
    }

    return returnObject;
  }

  @SuppressWarnings("unchecked")
  public <T extends Entity> void setFields(Class<T> type, T instance, JsonNode node) {
    if (type != Entity.class) {
      setFields((Class<T>) type.getSuperclass(), instance, node);
    }

    Map<String, String> fieldMap = mongoMapper.getFieldMap(type);

    for (String javaFieldName : fieldMap.keySet()) {

      String dbFieldName = fieldMap.get(javaFieldName);

      try {
        if (node.has(dbFieldName)) {
          Field field = type.getDeclaredField(javaFieldName);
          field.setAccessible(true);
          field.set(instance, getValue(field.getType(), node.get(dbFieldName)));
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

  private Object getValue(Class<?> type, JsonNode value) {
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
