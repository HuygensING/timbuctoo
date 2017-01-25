package nl.knaw.huygens.timbuctoo.solr;

import java.io.IOException;

public interface SolrWebhook {

  void startIndexingForVre(String vreName) throws IOException;
}
