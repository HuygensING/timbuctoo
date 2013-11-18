package nl.knaw.huygens.timbuctoo.storage.mongo;

import java.io.IOException;
import java.util.List;

import nl.knaw.huygens.timbuctoo.config.TypeRegistry;
import nl.knaw.huygens.timbuctoo.model.Entity;
import nl.knaw.huygens.timbuctoo.model.util.Datable;
import nl.knaw.huygens.timbuctoo.model.util.PersonName;
import nl.knaw.huygens.timbuctoo.storage.FieldMapper;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;

/**
 * Base class for {@code VariationInducer} and {@code VariationInducer}.
 */
class VariationConverter {

  protected static final String AGREED = "a";
  protected static final String VALUE = "v";
  protected static final String BASE_MODEL_PACKAGE = "model";
  protected static final String DEFAULT_VARIATION = "!defaultVRE";

  protected final TypeRegistry typeRegistry;
  protected final ObjectMapper mapper;
  protected final MongoObjectMapper mongoObjectMapper;
  protected final FieldMapper fieldMapper;

  public VariationConverter(TypeRegistry registry) {
    typeRegistry = registry;
    mapper = new ObjectMapper();
    mongoObjectMapper = new MongoObjectMapper();
    fieldMapper = new FieldMapper();
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

  @SuppressWarnings("unchecked")
  protected <T extends Entity> Class<? extends T> variationNameToType(String id) {
    return (Class<? extends T>) typeRegistry.getTypeForIName(normalize(id));
  }

  private String normalize(String typeString) {
    return typeString.replaceFirst("[a-z]*-", "");
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
      return mapper.readValue(value.toString(), PersonName.class);
    }

    return value.asText();
  }

  protected Object createCollection(JsonNode value) throws IOException, JsonParseException, JsonMappingException {
    ArrayNode array = (ArrayNode) value;
    array.size();

    List<? extends Object> returnValue = mapper.readValue(value.toString(), new TypeReference<List<? extends Object>>() {});

    return returnValue;
  }
}
