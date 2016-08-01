package nl.knaw.huygens.timbuctoo.rml;

import java.util.Iterator;
import java.util.Map;

public interface DataSource {

  Iterator<Map<String, Object>> getItems();

  void willBeJoinedOn(String fieldName, Object referenceJoinValue, String uri, String outputFieldName);

}
