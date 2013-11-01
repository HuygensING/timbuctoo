package nl.knaw.huygens.timbuctoo.storage.mongo;

import java.util.Map;

import nl.knaw.huygens.timbuctoo.config.TypeRegistry;
import nl.knaw.huygens.timbuctoo.model.Entity;

import org.mongojack.internal.stream.JacksonDBObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.common.base.Preconditions;
import com.mongodb.DBObject;

class VariationInducer extends VariationConverter {

  private final ObjectWriter writer;
  private static final Logger LOG = LoggerFactory.getLogger(VariationInducer.class);

  public VariationInducer(TypeRegistry registry, Class<?> view) {
    super(registry);
    writer = mapper.writerWithView(view);
  }

  /**
   * Convenience method for {@code induce(type, item, null)}.
   * @param type
   * @param item
   * @return
   * @throws VariationException
   */
  public <T extends Entity> JsonNode induce(Class<T> type, T item) throws VariationException {
    return induce(type, item, (ObjectNode) null);
  }

  @SuppressWarnings("unchecked")
  public <T extends Entity> JsonNode induce(Class<T> type, T item, DBObject existingItem) throws VariationException {
    ObjectNode node = null;
    if (existingItem instanceof JacksonDBObject) {
      node = (ObjectNode) (((JacksonDBObject<JsonNode>) existingItem).getObject());
    } else if (existingItem instanceof DBJsonNode) {
      node = (ObjectNode) ((DBJsonNode) existingItem).getDelegate();
    } else if (existingItem != null) {
      throw new VariationException("Unknown type of DBObject!");
    }
    return induce(type, item, node);
  }

  /**
   * Converts an Entity to a JsonTree and combines it with the {@code existionItem}. 
   * If the {@code existionItem} is null it creates a new item. 
   * @param type the type of the item to convert.
   * @param item the new item to convert.
   * @param existingItem the existing item.
   * @return the converted and combined item.
   * @throws VariationException
   */
  public <T extends Entity> JsonNode induce(Class<T> type, T item, ObjectNode existingItem) throws VariationException {
    Preconditions.checkArgument(item != null);
    Preconditions.checkArgument(type != null);

    Map<String, Object> map = createObjectMap(type, item);
    JsonNode node = mapper.valueToTree(map);

    return node;
  }

  @SuppressWarnings("unchecked")
  private <T extends Entity> Map<String, Object> createObjectMap(Class<T> type, T item) {
    return new MongoObjectMapper(typeRegistry).mapObject(type, item, true);
  }
}
