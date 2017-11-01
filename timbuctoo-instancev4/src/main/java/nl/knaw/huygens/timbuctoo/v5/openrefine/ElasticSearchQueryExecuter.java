package nl.knaw.huygens.timbuctoo.v5.openrefine;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Map;
import java.util.TreeMap;

/**
 * Created by meindertk on 31-10-2017.
 */
public class ElasticSearchQueryExecuter implements ReconciliationQueryExecuter {
  @Override
  public Map<String, QueryResults> excute(Map<String, Query> query) throws IOException {
    Map<String, QueryResults> queryResult = new TreeMap<>();
    for (Map.Entry<String, Query> stringQueryEntry : query.entrySet()) {
      String myQuery = stringQueryEntry.getValue().query;
      QueryResult qr = new QueryResult();
      qr.id = stringQueryEntry.getKey().substring(1);
      qr.name = stringQueryEntry.getValue().query;
      qr.match = true;
      qr.score = 1.0;
      qr.type = new String[]{"String"};
      ArrayList<QueryResult> qrAl = new ArrayList<>();
      qrAl.add(qr);
      QueryResults queryResults = new QueryResults();
      queryResults.queryResults = qrAl;
      queryResult.put(stringQueryEntry.getKey(), queryResults);
    }
    return queryResult;
  }
}
