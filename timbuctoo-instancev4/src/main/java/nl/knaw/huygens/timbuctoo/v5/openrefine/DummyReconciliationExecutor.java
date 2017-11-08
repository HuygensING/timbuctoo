package nl.knaw.huygens.timbuctoo.v5.openrefine;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Map;
import java.util.TreeMap;

public class DummyReconciliationExecutor implements ReconciliationQueryExecutor {
  @Override
  public Map<String, QueryResults> execute(Map<String, Query> query) throws IOException {
    Map<String, QueryResults> queryResult = new TreeMap<>();
    for (Map.Entry<String, Query> stringQueryEntry : query.entrySet()) {
      QueryResult qr = new QueryResult();
      qr.id = stringQueryEntry.getKey().substring(1);
      qr.name = stringQueryEntry.getValue().query;
      qr.match = true;
      qr.score = 1.0;
      qr.type = new String[]{"String"};
      ArrayList<QueryResult> qrAl = new ArrayList<>();
      qrAl.add(qr);
      QueryResults queryResults = new QueryResults();
      queryResults.result = qrAl;
      queryResult.put(stringQueryEntry.getKey(), queryResults);
    }
    return queryResult;
  }
}
