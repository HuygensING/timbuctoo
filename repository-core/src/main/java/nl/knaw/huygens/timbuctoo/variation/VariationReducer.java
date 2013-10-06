package nl.knaw.huygens.timbuctoo.variation;

import java.io.IOException;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import nl.knaw.huygens.timbuctoo.config.DocTypeRegistry;
import nl.knaw.huygens.timbuctoo.model.Entity;
import nl.knaw.huygens.timbuctoo.model.Reference;
import nl.knaw.huygens.timbuctoo.storage.mongo.MongoChanges;
import nl.knaw.huygens.timbuctoo.storage.mongo.variation.DBJsonNode;

import org.mongojack.internal.stream.JacksonDBObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.common.collect.Lists;
import com.mongodb.DBObject;

public class VariationReducer {

  private static final String VERSIONS_FIELD = "versions";

  private final TypeConverter converter;
  private final ObjectMapper mapper;
  private static final Logger LOG = LoggerFactory.getLogger(VariationReducer.class);

  public VariationReducer(DocTypeRegistry registry, ObjectMapper mapper) {
    converter = new TypeConverter(registry);
    this.mapper = mapper;
  }

  public VariationReducer(DocTypeRegistry registry) {
    this(registry, new ObjectMapper());
  }

  public <T extends Entity> MongoChanges<T> reduceMultipleRevisions(Class<T> type, DBObject obj) throws IOException {
    if (obj == null) {
      return null;
    }
    JsonNode tree = convertToTree(obj);
    ArrayNode versionsNode = (ArrayNode) tree.get(VERSIONS_FIELD);
    MongoChanges<T> changes = null;

    T item = null;
    for (int i = 0; versionsNode.hasNonNull(i); i++) {
      item = reduce(versionsNode.get(i), type);

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

    return reduce(objectToReduce, type);
  }

  public <T extends Entity> T reduceDBObject(DBObject obj, Class<T> cls) throws IOException {
    return reduceDBObject(obj, cls, null);
  }

  public <T extends Entity> T reduceDBObject(DBObject obj, Class<T> cls, String variation) throws IOException {
    if (obj == null) {
      return null;
    }
    JsonNode tree = convertToTree(obj);
    return reduce(tree, cls, variation);
  }

  public <T extends Entity> List<T> reduceDBObject(List<DBObject> nodes, Class<T> cls) throws IOException {
    List<T> rv = Lists.newArrayListWithCapacity(nodes.size());
    for (DBObject n : nodes) {
      rv.add(reduceDBObject(n, cls));
    }
    return rv;
  }

  public <T extends Entity> List<T> reduce(List<JsonNode> nodes, Class<T> cls) throws VariationException, JsonProcessingException {
    List<T> rv = Lists.newArrayListWithCapacity(nodes.size());
    for (JsonNode n : nodes) {
      rv.add(reduce(n, cls));
    }
    return rv;
  }

  public <T extends Entity> T reduce(JsonNode node, Class<T> cls) throws VariationException, JsonProcessingException {
    return reduce(node, cls, null);
  }

  public <T extends Entity> T reduce(JsonNode node, Class<T> cls, String requestedVariation) throws VariationException, JsonProcessingException {
    final String classVariation = VariationUtils.getVariationName(cls);
    String idPrefix = classVariation + "-";
    List<JsonNode> specificData = Lists.newArrayListWithExpectedSize(1);
    List<String> types = getTypes(node);
    String requestedClassId = VariationUtils.getClassId(cls);

    String variationToRetrieve = null;
    JsonNode defaultVariationNode = null;

    if (node.get(requestedClassId) != null) {
      defaultVariationNode = node.get(requestedClassId).get(VariationUtils.DEFAULT_VARIATION);
    }

    variationToRetrieve = getVariationToRetrieve(classVariation, defaultVariationNode, requestedVariation, types);

    ObjectNode rv = mapper.createObjectNode();
    for (Class<? extends Entity> someCls : VariationUtils.getAllClasses(cls)) {
      String id = VariationUtils.getClassId(someCls);
      JsonNode data = node.get(id);
      if (data != null) {
        if (id.startsWith(idPrefix)) {
          specificData.add(data);
        } else {
          processCommonData(variationToRetrieve, data, rv);
        }
      }
    }
    for (JsonNode d : specificData) {
      if (d.isObject()) {
        rv.setAll((ObjectNode) d);
      } else {
        throw new VariationException("Non-object variation data; this should never happen.");
      }
    }
    Iterator<Entry<String, JsonNode>> nodeFields = node.fields();
    while (nodeFields.hasNext()) {
      Entry<String, JsonNode> entry = nodeFields.next();
      String key = entry.getKey();
      if (key.startsWith("^") || key.startsWith("_")) {
        rv.put(key, entry.getValue());
      }
    }
    //The @JsonTypeInfo on entity expects the property @class everytime, the class is deserialized. 
    rv.put("@class", cls.getName());

    T returnObject = mapper.treeToValue(rv, cls);
    returnObject.setVariations(getVariations(types, returnObject.getId()));
    if (defaultVariationNode != null) {
      returnObject.setCurrentVariation(variationToRetrieve);
    }

    return returnObject;
  }

  /**
   * Create the references to all the possible variations.
   * @param typeStrings
   * @param id
   * @return
   * @throws VariationException 
   */
  private List<Reference> getVariations(List<String> typeStrings, String id) throws VariationException {
    List<Reference> references = Lists.<Reference> newLinkedList();
    for (String typeString : typeStrings) {
      Class<? extends Entity> type = converter.getClass(typeString);
      if (typeString.contains("-")) {
        // project specific classes don't have any variation
        references.add(new Reference(type, id));
      } else if (type != null) {
        references.add(new Reference(type, id));
      } else {
        LOG.error("Unknown variation {}", typeString);
        throw new VariationException("Unknown variation " + typeString);
      }
    }
    return references;
  }

  private String getVariationToRetrieve(final String packageName, JsonNode defaultVariationNode, String requestedVariation, List<String> variations) throws VariationException {
    String variationToGet = packageName;
    if (VariationUtils.BASE_MODEL_PACKAGE_VARIATION.equals(packageName)) {
      //if the package is equal to the base package, different variations may be available.
      if (requestedVariation != null && isRequestedVariationAvailable(requestedVariation, variations)) {
        variationToGet = requestedVariation;
      } else {
        // if the requested variation is not available return the default variation 
        variationToGet = defaultVariationNode != null ? defaultVariationNode.asText() : packageName;
      }
    } else if (requestedVariation != null && !packageName.contains(requestedVariation)) {
      // The requested type is project specific and only available in the projects variation.
      throw new VariationException("Variation does not exist for requested type.");
    }
    return variationToGet;
  }

  private boolean isRequestedVariationAvailable(String requestedVariation, List<String> projectVariations) {
    for (String variation : projectVariations) {
      if (variation.contains(requestedVariation)) {
        return true;
      }
    }
    return false;
  }

  private List<String> getTypes(JsonNode node) {
    List<String> variations = Lists.newArrayList();
    Iterator<Map.Entry<String, JsonNode>> fieldIterator = node.fields();

    Map.Entry<String, JsonNode> fieldEntry = null;

    while (fieldIterator.hasNext()) {
      fieldEntry = fieldIterator.next();
      if (fieldEntry.getValue() instanceof ObjectNode) {
        variations.add(fieldEntry.getKey());
      }
    }

    return variations;
  }

  private void processCommonData(final String variationName, JsonNode commonData, ObjectNode rv) throws VariationException {
    Iterator<Entry<String, JsonNode>> fields = commonData.fields();
    // Go through all common fields:
    while (fields.hasNext()) {
      Entry<String, JsonNode> f = fields.next();
      String k = f.getKey();
      JsonNode fV = f.getValue();
      // Loop through values:
      if (fV.isArray()) {
        ArrayNode ary = (ArrayNode) fV;
        fetchAndAssignMatchingValue(variationName, rv, k, ary);
      } else if (k.startsWith("!")) {
        // Ignore properties starting with a "!"
      } else {
        throw new VariationException("Unknown variation value for key: \"" + k + "\"");
      }
    }
  }

  private void fetchAndAssignMatchingValue(final String variationName, ObjectNode rv, String k, ArrayNode ary) throws VariationException {
    int i = 0;
    for (JsonNode elem : ary) {
      if (elem.isObject()) {
        // Check the list of agreeing VREs to see if we want this one:
        JsonNode agreedValueNode = elem.get(VariationUtils.AGREED);
        if (agreedValueNode != null && agreedValueNode.isArray()) {
          ArrayNode agreedValues = (ArrayNode) agreedValueNode;
          if (arrayContains(agreedValues, variationName)) {
            rv.put(k, elem.get(VariationUtils.VALUE));
            return;
          }
        } else {
          throw new VariationException("Unknown variation 'agreed' object for key " + k + " and index " + i);
        }
      } else {
        throw new VariationException("Unknown variation array element for key " + k + " and index " + i);
      }
      i++;
    }
    // The loop will return as we found a value that agrees;
    // if we get here that means no such value exists, so
    // we will put null:
    rv.putNull(k);
  }

  private boolean arrayContains(ArrayNode stringAry, String stringEl) {
    // I assume there is a better way to do this but I have not found it:
    int i = stringAry.size();
    while (i-- > 0) {
      if (stringAry.get(i).asText().equals(stringEl)) {
        return true;
      }
    }
    return false;
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
   * Example1: if cls is Person.class, it will retrieve Person, Scientist, CivilServant and their project related subtypes.
   * Example2:  if cls is Scientist.class, it will retrieve Person, Scientist, CivilServant and their project related subtypes.
   * Example3:  if cls is ProjectAScientist.class, it will retrieve Person, Scientist, CivilServant and their project related subtypes.
   */
  public <T extends Entity> List<T> getAllForDBObject(DBObject item, Class<T> cls) throws IOException {
    JsonNode jsonNode = convertToTree(item);
    Iterator<String> fieldNames = jsonNode.fieldNames();
    List<T> rv = Lists.newArrayList();
    while (fieldNames.hasNext()) {
      String name = fieldNames.next();
      if (!name.startsWith("^") && !name.startsWith("_")) {
        JsonNode subNode = jsonNode.get(name);
        if (subNode != null && subNode.isObject()) {
          Class<? extends T> indicatedClass = converter.getClass(name);
          rv.add(reduce(jsonNode, indicatedClass));
        }
      }
    }
    return rv;
  }

}
