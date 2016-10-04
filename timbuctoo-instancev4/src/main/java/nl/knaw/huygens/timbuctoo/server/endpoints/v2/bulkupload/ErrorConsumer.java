package nl.knaw.huygens.timbuctoo.server.endpoints.v2.bulkupload;

import nl.knaw.huygens.timbuctoo.rml.ErrorHandler;

import java.util.Map;
import java.util.function.Consumer;

public class ErrorConsumer implements ErrorHandler {
  private final Consumer<String> consumer;

  public ErrorConsumer(Consumer<String> consumer) {
    this.consumer = consumer;
  }


  @Override
  public void linkError(Map<String, Object> rowData, String childField, String parentCollection, String parentField) {
    consumer.accept("\nF");
  }
}
