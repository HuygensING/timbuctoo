package nl.knaw.huygens.timbuctoo.v5.openrefine;

import java.io.IOException;
import java.util.Map;

public interface ReconciliationQueryExecutor {
  Map<String, QueryResults> execute(Map<String, Query> query) throws IOException;
}
