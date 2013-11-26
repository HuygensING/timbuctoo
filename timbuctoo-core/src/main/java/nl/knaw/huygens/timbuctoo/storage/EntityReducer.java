package nl.knaw.huygens.timbuctoo.storage;

import static com.google.common.base.Preconditions.checkNotNull;

import java.io.IOException;
import java.util.List;

import nl.knaw.huygens.timbuctoo.config.TypeRegistry;
import nl.knaw.huygens.timbuctoo.model.Entity;
import nl.knaw.huygens.timbuctoo.storage.mongo.MongoChanges;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.Lists;

public class EntityReducer {

  private static final Logger LOG = LoggerFactory.getLogger(EntityReducer.class);

  protected static final String BASE_MODEL_PACKAGE = "model";

  protected final TypeRegistry typeRegistry;
  protected final ObjectMapper jsonMapper;
  protected final FieldMapper fieldMapper;

  public EntityReducer(TypeRegistry registry) {
    typeRegistry = registry;
    jsonMapper = new ObjectMapper();
    fieldMapper = new FieldMapper();
  }

  public <T extends Entity> T reduceVariation(Class<T> type, JsonNode node) throws StorageException, JsonProcessingException {
    return reduceVariation(type, node, null);
  }

  public <T extends Entity> T reduceVariation(Class<T> type, JsonNode node, String variation) throws StorageException, JsonProcessingException {
    LOG.info("Enter reduceVariation");
    checkNotNull(node);

    try {
      return type.newInstance();
    } catch (InstantiationException e) {
      throw new StorageException(e);
    } catch (IllegalAccessException e) {
      throw new StorageException(e);
    }
  }

  public <T extends Entity> List<T> reduceAllVariations(Class<T> type, JsonNode tree) throws IOException {
    LOG.info("Enter reduceAllVariations");
    checkNotNull(tree);

    return Lists.newArrayList();
  }

  public <T extends Entity> T reduceRevision(Class<T> type, JsonNode tree) throws IOException {
    LOG.info("Enter reduceRevision");
    checkNotNull(tree);

    try {
      return type.newInstance();
    } catch (InstantiationException e) {
      throw new StorageException(e);
    } catch (IllegalAccessException e) {
      throw new StorageException(e);
    }
  }

  public <T extends Entity> MongoChanges<T> reduceAllRevisions(Class<T> type, JsonNode tree) throws IOException {
    LOG.info("Enter reduceAllRevisions");
    checkNotNull(tree);

    try {
      return new MongoChanges<T>("id", type.newInstance());
    } catch (InstantiationException e) {
      throw new StorageException(e);
    } catch (IllegalAccessException e) {
      throw new StorageException(e);
    }
  }

}
