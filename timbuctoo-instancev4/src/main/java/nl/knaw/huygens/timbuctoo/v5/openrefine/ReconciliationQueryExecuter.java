package nl.knaw.huygens.timbuctoo.v5.openrefine;

import java.io.IOException;
import java.util.Map;

/**
 * Created by meindertk on 26-10-2017.
 *
 */
public interface ReconciliationQueryExecuter {
  public Map<String,QueryResults> excute(Map<String,Query> query) throws IOException;
}
