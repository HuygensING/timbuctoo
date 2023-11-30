package nl.knaw.huygens.timbuctoo.webhook;

import java.io.IOException;

public interface Webhooks {
  void dataSetUpdated(String dataSetId) throws IOException;
}
