package nl.knaw.huygens.timbuctoo.rml;

import java.util.Iterator;

public interface DataSource {

  Iterator<Row> getRows();

  void willBeJoinedOn(String fieldName, Object referenceJoinValue, String uri, String outputFieldName);

}
