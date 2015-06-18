package nl.knaw.huygens.timbuctoo.tools.conversion;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import nl.knaw.huygens.timbuctoo.model.DomainEntity;
import nl.knaw.huygens.timbuctoo.model.Entity;
import nl.knaw.huygens.timbuctoo.storage.EntityReducer;
import nl.knaw.huygens.timbuctoo.storage.StorageException;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import edu.emory.mathcs.backport.java.util.Collections;

public class AllVersionVariationMap<T extends DomainEntity> {

  private Map<Integer, JsonNode> revisions;
  private EntityReducer reducer;
  private Class<T> type;

  private AllVersionVariationMap(Class<T> type, EntityReducer reducer) {
    this.type = type;
    this.reducer = reducer;
    this.revisions = Maps.newHashMap();
  }

  private void mapRevisions(JsonNode tree) {
    ArrayNode versionsNode = (ArrayNode) tree.get("versions");
    for (int i = 0; versionsNode.hasNonNull(i); i++) {
      JsonNode version = versionsNode.get(i);
      addRevision(version);
    }
  }

  private void addRevision(JsonNode version) {
    revisions.put(getRevision(version), version);
  }

  public List<Integer> revisionsInOrder() {
    ArrayList<Integer> returnValue = Lists.newArrayList(revisions.keySet());
    Collections.sort(returnValue);
    return returnValue;
  }

  private int getRevision(JsonNode version) {
    return version.get(Entity.REVISION_PROPERTY_NAME).asInt();
  }

  public List<T> get(Integer revisionToGet) throws StorageException {
    JsonNode revision = revisions.remove(revisionToGet);
    List<T> allVariations = reducer.reduceAllVariations(type, revision);
    return allVariations;
  }

  public static <T extends DomainEntity> AllVersionVariationMap<T> forVersionNode(Class<T> type, JsonNode tree, EntityReducer reducer) {
    AllVersionVariationMap<T> map = new AllVersionVariationMap<T>(type, reducer);

    map.mapRevisions(tree);

    return map;
  }

  public static <T extends DomainEntity> AllVersionVariationMap<T> forNormalNode(Class<T> type, JsonNode tree, EntityReducer reducer) {
    AllVersionVariationMap<T> map = new AllVersionVariationMap<T>(type, reducer);

    map.addRevision(tree);

    return map;
  }
}
