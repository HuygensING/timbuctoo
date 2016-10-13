package nl.knaw.huygens.timbuctoo.rml.datasource.joinhandlers;

import nl.knaw.huygens.timbuctoo.rml.datasource.JoinHandler;
import nl.knaw.huygens.timbuctoo.util.Tuple;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class HashMapBasedJoinHandler implements JoinHandler {
  private Map<String, Tuple<String, Map<Object, List<String>>>> cachedUris = new HashMap<>();


  @Override
  public void resolveReferences(Map<String, Object> valueMap) {
    for (Map.Entry<String, Tuple<String, Map<Object, List<String>>>> stringMapEntry : cachedUris
      .entrySet()) {
      final Tuple<String, Map<Object, List<String>>> stringMapTuple = cachedUris.get(stringMapEntry.getKey());

      List<String> uri = cachedUris.get(
        stringMapEntry.getKey()).getRight().get(valueMap.get(stringMapTuple.getLeft())
      );

      valueMap.put(stringMapEntry.getKey(), uri);
    }
  }

  @Override
  public void announceJoinOn(String fieldName, Object referenceJoinValue, String uri, String outputFieldName) {
    cachedUris.computeIfAbsent(outputFieldName, x -> Tuple.tuple(fieldName, new HashMap<>()))
      .getRight()
      .computeIfAbsent(referenceJoinValue, x -> new ArrayList<>())
      .add(uri);
  }

}

