package nl.knaw.huygens.timbuctoo.server.endpoints.v2.bulkupload;

import nl.knaw.huygens.timbuctoo.rml.ErrorHandler;
import nl.knaw.huygens.timbuctoo.rml.Row;
import nl.knaw.huygens.timbuctoo.rml.datasource.JoinHandler;

import java.util.Map;

public interface RowFactory {
  JoinHandler getJoinHandler();

  Row makeRow(Map<String, String> values, ErrorHandler errorHandler);
}
