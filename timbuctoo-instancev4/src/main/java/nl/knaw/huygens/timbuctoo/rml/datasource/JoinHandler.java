package nl.knaw.huygens.timbuctoo.rml.datasource;

import java.util.Map;

public interface JoinHandler {

  void resolveReferences(Map<String, Object> valueMap);

  void willBeJoinedOn(String fieldName, Object referenceJoinValue, String uri, String outputFieldName);

}
