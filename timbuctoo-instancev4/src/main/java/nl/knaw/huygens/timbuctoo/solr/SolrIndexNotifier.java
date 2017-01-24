package nl.knaw.huygens.timbuctoo.solr;

import java.io.IOException;

public interface SolrIndexNotifier {

  void startIndexingForVre(String vreName) throws IOException;

  boolean isEnabled();
}
