package nl.knaw.huygens.timbuctoo.rml;

import java.util.Map;

public interface ErrorHandler {
  void handleLink(Map<String, Object> rowData, String child);
}
