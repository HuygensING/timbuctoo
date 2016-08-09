package nl.knaw.huygens.timbuctoo.rml;

import java.util.Map;

public class NoOpErrorHandler implements ErrorHandler {
  @Override
  public void handleLink(Map<String, Object> rowData, String child) {

  }
}
