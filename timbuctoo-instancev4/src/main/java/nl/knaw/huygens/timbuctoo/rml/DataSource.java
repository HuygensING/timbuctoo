package nl.knaw.huygens.timbuctoo.rml;

import java.util.Iterator;

public interface DataSource {
  Iterator<Row> getRows(ErrorHandler defaultErrorHandler);
}
