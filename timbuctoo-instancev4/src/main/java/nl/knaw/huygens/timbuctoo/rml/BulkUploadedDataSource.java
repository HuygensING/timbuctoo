package nl.knaw.huygens.timbuctoo.rml;

import java.util.Iterator;
import java.util.Map;

public class BulkUploadedDataSource implements DataSource {
  @Override
  public Iterator<Map<String, Object>> getItems() {
    throw new UnsupportedOperationException("");//FIXME: implement
  }

  @Override
  public void willBeJoinedOn(String fieldName, Object referenceJoinValue, String uri, String outputFieldName) {
    throw new UnsupportedOperationException("");//FIXME: implement
  }
}
