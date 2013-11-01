package nl.knaw.huygens.timbuctoo.storage.mongo;

import java.util.Iterator;
import java.util.Map;

import nl.knaw.huygens.timbuctoo.config.TypeRegistry;
import nl.knaw.huygens.timbuctoo.model.DomainEntity;
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
  private static final Logger LOG = LoggerFactory.getLogger(VariationInducer.class);

  private final ObjectWriter writer;
  private final MongoObjectMapper mongoMapper;

  public VariationInducer(TypeRegistry registry, Class<?> view, MongoObjectMapper mongoMapper) {
    super(registry);
    writer = mapper.writerWithView(view);
    this.mongoMapper = mongoMapper;
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

    Map<String, Object> map = mongoMapper.mapObject(type, item, true);
    ObjectNode newNode = mapper.valueToTree(map);

    if (existingItem != null && DomainEntity.class.isAssignableFrom(type)) {
      newNode = merge(type, existingItem, newNode);
    }

    return cleanUp(newNode);
  }

  private ObjectNode merge(Class<? extends Entity> type, ObjectNode existingNode, JsonNode newNode) {
    Iterator<String> fieldNames = newNode.fieldNames();
    while (fieldNames.hasNext()) {
      String fieldName = fieldNames.next();
      if (!existingNode.has(fieldName)) {
        existingNode.put(fieldName, newNode.get(fieldName));
      } else {
        String typeName = typeRegistry.getINameForType(type);
        if (fieldName.contains(".") && !fieldName.startsWith(typeName)) {
          existingNode.put(fieldName.replace(fieldName.substring(0, fieldName.indexOf('.')), typeName), newNode.get(fieldName));
        }
      }
    }
    return existingNode;
  }

  // remove all the runtime fields from the node
  private ObjectNode cleanUp(ObjectNode node) {
    Iterator<String> fieldNames = node.fieldNames();
    // deepcopy is neede, because during iteration over the fields the fields cannot be removed.
    ObjectNode nodeToCleanUp = node.deepCopy();
    while (fieldNames.hasNext()) {
      String fieldName = fieldNames.next();
      if (fieldName.startsWith("!")) {
        nodeToCleanUp.remove(fieldName);
      }
    }
    return nodeToCleanUp;
  }
}
