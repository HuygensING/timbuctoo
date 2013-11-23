package nl.knaw.huygens.timbuctoo.storage.mongo;

import java.io.IOException;
import java.util.List;

import nl.knaw.huygens.timbuctoo.config.TypeRegistry;
import nl.knaw.huygens.timbuctoo.model.Entity;
import nl.knaw.huygens.timbuctoo.model.util.Datable;
import nl.knaw.huygens.timbuctoo.model.util.PersonName;
import nl.knaw.huygens.timbuctoo.storage.FieldMapper;

import org.mongojack.internal.stream.JacksonDBObject;
import org.slf4j.Logger;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mongodb.DBObject;

/**
 * Base class for {@code VariationInducer} and {@code VariationInducer}.
 */
abstract class VariationConverter {

  protected static final String BASE_MODEL_PACKAGE = "model";

  protected final TypeRegistry typeRegistry;
  protected final ObjectMapper jsonMapper;
  protected final FieldMapper fieldMapper;
  protected final MongoObjectMapper propertyMapper;

  public VariationConverter(TypeRegistry registry) {
    typeRegistry = registry;
    jsonMapper = new ObjectMapper();
    fieldMapper = new FieldMapper();
    propertyMapper = new MongoObjectMapper();
  }

  protected abstract Logger getLogger();

  @SuppressWarnings("unchecked")
  protected JsonNode convertDBObjectToJsonNode(DBObject object) throws IOException {
    if (object instanceof JacksonDBObject) {
      return (((JacksonDBObject<JsonNode>) object).getObject());
    } else if (object instanceof DBJsonNode) {
      return ((DBJsonNode) object).getDelegate();
    } else {
      getLogger().error("Failed to convert {}", object.getClass());
      throw new IOException("Unknown DBObject type");
    }
  }

  protected String getPackageName(Class<? extends Entity> type) {
    String name = type.getPackage().getName();
    return name.substring(name.lastIndexOf('.') + 1);
  }

  protected String typeToVariationName(Class<? extends Entity> type) {
    String typeId = typeRegistry.getINameForType(type);
    String variationId = getPackageName(type);
    return variationId.equals(BASE_MODEL_PACKAGE) ? typeId : variationId + "-" + typeId;
  }

  protected <T> Object convertValue(Class<T> fieldType, JsonNode value) throws IOException {
    if (value.isArray()) {
      return createCollection(value);
    } else if (fieldType == Integer.class || fieldType == int.class) {
      return value.asInt();
    } else if (fieldType == Boolean.class || fieldType == boolean.class) {
      return value.asBoolean();
    } else if (fieldType == Character.class || fieldType == char.class) {
      return value.asText().charAt(0);
    } else if (fieldType == Double.class || fieldType == double.class) {
      return value.asDouble();
    } else if (fieldType == Float.class || fieldType == float.class) {
      return (float) value.asDouble();
    } else if (fieldType == Long.class || fieldType == long.class) {
      return value.asLong();
    } else if (fieldType == Short.class || fieldType == short.class) {
      return (short) value.asInt();
    } else if (Class.class.isAssignableFrom(fieldType)) {
      try {
        return Class.forName(value.asText());
      } catch (ClassNotFoundException e) {
        throw new IOException(e);
      }
    } else if (Datable.class.isAssignableFrom(fieldType)) {
      return new Datable(value.asText());
    } else if (PersonName.class.isAssignableFrom(fieldType)) {
      return jsonMapper.readValue(value.toString(), PersonName.class);
    }

    return value.asText();
  }

  private Object createCollection(JsonNode value) throws IOException, JsonParseException, JsonMappingException {
    return jsonMapper.readValue(value.toString(), new TypeReference<List<? extends Object>>() {});
  }

}
