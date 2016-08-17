package nl.knaw.huygens.timbuctoo.rml;

import nl.knaw.huygens.timbuctoo.rml.Row;

import java.util.Iterator;

public interface DataSource {

  Iterator<Row> getRows(ErrorHandler defaultErrorHandler);

  void willBeJoinedOn(String fieldName, Object referenceJoinValue, String uri, String outputFieldName);

}
