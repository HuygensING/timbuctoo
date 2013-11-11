package nl.knaw.huygens.timbuctoo.storage.mongo;

import java.lang.reflect.Modifier;
import java.util.List;

import nl.knaw.huygens.timbuctoo.config.TypeRegistry;
import nl.knaw.huygens.timbuctoo.model.Entity;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.Lists;

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
  protected final MongoFieldMapper mongoFieldMapper;

  public VariationConverter(TypeRegistry registry, MongoObjectMapper mongoObjectMapper, MongoFieldMapper mongoFieldMapper) {
    typeRegistry = registry;
    mapper = new ObjectMapper();
    this.mongoObjectMapper = mongoObjectMapper;
    this.mongoFieldMapper = mongoFieldMapper;
  }

  /**
   * Returns variation names for the specified entity type and its superclasses.
   */
  @SuppressWarnings("unchecked")
  protected List<String> getVariationNamesForType(Class<? extends Entity> type) {
    List<String> names = Lists.newArrayList();
    // TODO Use TypeRegistry, this loop is fragile
    while (type != null && !Modifier.isAbstract(type.getModifiers())) {
      names.add(typeToVariationName(type));
      type = (Class<? extends Entity>) type.getSuperclass();
    }
    return names;
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

  protected Object convertValue(Class<?> type, JsonNode value) {
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

}
